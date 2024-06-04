package net.meowcorp.mod.rewind.util;

import com.google.gson.JsonObject;
import net.minecraft.network.packet.Packet;

public interface IPacketStrategy {
	JsonObject serialize(Packet<?> packet);
	Packet<?> deserialize(JsonObject json);
}
