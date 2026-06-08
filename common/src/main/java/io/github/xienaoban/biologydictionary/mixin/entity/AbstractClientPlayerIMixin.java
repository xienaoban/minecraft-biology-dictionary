package io.github.xienaoban.biologydictionary.mixin.entity;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@ClientOnly
@Mixin(AbstractClientPlayer.class)
public interface AbstractClientPlayerIMixin {
    @Invoker("getPlayerInfo")
    PlayerInfo biologydictionary$invokeGetPlayerInfo();
}
