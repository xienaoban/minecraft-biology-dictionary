package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerIMixin {
    @Accessor
    MinecraftServer getServer();

    @Accessor
    int getContainerCounter();

    @Invoker
    void invokeNextContainerCounter();

    @Invoker
    void invokeInitMenu(AbstractContainerMenu abstractContainerMenu);
}
