package net.meowcorp.mod.rewind.packet;

import com.google.gson.JsonObject;
import net.meowcorp.mod.rewind.Rewind;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;

import java.util.HashMap;
import java.util.Map;

public class PacketSerializer {
	private final Map<Class<? extends Packet<?>>, PacketStrategy> serializers = new HashMap<>();

	public PacketSerializer() {
		serializers.put(EntityS2CPacket.MoveRelative.class, new PacketStrategy.EMoveRelative());
		serializers.put(EntityS2CPacket.Rotate.class, new PacketStrategy.ERotate());
		serializers.put(EntityS2CPacket.RotateAndMoveRelative.class, new PacketStrategy.ERotateAndMoveRelative());
	}

	public JsonObject serialize(Packet<?> packet) {
		PacketStrategy strategy = serializers.get(packet.getClass());
		if (strategy != null) return strategy.serialize(packet);

		Rewind.LOGGER.warn("No serializer found for packet: {}", packet.getClass().getSimpleName());
		return null;
	}

	public Packet<?> deserialize(String packetType, JsonObject json) {
		try {
			Class<?> clazz = Class.forName(packetType);
			if (Packet.class.isAssignableFrom(clazz)) {
				@SuppressWarnings("unchecked")
				Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>) clazz;

				PacketStrategy strategy = serializers.get(packetClass);
				if (strategy != null) return strategy.deserialize(json);

				Rewind.LOGGER.warn("No deserializer found for packet type: {}", packetType);
				return null;
			} else {
				Rewind.LOGGER.error("Class is not a packet: {}", packetType);
				return null;
			}

		} catch (ClassNotFoundException e) {
			Rewind.LOGGER.error("Failed to find packet class: {}", packetType);
			return null;
		}
	}
}
