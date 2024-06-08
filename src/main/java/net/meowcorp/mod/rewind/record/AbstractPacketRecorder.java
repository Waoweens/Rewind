package net.meowcorp.mod.rewind.record;

import net.meowcorp.mod.rewind.util.IStorageStrategy;
import net.minecraft.network.packet.Packet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractPacketRecorder<T extends Packet<?>> implements IStorageStrategy<T> {
	public abstract void store(Connection connection, T packet) throws SQLException;

	/**
	 * Insert a base packet into the database
	 * @param connection JDBC connection
	 * @param packet packet to insert
	 * @return the id of the inserted packet
	 */
	protected int insertBasePacket(Connection connection, T packet) throws SQLException {
		String sql = "INSERT INTO BasePackets DEFAULT VALUES";
		PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
		statement.executeUpdate();
		ResultSet rs = statement.getGeneratedKeys();
		if (rs.next()) {
			return rs.getInt(1);
		}
		throw new SQLException("Failed to insert base packet. No id returned.");
	}
}
