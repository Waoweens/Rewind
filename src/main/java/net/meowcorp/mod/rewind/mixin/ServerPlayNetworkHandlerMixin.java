package net.meowcorp.mod.rewind.mixin;

import net.meowcorp.mod.rewind.Rewind;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow public ServerPlayerEntity player;

	// log packets sent by the server
	@Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"))
	private void onSendPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
		Class<?> packetClass = packet.getClass();
		World world = player.getServerWorld();

		Rewind.LOGGER.info("Sent packet: {}", packet.getClass().getName());

		// get id field from packet if exists
		try {
			Field inField = packetClass.getDeclaredField("id");
			inField.setAccessible(true);

			int entityId = inField.getInt(packet);

			Entity entity = world.getEntityById(entityId);
			if (entity != null) Rewind.LOGGER.info("Entity name: {}", entity.getName().getString());
		} catch (NoSuchFieldException ignored) {} catch (IllegalAccessException e) {
			Rewind.LOGGER.error("Failed to access 'id' field on packet {}", packet.getClass().getSimpleName());
		}


		logFields(packet, packetClass);
	}

	@Unique
	private void logFields(Object packet, Class<?> packetClass) {

		if (packetClass == null || packetClass == Object.class) return;

		for(Field field : packetClass.getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Rewind.LOGGER.info("Field: {} = {}", field.getName(), field.get(packet));
			} catch (IllegalAccessException e) {
				Rewind.LOGGER.error("Failed to access field: {}", field.getName());
			}
		}

		logFields(packet, packetClass.getSuperclass());
	}

}
