package net.meowcorp.mod.rewind;

import net.fabricmc.api.ModInitializer;

import net.meowcorp.mod.rewind.command.CommandRegistry;
import net.meowcorp.mod.rewind.util.DatabaseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rewind implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("rewind");
	public static final String DATABASE_PATH = "jdbc:sqlite:rewind.db";
	public static final String MOD_ID = "rewind";

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		DatabaseHelper.initializeDatabase();

		// Registry
		CommandRegistry.register();
	}
}