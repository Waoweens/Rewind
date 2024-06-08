package net.meowcorp.mod.rewind.util;

import net.minecraft.network.packet.Packet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IStorageStrategy<T extends Packet<?>> {
	void store(Connection connection, T packet) throws SQLException;
	List<PacketData> retrieve(Connection connection, int seconds) throws SQLException;
}
