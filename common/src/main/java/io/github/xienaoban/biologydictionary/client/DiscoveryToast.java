package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@ClientOnly
public class DiscoveryToast implements Toast {
    private static final Identifier BACKGROUND_SPRITE = IdentifierUtils.bd("toast/discovery_toast");
    private static final int DISPLAY_TIME = 7000;
    private final Component entityName;
    private final ItemStack eggStack;
    private final Component title;
    private final long createdAt;
    private Visibility wantedVisibility = Visibility.HIDE;

    /**
     * The local player discovered a new entity type.
     */
    public static DiscoveryToast bySelf(EntityType<?> entityType) {
        return new DiscoveryToast(entityType, Component.empty());
    }

    /**
     * Another player discovered an entity type that is shared globally.
     */
    public static DiscoveryToast byGlobal(EntityType<?> entityType, String discovererName) {
        return new DiscoveryToast(entityType,
                TextUtils.translate(Lang.TEXT_ENTITY_DISCOVERED_GLOBAL, discovererName));
    }

    /**
     * Another player actively shared a discovery.
     */
    public static DiscoveryToast byOther(EntityType<?> entityType, String sharerName) {
        return new DiscoveryToast(entityType,
                TextUtils.translate(Lang.TEXT_ENTITY_DISCOVERED_SHARED_BY, sharerName));
    }

    /**
     * @param nameSuffix appended to the entity name, e.g. the sharer or global-share annotation
     */
    private DiscoveryToast(EntityType<?> entityType, Component nameSuffix) {
        this.entityName = TextUtils.concat(EntityUtils.getEntityTypeNameText(entityType), nameSuffix);
        Item spawnEgg = EntityUtils.getSpawnEggItem(entityType);
        this.eggStack = spawnEgg == null ? null : spawnEgg.getDefaultInstance();
        this.title = TextUtils.translate(Lang.TEXT_NEW_ENTITY_DISCOVERED).withStyle(ChatFormatting.YELLOW);
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(ToastManager toastManager, long gameTime) {
        long elapsed = System.currentTimeMillis() - createdAt;
        wantedVisibility = elapsed >= DISPLAY_TIME * toastManager.getNotificationDisplayTimeMultiplier()
            ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, Font font, long gameTime) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, width(), height());
        if (eggStack != null) {
            guiGraphics.fakeItem(eggStack, 8, 8);
        }
        guiGraphics.text(font, title, 30, 7, -256, false);
        guiGraphics.text(font, entityName, 30, 18, -1, false);
    }
}
