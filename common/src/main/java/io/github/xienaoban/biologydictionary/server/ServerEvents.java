package io.github.xienaoban.biologydictionary.server;

import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class ServerEvents {
	@PlatformEntry
	public static final List<ServerListener> STARTED = List.of();

	@PlatformEntry
	public static final List<ServerListener> STOPPING = List.of();

	@PlatformEntry
	public static final List<PlayerListener> PLAYER_LOGGED_IN = List.of();

	private ServerEvents() {}

	@FunctionalInterface
	public interface ServerListener {
		void run(MinecraftServer server);
	}

	@FunctionalInterface
	public interface PlayerListener {
		void run(ServerPlayer player);
	}
}
