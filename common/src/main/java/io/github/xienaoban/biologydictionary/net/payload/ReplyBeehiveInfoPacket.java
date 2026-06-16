package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record ReplyBeehiveInfoPacket(CompoundTag bees) implements Packet {
	public static final Packet.Factory<ReplyBeehiveInfoPacket> FACTORY = ReplyBeehiveInfoPacket::new;

	private ReplyBeehiveInfoPacket(FriendlyByteBuf buf) {
		this(buf.readNbt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeNbt(bees);
	}

	@ClientOnly
	@Override
	public void clientReceive(ClientNetApi.Context ctx) {
		BiologyDictionaryClient.handleBeehiveInfo(bees);
	}
}
