package net.meowcorp.mod.rewind.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class TestCommand {
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				CommandManager.literal("testCommand")
						.then(CommandManager.literal("hello")
								.then(CommandManager.argument("argument", StringArgumentType.string())
										.executes(context -> {
											final String argument = StringArgumentType.getString(context, "argument");
											context.getSource().sendFeedback(() -> Text.literal("Hello, " + argument + "!"), false);
											return 1;
										})))));
	}
}
