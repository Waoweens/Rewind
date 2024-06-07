package net.meowcorp.mod.rewind.command;

public class CommandRegistry {
	public static void register() {
		RewindCommand.register();
		TestCommand.register();
	}
}
