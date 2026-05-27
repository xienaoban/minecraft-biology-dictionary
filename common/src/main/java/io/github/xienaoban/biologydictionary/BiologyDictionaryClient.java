package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.core.widget.EntityPropertyWidgets;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.client.ClientEventRegistry;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

@ClientOnly
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

        ClientEventRegistry.registerWorldConnected(client -> {
            WorldSession.init(ClientUtils.getClientLevel(client));
            ClientWorldSession.init();
            // Only request server configs from remote servers, not local servers.
            if (!ClientUtils.isLocalServer(client)) { ClientNetManager.requestServerConfigs(); }
        });
        ClientEventRegistry.registerWorldDisconnecting(client -> {
            ClientWorldSession.deinit();
            WorldSession.deinit();
            ConfigsManager.setLocalServerConfigs();
            hitEntity = null;
            hitBlock = null;
            hitEntityProperties = null;
        });
        ClientEventRegistry.registerEndTick(client -> {
            tick(client);
            ClientWorldSession cws = ClientWorldSession.get();
            if (cws != null) { cws.tick(); }
        });

        EntityPropertyWidgets.init();
        KeyMappingManager.init();
        ClientNetManager.init();

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
            ClientUtils.sendCenteredMessage(text);
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

    public static void printThrowableToLoggerAndGame(String message, Throwable throwable) {
        LOGGER.error("{}", message, throwable);
        ClientUtils.sendTextBoxMessage(TextUtils.concat(
                Arrays.asList(
                        TextUtils.translate(Lang.TEXT_INFO_FROM_THIS_MOD).withStyle(ChatFormatting.DARK_GREEN),
                        TextUtils.literal(message).withStyle(ChatFormatting.RED),
                        TextUtils.newline(),
                        TextUtils.literal(throwable.toString()).withStyle(ChatFormatting.RED),
                        TextUtils.newline(),
                        TextUtils.translate(Lang.TEXT_PLEASE_REPORT_ISSUE).withStyle(ChatFormatting.GOLD)
                )
        ));
    }
}
