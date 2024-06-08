package net.meowcorp.mod.rewind.util;

import net.minecraft.network.packet.Packet;

import java.sql.Timestamp;

public class PacketData<T extends Packet<?>> {
	public Timestamp timestamp;
	public T packet;

	public PacketData(Timestamp timestamp, T packet) {
		this.timestamp = timestamp;
		this.packet = packet;
	}
}
