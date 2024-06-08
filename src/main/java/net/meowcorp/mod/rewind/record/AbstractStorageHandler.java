package net.meowcorp.mod.rewind.record;

import net.meowcorp.mod.rewind.mixin.EntityS2CPacketAccessor;
import net.meowcorp.mod.rewind.util.IStorageStrategy;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractStorageHandler<T extends Packet<?>> implements IStorageStrategy<T> {
	public abstract void store(Connection connection, T packet) throws SQLException;

	/**
	 * Insert a base packet into the database
	 * @param connection JDBC connection
	 * @param packet packet to insert
	 * @return the id of the inserted packet
	 */
	protected int insertBasePacket(Connection connection, T packet) throws SQLException {
		String sql = "INSERT INTO BasePackets DEFAULT VALUES";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.executeUpdate();
		return statement.getGeneratedKeys().getInt(1);
	}

	public static class EntityS2CPacketStrategy extends AbstractStorageHandler<EntityS2CPacket> {

		@Override
		public void store(Connection connection, EntityS2CPacket packet) throws SQLException{
			final int id = ((EntityS2CPacketAccessor) packet).getId();
			final short deltaX = packet.getDeltaX();
			final short deltaY = packet.getDeltaY();
			final short deltaZ = packet.getDeltaZ();
			final byte yaw = packet.getYaw();
			final byte pitch = packet.getPitch();
			final boolean onGround = packet.isOnGround();
			final boolean rotate = packet.hasRotation();
			final boolean positionChanged = packet.isPositionChanged();

			// EntityS2CPacket.Rotate
			if (rotate && !positionChanged) {
				// store rotation
				String sql = "INSERT INTO EntityS2CPacket (entityId, yaw, pitch, onGround) VALUES (?, ?, ?, ?)";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setInt(1, id);
				statement.setByte(2, yaw);
				statement.setByte(3, pitch);
				statement.setBoolean(4, onGround);
				statement.executeUpdate();
			}

			// EntityS2CPacket.MoveRelative
			if (positionChanged && !rotate) {
				// store position
				String sql = "INSERT INTO EntityS2CPacket (entityId, dX, dY, dZ, onGround) VALUES (?, ?, ?, ?, ?)";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setInt(1, id);
				statement.setShort(2, deltaX);
				statement.setShort(3, deltaY);
				statement.setShort(4, deltaZ);
				statement.setBoolean(5, onGround);
				statement.executeUpdate();
			}

			// EntityS2CPacket.RotateAndMoveRelative
			if (positionChanged && rotate) {
				// store both
				String sql = "INSERT INTO EntityS2CPacket (entityId, dX, dY, dZ, yaw, pitch, onGround) VALUES (?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setInt(1, id);
				statement.setShort(2, deltaX);
				statement.setShort(3, deltaY);
				statement.setShort(4, deltaZ);
				statement.setByte(5, yaw);
				statement.setByte(6, pitch);
				statement.setBoolean(7, onGround);
				statement.executeUpdate();
			}
		}
	}
}
