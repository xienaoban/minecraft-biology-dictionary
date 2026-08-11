package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.widget.EntityPropertyWidgets;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

@ClientOnly
public final class BiologyDictionaryClient {
    private static final Queue<Component> pendingTextBoxLogs = new ConcurrentLinkedQueue<>();

    private static int ticks;
    private static Entity hitEntity;
    private static BlockPos hitBlock;
    private static EntityProperties<? extends Entity> hitEntityProperties;

    private static boolean showOnlyDiscoveredEntities =
            ConfigsManager.getClient().isShowOnlyDiscoveredEntitiesByDefault();
    private static boolean demoMode = false;
    private static boolean debugMode = false;

    static {
        EntityPropertyWidgets.init();
        LOGGER.info("BiologyDictionary (client) initialized.");
    }

    private BiologyDictionaryClient() {}

    public static void forceInitialize() { /* do nothing but to trigger cinit */ }

    public static void tick() {
        ++ticks;
    }

    public static int getTicks() {
        return ticks;
    }

    public static Entity getHitEntity() {
        return hitEntity;
    }

    public static void setHitEntity(Entity hitEntity) {
        BiologyDictionaryClient.hitEntity = hitEntity;
    }

    public static BlockPos getHitBlock() {
        return hitBlock;
    }

    public static void setHitBlock(BlockPos hitBlock) {
        BiologyDictionaryClient.hitBlock = hitBlock;
    }

    public static EntityProperties<? extends Entity> getHitEntityProperties() {
        return hitEntityProperties;
    }

    public static void setHitEntityProperties(EntityProperties<? extends Entity> hitEntityProperties) {
        BiologyDictionaryClient.hitEntityProperties = hitEntityProperties;
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
        printLogToTextBox(TextUtils.literal(message).withStyle(ChatFormatting.RED), throwable);
    }

    public static void printLogToTextBox(Component message, Throwable throwable) {
        ClientUtils.sendTextBoxMessage(TextUtils.concat(
                Arrays.asList(
                        TextUtils.translate(Lang.TEXT_INFO_FROM_THIS_MOD).withStyle(ChatFormatting.DARK_GREEN),
                        message,
                        throwable == null ? TextUtils.empty() : TextUtils.concat(
                                TextUtils.newline(),
                                TextUtils.literal(throwable.toString()).withStyle(ChatFormatting.RED),
                                TextUtils.newline(),
                                TextUtils.translate(Lang.TEXT_PLEASE_REPORT_ISSUE).withStyle(ChatFormatting.GOLD)
                        )
                )
        ));
    }

    public static void printLogToTextBoxWhenReady(Component text) {
        pendingTextBoxLogs.add(text);
        printPendingTextBoxLogs();
    }

    public static void printPendingTextBoxLogs() {
        if (ClientWorldSession.get() == null) { return; }

        Component text;
        while ((text = pendingTextBoxLogs.poll()) != null) {
            printLogToTextBox(text, null);
        }
    }

    public static boolean shouldShowOnlyDiscoveredEntities() {
        return showOnlyDiscoveredEntities;
    }

    public static boolean toggleShowOnlyDiscoveredEntities() {
        showOnlyDiscoveredEntities = !showOnlyDiscoveredEntities;
        return showOnlyDiscoveredEntities;
    }

    public static boolean isDemoMode() {
        return demoMode;
    }

    public static boolean toggleDemoMode() {
        demoMode = !demoMode;
        return demoMode;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static boolean toggleDebugMode() {
        debugMode = !debugMode;
        return debugMode;
    }
}
