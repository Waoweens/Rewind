package net.meowcorp.mod.rewind.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.meowcorp.mod.rewind.util.IPacketStrategy;
import net.minecraft.network.packet.Packet;

public abstract class PacketStrategy implements IPacketStrategy {
	protected Gson gson = new Gson();

	@Override
	public JsonObject serialize(Packet<?> packet) {
		// default
		return gson.toJsonTree(packet).getAsJsonObject();
	}

	@Override
	public abstract Packet<?> deserialize(JsonObject json);

	// Subclasses

	public static class EMoveRelative extends PacketStrategy {
		@Override
		public Packet<?> deserialize(JsonObject json) {
			return null;
		}
	}

	public static class ERotate extends PacketStrategy {
		@Override
		public Packet<?> deserialize(JsonObject json) {
			return null;
		}
	}

	public static 	class ERotateAndMoveRelative extends PacketStrategy {
		@Override
		public Packet<?> deserialize(JsonObject json) {
			return null;
		}
	}
}