package xienaoban.minecraft.biologydictionary.gui.screen.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AbstractVillagerInventoryScreen extends AbstractContainerScreen<AbstractVillagerInventoryScreenHandler> {

    public AbstractVillagerInventoryScreen(AbstractVillagerInventoryScreenHandler abstractContainerMenu,
                                           Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {

    }
}