package net.meowcorp.mod.rewind.util;

import net.minecraft.network.packet.Packet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketLogger {
	private final BlockingQueue<Packet<?>> queue = new LinkedBlockingQueue<>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public PacketLogger() {

	}
}
