package net.meowcorp.mod.rewind.util;

import net.minecraft.network.packet.Packet;

import java.sql.Connection;
import java.sql.SQLException;

public interface IStorageStrategy<T extends Packet<?>> {
	void store(Connection connection, T packet) throws SQLException;
}
