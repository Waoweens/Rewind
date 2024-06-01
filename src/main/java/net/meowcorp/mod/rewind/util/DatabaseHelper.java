package net.meowcorp.mod.rewind.util;

import net.meowcorp.mod.rewind.Rewind;

import java.io.File;
import java.sql.*;

public class DatabaseHelper {
	public static void initializeDatabase() {
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
}
