package net.meowcorp.mod.rewind.record.strategies;

import net.meowcorp.mod.rewind.mixin.EntityS2CPacketAccessor;
import net.meowcorp.mod.rewind.record.AbstractPacketRecorder;
import net.meowcorp.mod.rewind.util.PacketData;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntityS2CPacketStrategy extends AbstractPacketRecorder<EntityS2CPacket> {
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
			String sql = "INSERT INTO EntityS2CPacket (id, type, entityId, yaw, pitch, onGround) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, basePacketId);
			statement.setInt(2, 0);
			statement.setInt(3, id);
			statement.setByte(4, yaw);
			statement.setByte(5, pitch);
			statement.setBoolean(6, onGround);
			statement.executeUpdate();
		}

		// EntityS2CPacket.MoveRelative
		if (positionChanged && !rotate) {
			// store position
			String sql = "INSERT INTO EntityS2CPacket (id, type, entityId, dX, dY, dZ, onGround) VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, basePacketId);
			statement.setInt(2, 1);
			statement.setInt(3, id);
			statement.setShort(4, deltaX);
			statement.setShort(5, deltaY);
			statement.setShort(6, deltaZ);
			statement.setBoolean(7, onGround);
			statement.executeUpdate();
		}

		// EntityS2CPacket.RotateAndMoveRelative
		if (positionChanged && rotate) {
			// store both
			String sql = "INSERT INTO EntityS2CPacket (id, type, entityId, dX, dY, dZ, yaw, pitch, onGround) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, basePacketId);
			statement.setInt(2, 2);
			statement.setInt(3, id);
			statement.setShort(4, deltaX);
			statement.setShort(5, deltaY);
			statement.setShort(6, deltaZ);
			statement.setByte(7, yaw);
			statement.setByte(8, pitch);
			statement.setBoolean(9, onGround);
			statement.executeUpdate();
		}
	}

	@Override
	public List<PacketData> retrieve(Connection connection, int seconds) throws SQLException {
		List<PacketData> packets = new ArrayList<>();
		String sql = """
				SELECT bp.id, bp.timestamp,
				       ep.entityId,
				       ep.dX, ep.dY, ep.dZ,
				       ep.yaw, ep.pitch,
				       ep.onGround
				FROM BasePackets bp
				LEFT JOIN EntityS2CPacket ep ON bp.id = ep.id
				WHERE bp.timestamp >= datetime('NOW', ?)
				""";

		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, String.format("-%d seconds", seconds));
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Timestamp timestamp = rs.getTimestamp("timestamp");
				Packet<?> packet = deserializePacket(rs);
				packets.add(new PacketData(timestamp, packet));
			}
		}

		return packets;
	}

	private Packet<?> deserializePacket(ResultSet rs) throws SQLException {
		final int type = rs.getInt("type");
		final int entityId = rs.getInt("entityId");
		final short deltaX = rs.getShort("dX");
		final short deltaY = rs.getShort("dY");
		final short deltaZ = rs.getShort("dZ");
		final byte yaw = rs.getByte("yaw");
		final byte pitch = rs.getByte("pitch");
		final boolean onGround = rs.getBoolean("onGround");

		return switch (type) {
			case 0 -> new EntityS2CPacket.Rotate(entityId, yaw, pitch, onGround);
			case 1 -> new EntityS2CPacket.MoveRelative(entityId, deltaX, deltaY, deltaZ, onGround);
			case 2 -> new EntityS2CPacket.RotateAndMoveRelative(entityId, deltaX, deltaY, deltaZ, yaw, pitch, onGround);
			default -> throw new SQLException("Invalid EntityS2CPacket type: " + type);
		};
	}
}
