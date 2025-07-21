package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DebugScreen extends Screen {
    private final List<LivingEntity> entities = new ArrayList<>();

    public DebugScreen() {
        super(Component.literal("Debug"));
        Minecraft client = McClientUtils.getClient();
        entities.add(EntityType.COW.create(client.level, null));
        entities.add(EntityType.HORSE.create(client.level, null));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        super.render(guiGraphics, mouseX, mouseY, f);

        Minecraft client = McClientUtils.getClient();
        ModelPart flag = client.getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG).getChild("flag");
        DyeColor dyeColor = DyeColor.ORANGE;
        BannerPatternLayers resultBannerPatterns = BannerPatternLayers.EMPTY;

        int left;
        int top;
        left = width / 2 - 100;
        top = height / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, left + 26, top + 8, left + 75, top + 78, 30, 0.0625F, mouseX, mouseY, client.player);
        // guiGraphics.submitBannerPatternRenderState(flag, dyeColor, resultBannerPatterns, left, top, left + 20, top + 40);
        left = width / 2;
        top = height / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, left + 26, top + 8, left + 75, top + 78, 30, 0.0625F, mouseX, mouseY, entities.get(0));
        // guiGraphics.submitBannerPatternRenderState(flag, dyeColor, resultBannerPatterns, left, top, left + 20, top + 40);
        left = width / 2 + 100;
        top = height / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, left + 26, top + 8, left + 75, top + 78, 30, 0.0625F, mouseX, mouseY, entities.get(1));
        // guiGraphics.submitBannerPatternRenderState(flag, dyeColor, resultBannerPatterns, left, top, left + 20, top + 40);
    }
}
