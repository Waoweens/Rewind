package net.meowcorp.mod.rewind.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.meowcorp.mod.rewind.Rewind;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PacketDatabase {
	private final BlockingQueue<PacketData> queue = new LinkedBlockingQueue<>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Gson gson = new Gson();

	public PacketDatabase() {
		initializeDatabase();
		startWorker();
	}

	public static class PacketData {
		public String packetType;
		public JsonObject json;

		PacketData(String packetType, JsonObject json) {
			this.packetType = packetType;
			this.json = json;
		}
	}

	private void initializeDatabase() {
		try (Connection conn = DriverManager.getConnection(Rewind.DATABASE_PATH)) {
			if (conn != null) {
				try (Statement statement = conn.createStatement()) {
					String sql =
							"CREATE TABLE IF NOT EXISTS packets (" +
									"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
									"timestamp DATETIME DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'NOW')), " +
									"packetType TEXT NOT NULL, " +
									"packetData TEXT NOT NULL)";
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
					PacketData packetData = queue.take();
					storePacket(packetData.packetType, packetData.json);
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

	public void logPacket(String packetType, JsonObject json) {
		if (!queue.offer(new PacketData(packetType, json))) {
			Rewind.LOGGER.error("Failed to enqueue packet");
		}
	}

	private void storePacket(String packetType, JsonObject json) {
		String packetData = json.toString();

		String sql = "INSERT INTO packets (packetType, packetData) VALUES (?, ?)";
		try (Connection conn = DriverManager.getConnection(Rewind.DATABASE_PATH);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, packetType);
			pstmt.setString(2, packetData);
			pstmt.executeUpdate();

		} catch (SQLException e) {
			Rewind.LOGGER.error("Failed to store packet: {}", e.getMessage());
		}
	}

	public List<PacketData> getPacketsSec(int seconds) {
		List<PacketData> packets = new ArrayList<>();
		String sql = "SELECT * FROM packets  WHERE timestamp >= datetime('now', ?)";

		try(Connection conn = DriverManager.getConnection(Rewind.DATABASE_PATH);
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, "-" + seconds + " seconds");
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String packetData = rs.getString("packetData");
				String packetType = rs.getString("packetType");
				JsonObject json = gson.fromJson(packetData, JsonObject.class);
				packets.add(new PacketData(packetType, json));

				Rewind.LOGGER.info("Packet: [{}] {} {}", rs.getDate("timestamp"), packetType, packetData);
			}

		} catch (SQLException e) {
			Rewind.LOGGER.error("Failed to get packets: {}", e.getMessage());
		}

		return packets;
	}
}
