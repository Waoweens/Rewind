package net.meowcorp.mod.rewind.record;

import org.intellij.lang.annotations.Language;


public class SQLQueries {
	@Language("SQL")
	public static final String CREATE_BASE_PACKETS = """
			CREATE TABLE IF NOT EXISTS BasePackets (
			    			id INTEGER PRIMARY KEY AUTOINCREMENT,
			    			timestamp DATETIME DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'NOW'))
						)
			""";

	@Language("SQL")
	public static final String CREATE_ENTITY_S2C_PACKET = """
			CREATE TABLE IF NOT EXISTS EntityS2CPacket (
			    id INTEGER PRIMARY KEY,
			    type INTEGER NOT NULL, -- int, 0 = Rotate, 1 = MoveRelative, 2 = RotateAndMoveRelative
			    entityId INTEGER NOT NULL, -- int
			    dX INTEGER, -- short
			    dY INTEGER, -- short
			    dZ INTEGER, -- short
			    yaw INTEGER, -- byte
			    pitch INTEGER, -- byte
			    onGround INTEGER, -- boolean
			    FOREIGN KEY (id) REFERENCES BasePackets(id)
			)
			""";

	@Language("SQL")
	public static final String QUERY_PACKETS_BY_TIME = """
			SELECT
				-- BasePackets
				BasePackets.id, BasePackets.timestamp,
				-- EntityS2CPacket
				EntityS2CPacket.entityId, EntityS2CPacket.type,
				EntityS2CPacket.dX, EntityS2CPacket.dY, EntityS2CPacket.dZ,
				EntityS2CPacket.yaw, EntityS2CPacket.pitch, EntityS2CPacket.onGround
			FROM BasePackets
			LEFT JOIN EntityS2CPacket ON BasePackets.id = EntityS2CPacket.id
			WHERE BasePackets.timestamp >= datetime('NOW', ?)
			""";
}
