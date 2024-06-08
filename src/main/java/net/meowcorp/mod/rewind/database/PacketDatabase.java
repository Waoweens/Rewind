package net.meowcorp.mod.rewind.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.meowcorp.mod.rewind.Rewind;
import net.meowcorp.mod.rewind.record.PacketRecorderFactory;
import net.meowcorp.mod.rewind.util.IStorageStrategy;
import net.minecraft.network.packet.Packet;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PacketDatabase {
	private final BlockingQueue<Packet<?>> queue = new LinkedBlockingQueue<>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private PacketRecorderFactory recorderFactory = new PacketRecorderFactory();

	public PacketDatabase() {
		initializeDatabase();
		startWorker();
	}

//	public static class PacketData {
//		public String packetType;
//		public JsonObject json;
//
//		PacketData(String packetType, JsonObject json) {
//			this.packetType = packetType;
//			this.json = json;
//		}
//	}

	private void initializeDatabase() {
		try (Connection conn = DriverManager.getConnection(Rewind.DATABASE_PATH)) {
			if (conn != null) {
				try (Statement statement = conn.createStatement()) {
					String sql =
"""
CREATE TABLE IF NOT EXISTS BasePackets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp DATETIME DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'NOW'))
);

CREATE TABLE IF NOT EXISTS EntityS2CPacket (
    id INTEGER PRIMARY KEY,
    entityId INTEGER NOT NULL, -- int
    dX INTEGER, -- short
    dY INTEGER, -- short
    dZ INTEGER, -- short
    yaw INTEGER, -- byte
    pitch INTEGER, -- byte
    onGround INTEGER, -- boolean
    FOREIGN KEY (id) REFERENCES BasePackets(id)
);
""";
					statement.execute(sql);
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

//	public List<PacketData> getPacketsSec(int seconds) {
//		List<PacketData> packets = new ArrayList<>();
//		String sql = "SELECT * FROM packets  WHERE timestamp >= datetime('now', ?)";
//
//		try(Connection conn = DriverManager.getConnection(Rewind.DATABASE_PATH);
//			PreparedStatement pstmt = conn.prepareStatement(sql)) {
//			pstmt.setString(1, "-" + seconds + " seconds");
//			ResultSet rs = pstmt.executeQuery();
//
//			while (rs.next()) {
//				String packetData = rs.getString("packetData");
//				String packetType = rs.getString("packetType");
//				JsonObject json = gson.fromJson(packetData, JsonObject.class);
//				packets.add(new PacketData(packetType, json));
//
//				Rewind.LOGGER.info("Packet: [{}] {} {}", rs.getDate("timestamp"), packetType, packetData);
//			}
//
//		} catch (SQLException e) {
//			Rewind.LOGGER.error("Failed to get packets: {}", e.getMessage());
//		}
//
//		return packets;
//	}
}
