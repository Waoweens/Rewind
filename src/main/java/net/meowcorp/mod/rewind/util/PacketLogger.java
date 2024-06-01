package net.meowcorp.mod.rewind.util;

import io.netty.buffer.Unpooled;
import net.meowcorp.mod.rewind.Rewind;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.concurrent.*;

public class PacketLogger {
	private final BlockingQueue<Packet<?>> queue = new LinkedBlockingQueue<>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public PacketLogger() {
		initializeDatabase();
	}

	private void initializeDatabase() {
		try (Connection conn = DriverManager.getConnection(Rewind.DATABASE_PATH)) {
			if (conn != null) {
				DatabaseMetaData meta = conn.getMetaData();
				try (Statement statement = conn.createStatement()) {
					String sql =
							"CREATE TABLE IF NOT EXISTS packets (" +
									"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
									"timestamp DATETIME DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'NOW')), " +
									"packetType TEXT NOT NULL, " +
									"packetData BLOB)";
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
					Packet<?> packet = queue.take();
					storePacket(packet);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
	}

	public void shutdown() {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
				executor.shutdownNow();
				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
					Rewind.LOGGER.error("Executor did not terminate");
				}
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	public byte[] serializePacket(Packet<?> packet) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		packet.write(buf);

		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);
		return data;
	}

	// FIXME: this is very fucking cursed and unsafe
	public Packet<?> deserializePacket(String packetType, byte[] data) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(data));
		try {
			Class<?> packetClass = Class.forName(packetType);
			Method readMethod = packetClass.getMethod("read", PacketByteBuf.class);
			Packet<?> packet = (Packet<?>) packetClass.getDeclaredConstructor().newInstance();
			readMethod.invoke(packet, buf);
			return packet;
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
				 IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void logPacket(Packet<?> packet) {
		if (!queue.offer(packet)) {
			Rewind.LOGGER.error("Failed to enqueue packet");
		}
	}

	private void storePacket(Packet<?> packet) {
		String sql = "INSERT INTO packets (packetType, packetData) VALUES (?, ?)";

		try (Connection conn = DriverManager.getConnection(Rewind.DATABASE_PATH);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, packet.getClass().getName());
			pstmt.setBytes(2, serializePacket(packet));
			pstmt.executeUpdate();

		} catch (SQLException e) {
			Rewind.LOGGER.error("Failed to store packet: {}", e.getMessage());
		}
	}
}
