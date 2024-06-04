package net.meowcorp.mod.rewind.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.meowcorp.mod.rewind.util.IPacketStrategy;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;

public class PacketStrategy<T extends Packet<?>> implements IPacketStrategy<T> {
	private final Class<T> packetClass;
	private static final Gson gson = new Gson();

	public PacketStrategy(Class<T> packetClass) {
		this.packetClass = packetClass;
	}

	/**
	 * Serialize a packet to a JSON object
	 * @param packet generic {@link Packet} type
	 * @return {@link JsonObject} containing packet data (fields)
	 */
	public JsonObject serialize(Packet<?> packet) {
		// default
		return gson.toJsonTree(packet).getAsJsonObject();
	}

	/**
	 * Deserialize a JSON object to a packet
	 * @param json {@link JsonObject} containing packet data
	 * @return {@link Packet}-type class with fields from {@code json}
	 */
	public T deserialize(JsonObject json) {
		return gson.fromJson(json, packetClass);
	}

}