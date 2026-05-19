package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SpawnEggItem;

@Environment(EnvType.CLIENT)
public class DiscoveryToast implements Toast {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.tryParse("biologydictionary:textures/gui/discovery_toast.png");
    private static final int DISPLAY_TIME = 7000;
    private final Component entityName;
    private final ItemStack eggStack;
    private final long createdAt;

    public DiscoveryToast(EntityType<?> entityType) {
        this.entityName = EntityUtils.getEntityTypeNameText(entityType);
        SpawnEggItem egg = SpawnEggItem.byId(entityType);
        this.eggStack = egg != null ? egg.getDefaultInstance() : null;
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l) {
        guiGraphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0, width(), height(), width(), height());
        if (eggStack != null) {
            guiGraphics.renderFakeItem(eggStack, 8, 8);
        }
        MutableComponent title = TextUtils.translate(Lang.TEXT_NEW_ENTITY_DISCOVERED)
            .withStyle(ChatFormatting.YELLOW);
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, title, 30, 7, -256, false);
        guiGraphics.drawString(font, entityName, 30, 18, -1, false);
        long elapsed = System.currentTimeMillis() - createdAt;
        if (elapsed >= DISPLAY_TIME * toastComponent.getNotificationDisplayTimeMultiplier()) {
            return Visibility.HIDE;
        }
        return Visibility.SHOW;
    }
}
