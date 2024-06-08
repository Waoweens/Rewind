package net.meowcorp.mod.rewind.record.strategies;

import net.meowcorp.mod.rewind.mixin.EntityS2CPacketAccessor;
import net.meowcorp.mod.rewind.record.AbstractPacketRecorder;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EntityS2CPacketStrategy  extends AbstractPacketRecorder<EntityS2CPacket> {
	@Override
	public void store(Connection connection, EntityS2CPacket packet) throws SQLException {
		final int basePacketId = insertBasePacket(connection, packet);
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
			String sql = "INSERT INTO EntityS2CPacket (id, entityId, yaw, pitch, onGround) VALUES (?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, basePacketId);
			statement.setInt(2, id);
			statement.setByte(3, yaw);
			statement.setByte(4, pitch);
			statement.setBoolean(5, onGround);
			statement.executeUpdate();
		}

		// EntityS2CPacket.MoveRelative
		if (positionChanged && !rotate) {
			// store position
			String sql = "INSERT INTO EntityS2CPacket (id, entityId, dX, dY, dZ, onGround) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, basePacketId);
			statement.setInt(2, id);
			statement.setShort(3, deltaX);
			statement.setShort(4, deltaY);
			statement.setShort(5, deltaZ);
			statement.setBoolean(6, onGround);
			statement.executeUpdate();
		}

		// EntityS2CPacket.RotateAndMoveRelative
		if (positionChanged && rotate) {
			// store both
			String sql = "INSERT INTO EntityS2CPacket (id, entityId, dX, dY, dZ, yaw, pitch, onGround) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, basePacketId);
			statement.setInt(2, id);
			statement.setShort(3, deltaX);
			statement.setShort(4, deltaY);
			statement.setShort(5, deltaZ);
			statement.setByte(6, yaw);
			statement.setByte(7, pitch);
			statement.setBoolean(8, onGround);
			statement.executeUpdate();
		}
	}
}
