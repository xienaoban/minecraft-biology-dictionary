package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DebugScreen extends Screen {
    private final List<LivingEntity> entities = new ArrayList<>();

    public DebugScreen() {
        super(TextUtils.literal("Debug"));
        Minecraft client = ClientUtils.getClient();
        ClientLevel level = ClientUtils.getClientLevel(client);

        entities.add(EntityUtils.create(EntityType.COW, level));
        entities.add(EntityUtils.create(EntityType.HORSE, level));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        super.render(guiGraphics, mouseX, mouseY, f);
        ScreenRenderingContext ctx = new ScreenRenderingContext(this);
        ctx.update(guiGraphics, 1F, 1F, mouseX, mouseY, f);

        Minecraft client = ClientUtils.getClient();

        int left;
        int top;
        left = width / 2 - 100;
        top = height / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, left + 26, top + 8, left + 75, top + 78, 30, 0.0625F, mouseX, mouseY, ClientUtils.getClientPlayer(client));
        // guiGraphics.submitBannerPatternRenderState(flag, dyeColor, resultBannerPatterns, left, top, left + 20, top + 40);
        left = width / 2;
        top = height / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, left + 26, top + 8, left + 75, top + 78, 30, 0.0625F, mouseX, mouseY, entities.get(0));
        // guiGraphics.submitBannerPatternRenderState(flag, dyeColor, resultBannerPatterns, left, top, left + 20, top + 40);
        left = width / 2 + 100;
        top = height / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, left + 26, top + 8, left + 75, top + 78, 30, 0.0625F, mouseX, mouseY, entities.get(1));
        // guiGraphics.submitBannerPatternRenderState(flag, dyeColor, resultBannerPatterns, left, top, left + 20, top + 40);

        LivingEntity e = entities.get(0);
        for (int i = 0; i < 40; ++i) {
            left = width / 2 -200 + i * 10;
            top = height / 2;
            ctx.renderEntityCentered(e, left, top, left + 20, top + 40, mouseX, mouseY);
        }
    }
}
