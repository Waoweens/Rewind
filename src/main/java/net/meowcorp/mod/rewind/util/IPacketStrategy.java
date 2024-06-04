package net.meowcorp.mod.rewind.util;

import com.google.gson.JsonObject;
import net.minecraft.network.packet.Packet;

public interface IPacketStrategy<T extends Packet<?>> {
	JsonObject serialize(Packet<?> packet);
	T deserialize(JsonObject json);
}
