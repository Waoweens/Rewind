package net.meowcorp.mod.rewind;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.meowcorp.mod.rewind.command.CommandRegistry;
import net.meowcorp.mod.rewind.database.PacketSerializer;
import net.meowcorp.mod.rewind.database.PacketDatabase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Rewind implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("rewind");
	public static String DATABASE_PATH;
	public static final String MOD_ID = "rewind";

	private static PacketDatabase packetLogger;
	public static PacketSerializer SERIALIZER = new PacketSerializer();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		LOGGER.info("Rewind packet logger initialized");

		// Registry
		CommandRegistry.register();

		// events
		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
			Path worldPath = server.getSavePath(WorldSavePath.ROOT);

			String worldPathString = worldPath.resolve("rewind.db").toString();
			DATABASE_PATH = "jdbc:sqlite:" + worldPathString;

			packetLogger = new PacketDatabase();
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			packetLogger.shutdown();
			LOGGER.info("Rewind packet logger shutdown");
		});
	}

	public static PacketDatabase getPacketLogger() {
		return packetLogger;
	}
}