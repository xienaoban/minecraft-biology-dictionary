package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jspecify.annotations.Nullable;

@ClientOnly
public class DiscoveryToast implements Toast {
    private static final Identifier BACKGROUND_SPRITE = Identifier.fromNamespaceAndPath("biologydictionary", "toast/discovery_toast");
    private static final int DISPLAY_TIME = 7000;
    private final Component entityName;
    @Nullable
    private final ItemStack eggStack;
    private final long createdAt;
    private Visibility wantedVisibility = Visibility.HIDE;

    public DiscoveryToast(EntityType<?> entityType) {
        this.entityName = EntityUtils.getEntityTypeNameText(entityType);
        this.eggStack = SpawnEggItem.byId(entityType)
                .map(itemHolder -> itemHolder.value().getDefaultInstance())
                .orElse(null);
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
        MutableComponent title = TextUtils.translate(Lang.TEXT_NEW_ENTITY_DISCOVERED)
            .withStyle(ChatFormatting.YELLOW);
        guiGraphics.text(font, title, 30, 7, -256, false);
        guiGraphics.text(font, entityName, 30, 18, -1, false);
    }
}
