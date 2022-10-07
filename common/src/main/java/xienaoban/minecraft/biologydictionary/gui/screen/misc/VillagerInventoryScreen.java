package xienaoban.minecraft.biologydictionary.gui.screen.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import xienaoban.minecraft.biologydictionary.util.Resources;

/**
 * @see HorseInventoryScreen
 */
@Environment(EnvType.CLIENT)
public class VillagerInventoryScreen extends AbstractContainerScreen<VillagerInventoryMenu> {
    private final AbstractVillager villager;

    public VillagerInventoryScreen(VillagerInventoryMenu abstractContainerMenu,
                                   Inventory inventory, AbstractVillager villager) {
        super(abstractContainerMenu, inventory, villager.getDisplayName());
        this.villager = villager;
        this.passEvents = false;
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float mouseX, int mouseY, int delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, Resources.HORSE_INVENTORY_LOCATION);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
        this.blit(poseStack, k + 79, l + 17 + 18, 0, this.imageHeight, 4 * 18, 2 * 18);
        InventoryScreen.renderEntityInInventory(k + 51, l + 60, 17, k + 51 - mouseX, l + 75 - 50 - mouseY, this.villager);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
}