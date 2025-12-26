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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

/**
 * @see net.minecraft.client.gui.screens.inventory.HorseInventoryScreen
 */
@Environment(EnvType.CLIENT)
public class InventoryStealingScreen extends AbstractContainerScreen<InventoryStealingMenu> {
    private final LivingEntity entity;
    private final int containerSize;
    private float xMouse;
    private float yMouse;

    public InventoryStealingScreen(InventoryStealingMenu menu, Inventory inventory, LivingEntity entity) {
        super(menu, inventory, Component.translatable(Lang.SCREEN_STEALING));
        this.imageWidth = 234;
        this.imageHeight = 194;
        this.entity = entity;
        this.containerSize = Math.min(menu.container.getContainerSize(), InventoryStealingMenu.MAX_SLOTS);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Textures.STEALING_INVENTORY.location(), left, top, 0, 0, this.imageWidth, this.imageHeight, (int) Textures.STEALING_INVENTORY.width(), (int) Textures.STEALING_INVENTORY.height());

        final int wh = 18;
        final int mod = InventoryStealingMenu.calculateMod(this.containerSize);
        for (int i = 0; i < this.containerSize; i++) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Textures.STEALING_INVENTORY.location(), left + 65 + (i % mod) * wh, top + 17 + (i / mod) * wh, (int) Textures.STEALING_INVENTORY.width() - wh, (int) Textures.STEALING_INVENTORY.height() - wh, wh, wh, (int) Textures.STEALING_INVENTORY.width(), (int) Textures.STEALING_INVENTORY.height());
        }

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, left + 8, top + 18, left + 60, top + 88, 17, 0.25F, this.xMouse, this.yMouse, this.entity);
    }

    protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x88FFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.xMouse = i;
        this.yMouse = j;
        super.render(guiGraphics, i, j, f);
        this.renderTooltip(guiGraphics, i, j);
    }
}
