package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.client.FirstPersonShoulderEntityRenderer;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import io.github.xienaoban.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.biologydictionary.common.client.ClientEventRegistry;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.EntityPropertyWidgets;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;

import java.util.List;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

@Environment(EnvType.CLIENT)
public final class BiologyDictionaryClient {
    public static final BiologyDictionaryClient BDC = new BiologyDictionaryClient();

    private int ticks;

    private Entity hitEntity;
    private BlockPos hitBlock;

    private EntityProperties<? extends Entity> hitEntityProperties;

    private BiologyDictionaryClient() {
        hitEntity = null;
        hitBlock = null;
        hitEntityProperties = null;

        ClientEventRegistry.registerWorldConnected(client -> EntityManager.init());
        ClientEventRegistry.registerWorldDisconnecting(client -> EntityManager.destroy());
        ClientEventRegistry.registerEndTick(this::tick);

        EntityPropertyWidgets.init();
        FirstPersonShoulderEntityRenderer.init();
        KeyMappingManager.init();
        ClientNetManager.init();
        HighlightManager.init();

        LOGGER.info("BiologyDictionary (client) initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }

    public int getTicks() { return ticks; }

    public Entity getHitEntity() { return hitEntity; }
    public void setHitEntity(Entity hitEntity) { this.hitEntity = hitEntity; }

    public BlockPos getHitBlock() { return hitBlock; }
    public void setHitBlock(BlockPos hitBlock) { this.hitBlock = hitBlock; }


    public EntityProperties<? extends Entity> getHitEntityProperties() { return hitEntityProperties; }
    public void setHitEntityProperties(EntityProperties<? extends Entity> hitEntityProperties) { this.hitEntityProperties = hitEntityProperties; }

    private void tick(Minecraft client) {
        if (!client.isPaused()) ++ticks;
    }

    public static void sendCenteredMessage(Component text) {
        if (ClientUtils.getCurrentScreen() instanceof AbstractBiologyDictionaryScreen screen) {
            screen.sendScreenMessage(text);
        } else {
            ClientUtils.sendCenteredMessage(ComponentUtils.formatList(
                    List.of(Component.translatable(Lang.TEXT_INFO_FROM_THIS_MOD).withStyle(ChatFormatting.DARK_GREEN), text),
                    Component.empty()
            ));
        }
    }

    public static void sendCenteredInfo(MutableComponent text) {
        sendCenteredMessage(text.withStyle(ChatFormatting.WHITE));
    }

    public static void sendCenteredWarning(MutableComponent text) {
        sendCenteredMessage(text.withStyle(ChatFormatting.YELLOW));
    }

    public static void sendCenteredError(MutableComponent text) {
        sendCenteredMessage(text.withStyle(ChatFormatting.RED));
    }

    public static void printThrowableToLoggerAndGame(Throwable throwable) {
        String errStack = Misc.getStackToString(throwable);
        LOGGER.error(errStack);
        ClientUtils.sendTextBoxMessage(ComponentUtils.formatList(
                List.of(
                        Component.translatable(Lang.TEXT_INFO_FROM_THIS_MOD).withStyle(ChatFormatting.DARK_GREEN),
                        Component.literal(throwable.toString()).withStyle(ChatFormatting.RED)
                ),
                Component.empty()
        ));
    }
}
