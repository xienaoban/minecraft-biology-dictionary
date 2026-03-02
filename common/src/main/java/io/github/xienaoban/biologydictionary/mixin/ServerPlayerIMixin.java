package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerIMixin {
    @Accessor("server")
    MinecraftServer biologydictionary$getServer();

    @Accessor("containerCounter")
    int biologydictionary$getContainerCounter();

    @Invoker("nextContainerCounter")
    void biologydictionary$invokeNextContainerCounter();

    @Invoker("initMenu")
    void biologydictionary$invokeInitMenu(AbstractContainerMenu abstractContainerMenu);
}
