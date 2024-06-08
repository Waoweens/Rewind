package net.meowcorp.mod.rewind.database;

import net.meowcorp.mod.rewind.Rewind;
import net.meowcorp.mod.rewind.record.PacketRecorderFactory;
import net.meowcorp.mod.rewind.record.SQLQueries;
import net.meowcorp.mod.rewind.util.IStorageStrategy;
import net.meowcorp.mod.rewind.util.PacketData;
import net.minecraft.network.packet.Packet;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class PacketDatabase {
	private final BlockingQueue<Packet<?>> queue = new LinkedBlockingQueue<>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final PacketRecorderFactory recorderFactory = new PacketRecorderFactory();

	public PacketDatabase() {
		initializeDatabase();
		startWorker();
	}

	private void initializeDatabase() {
		try (Connection conn = DriverManager.getConnection(Rewind.DATABASE_PATH)) {
			if (conn != null) {
				try (Statement statement = conn.createStatement()) {
					statement.execute(SQLQueries.CREATE_BASE_PACKETS);
					statement.execute(SQLQueries.CREATE_ENTITY_S2C_PACKET);
					Rewind.LOGGER.info("Database initialized successfully: {}", new File(Rewind.DATABASE_PATH).getAbsolutePath());
				}
			}
		} catch (SQLException e) {
			Rewind.LOGGER.error("Failed to connect to database: {}", e.getMessage());
		}
	}

	private void startWorker() {
		executor.submit(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
//					PacketData packetData = queue.take();
					storePacket(queue.take());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
	}

	public void shutdown() {
		executor.shutdown();
		try {
			// TODO: don't use a fixed timeout
			if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
				executor.shutdownNow();
				if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
					Rewind.LOGGER.error("Executor did not terminate");
				}
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	public <T extends Packet<?>> void logPacket(T packet) {
		if (!queue.offer(packet)) {
			Rewind.LOGGER.error("Failed to enqueue packet");
		}
	}

	private <T extends Packet<?>> void storePacket(T packet) {
		IStorageStrategy<T> recorder = recorderFactory.getRecorder(packet);
		if (recorder != null) {
			Connection conn = null;
			try {
				conn = DriverManager.getConnection(Rewind.DATABASE_PATH);
				conn.setAutoCommit(false);
				recorder.store(conn, packet);
				conn.commit();
			} catch (SQLException e) {
				Rewind.LOGGER.error("Failed to store packet: {}", e.getMessage());
				try {
					if (conn != null) {
						conn.rollback();
					}
				} catch (SQLException ex) {
					Rewind.LOGGER.error("Failed to rollback transaction: {}", ex.getMessage());
				}
			} finally {
				try {
					if (conn != null) {
						conn.setAutoCommit(true);
						conn.close();
					}
				} catch (SQLException e) {
					Rewind.LOGGER.error("Failed to close connection: {}", e.getMessage());
				}
			}
		} else {
			Rewind.LOGGER.error("No recorder found for packet: {}", packet.getClass().getName());
		}
	}

	public List<PacketData<?>> getPacketsSec(int seconds) {
		List<PacketData<?>> packets = new ArrayList<>();
		// get all packets from BasePacket from the last x seconds

		// TODO: build a dynamic query system later
		try (Connection conn = DriverManager.getConnection(Rewind.DATABASE_PATH);
			 PreparedStatement statement = conn.prepareStatement(SQLQueries.QUERY_PACKETS_BY_TIME)) {

			statement.setString(1, String.format("-%d seconds", seconds));
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Timestamp timestamp = rs.getTimestamp("timestamp");
//				Packet<?> packet = deserializePacket(rs);
//				packets.add(new PacketData<>(timestamp, packet));
			}

		} catch (SQLException e) {
			Rewind.LOGGER.error("Failed to get packets: {}", e.getMessage());
		} return null;
	}
}
