package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import io.github.xienaoban.biologydictionary.platform.net.Packet;

import java.util.List;

public final class PacketPayloads {
	private PacketPayloads() {}

	@PlatformEntry
	public static final List<Entry<?>> ENTRIES = List.of();

	public record Entry<T extends Packet>(Class<T> packetClass, Packet.Factory<T> factory) {}
}
