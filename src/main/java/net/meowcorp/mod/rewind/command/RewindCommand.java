package net.meowcorp.mod.rewind.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class RewindCommand {
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("rewind")
				.executes(context -> {
					context.getSource().sendFeedback(() -> Text.literal("Hello, Rewind!"), false);

					return 1;
				})
		));
	}
}
