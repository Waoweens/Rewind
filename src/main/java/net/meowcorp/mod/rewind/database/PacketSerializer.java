package net.meowcorp.mod.rewind.database;

import com.google.gson.JsonObject;
import net.meowcorp.mod.rewind.Rewind;
import net.meowcorp.mod.rewind.util.IPacketStrategy;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;

import java.util.HashMap;
import java.util.Map;

public class PacketSerializer {
	private final Map<String, IPacketStrategy<? extends Packet<?>>> serializers = new HashMap<>();

	public PacketSerializer() {
		registerStrategy(EntityS2CPacket.MoveRelative.class);
		registerStrategy(EntityS2CPacket.Rotate.class);
		registerStrategy(EntityS2CPacket.RotateAndMoveRelative.class);
	}

	private <T extends Packet<?>> void registerStrategy(Class<T> packetClass) {
		serializers.put(packetClass.getName(), new PacketStrategy<>(packetClass));
	}

	public <T extends Packet<?>> JsonObject serialize(T packet) {
		@SuppressWarnings("unchecked")
		IPacketStrategy<T> strategy = (IPacketStrategy<T>) serializers.get(packet.getClass().getName());
		if (strategy != null) return strategy.serialize(packet);

		Rewind.LOGGER.error("No serializer found for packet type: {}", packet.getClass().getName());
		return null;
	}

	public <T extends Packet<?>> T deserialize(String packetType, JsonObject json) {
		@SuppressWarnings("unchecked")
		IPacketStrategy<T> strategy = (IPacketStrategy<T>) serializers.get(packetType);
		if (strategy != null) return strategy.deserialize(json);

		Rewind.LOGGER.error("No deserializer found for packet type: {}", packetType);
		return null;
	}
}
