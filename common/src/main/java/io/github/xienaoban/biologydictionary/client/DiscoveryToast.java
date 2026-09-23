package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.ItemUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

@ClientOnly
public class DiscoveryToast implements Toast {
    private static final ResourceLocation BACKGROUND_TEXTURE = IdentifierUtils.bd("textures/gui/sprites/toast/discovery_toast.png");
    private static final int DISPLAY_TIME = 7000;
    private final Component entityName;
    private final ItemStack eggStack;
    private final Component title;
    private final long createdAt;

    /**
     * The local player discovered a new entity type.
     */
    public static DiscoveryToast bySelf(EntityType<?> entityType) {
        return new DiscoveryToast(entityType, TextUtils.empty());
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
        SpawnEggItem egg = ItemUtils.getSpawnEggItem(entityType);
        this.eggStack = egg == null ? null : egg.getDefaultInstance();
        this.title = TextUtils.translate(Lang.TEXT_NEW_ENTITY_DISCOVERED).withStyle(ChatFormatting.YELLOW);
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l) {
        guiGraphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0, width(), height(), width(), height());
        if (eggStack != null) {
            guiGraphics.renderFakeItem(eggStack, 8, 8);
        }
        Font font = ClientUtils.getClient().font;
        guiGraphics.drawString(font, title, 30, 7, -256, false);
        guiGraphics.drawString(font, entityName, 30, 18, -1, false);
        long elapsed = System.currentTimeMillis() - createdAt;
        if (elapsed >= DISPLAY_TIME * toastComponent.getNotificationDisplayTimeMultiplier()) {
            return Visibility.HIDE;
        }
        return Visibility.SHOW;
    }
}
