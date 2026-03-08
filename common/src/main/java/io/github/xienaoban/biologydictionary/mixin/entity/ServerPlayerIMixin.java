package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerIMixin {
    @Accessor("server")
    MinecraftServer biologydictionary$getServer();

    @Accessor("gameMode")
    GameType biologydictionary$getGameMode();

    @Accessor("containerCounter")
    int biologydictionary$getContainerCounter();

    @Invoker("nextContainerCounter")
    void biologydictionary$invokeNextContainerCounter();

    @Invoker("initMenu")
    void biologydictionary$invokeInitMenu(AbstractContainerMenu abstractContainerMenu);
}
