package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

/**
 * @see net.minecraft.client.gui.screens.inventory.HorseInventoryScreen
 */
@Environment(EnvType.CLIENT)
public class InventoryStealingScreen extends AbstractContainerScreen<InventoryStealingMenu> {
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    private static final Identifier CHEST_SLOTS_SPRITE = Identifier.withDefaultNamespace("container/horse/chest_slots");

    private final LivingEntity entity;
    private final int containerSize;
    private float xMouse;
    private float yMouse;

    public InventoryStealingScreen(InventoryStealingMenu menu, Inventory inventory, LivingEntity entity) {
        super(menu, inventory, Component.translatable(Lang.SCREEN_STEALING));
        this.imageWidth = 234;
        this.imageHeight = 194;
        this.entity = entity;
        this.containerSize = menu.container.getContainerSize();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Textures.STEALING_INVENTORY.location(), left, top, 0.0F, 0.0F, this.imageWidth, this.imageHeight, (int) Textures.STEALING_INVENTORY.width(), (int) Textures.STEALING_INVENTORY.height());

        if (this.containerSize % 2 == 0 && this.containerSize <= 10) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHEST_SLOTS_SPRITE, 90, 54, 0, 0, left + 79, top + 17, this.containerSize / 2 * 18, 18 * 2);
        } else {
            int c = this.containerSize / 3;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHEST_SLOTS_SPRITE, 90, 54, 0, 0, left + 79, top + 17, c * 18, 3 * 18);
            if (this.containerSize % 3 != 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHEST_SLOTS_SPRITE, 90, 54, 0, 0, left + 79, top + 17, (c + 1) * 18, (this.containerSize % 3) * 18);
            }
        }

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, left + 8, top + 18, left + 60, top + 70, 17, 0.25F, this.xMouse, this.yMouse, this.entity);
    }

    private void drawSlot(GuiGraphics guiGraphics, int i, int j) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, i, j, 18, 18);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.xMouse = i;
        this.yMouse = j;
        super.render(guiGraphics, i, j, f);
        this.renderTooltip(guiGraphics, i, j);
    }
}
