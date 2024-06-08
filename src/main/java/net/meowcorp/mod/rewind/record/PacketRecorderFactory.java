package net.meowcorp.mod.rewind.record;

import net.meowcorp.mod.rewind.record.strategies.EntityS2CPacketStrategy;
import net.meowcorp.mod.rewind.util.IStorageStrategy;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;

import java.util.HashMap;
import java.util.Map;

public class PacketRecorderFactory {
	private Map<Class<? extends Packet<?>>, IStorageStrategy<? extends Packet<?>>> strategies = new HashMap<>();

	public PacketRecorderFactory() {
		strategies.put(EntityS2CPacket.Rotate.class, new EntityS2CPacketStrategy());
		strategies.put(EntityS2CPacket.MoveRelative.class, new EntityS2CPacketStrategy());
		strategies.put(EntityS2CPacket.RotateAndMoveRelative.class, new EntityS2CPacketStrategy());
	}

	@SuppressWarnings("unchecked")
	public <T extends Packet<?>> IStorageStrategy<T> getRecorder(T packet) {
		return (IStorageStrategy<T>) strategies.get(packet.getClass());
	}
}
