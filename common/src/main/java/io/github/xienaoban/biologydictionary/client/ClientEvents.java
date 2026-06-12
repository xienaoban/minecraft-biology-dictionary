package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import net.minecraft.client.Minecraft;

import java.util.List;

@ClientOnly
public final class ClientEvents {
	@PlatformEntry
	public static final List<ClientListener> STARTED = List.of();

	@PlatformEntry
	public static final List<ClientListener> STOPPING = List.of();

	@PlatformEntry
	public static final List<ClientListener> WORLD_CONNECTED = List.of();

	@PlatformEntry
	public static final List<ClientListener> WORLD_DISCONNECTING = List.of();

	@PlatformEntry
	public static final List<ClientListener> END_TICK = List.of();

	private ClientEvents() {}

	@FunctionalInterface
	public interface ClientListener {
		void run(Minecraft client);
	}
}
