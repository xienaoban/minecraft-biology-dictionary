package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.KeyMappings;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@ClientOnly
public class InventoryStealingScreen extends AbstractContainerScreen<InventoryStealingMenu> {
    private final LivingEntity entity;
    private final int containerSize;
    private float xMouse;
    private float yMouse;

    private int tickCounter;
    private boolean hasDetected;

    public InventoryStealingScreen(InventoryStealingMenu menu, Inventory inventory, LivingEntity entity) {
        super(menu, inventory, TextUtils.translate(Lang.SCREEN_STEALING), 234, 194);
        this.entity = entity;
        this.containerSize = Math.min(menu.container.getContainerSize(), InventoryStealingMenu.MAX_SLOTS);
        this.tickCounter = 0;
        this.hasDetected = false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        super.extractRenderState(guiGraphics, mouseX, mouseY, tickDelta);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        super.extractBackground(guiGraphics, mouseX, mouseY, tickDelta);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Textures.STEALING_INVENTORY.location(), leftPos, topPos,
                0, 0, imageWidth, imageHeight, (int) Textures.STEALING_INVENTORY.width(),
                (int) Textures.STEALING_INVENTORY.height());

        final int wh = 18;
        final int mod = InventoryStealingMenu.calculateMod(containerSize);
        for (int i = 0; i < containerSize; i++) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Textures.STEALING_INVENTORY.location(),
                    leftPos + 65 + (i % mod) * wh, topPos + 17 + (i / mod) * wh,
                    (int) Textures.STEALING_INVENTORY.width() - wh,
                    (int) Textures.STEALING_INVENTORY.height() - wh, wh, wh,
                    (int) Textures.STEALING_INVENTORY.width(), (int) Textures.STEALING_INVENTORY.height());
        }

        InventoryScreen.extractEntityInInventoryFollowsMouse(guiGraphics, leftPos + 8, topPos + 18,
                leftPos + 60, topPos + 88, 17, 0.25F, xMouse, yMouse, entity);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.text(font, title, titleLabelX, titleLabelY, 0x88FFFFFF, false);
    }

    @Override
    protected void containerTick() {
        if (hasDetected) {
            return;
        }

        tickCounter++;
        if (tickCounter % 10 == 0) {
            if (isPlayerCaughtByEntity(entity, getMenu().getPlayer())) {
                hasDetected = true;
                ClientUtils.getClientPlayer().closeContainer();
                BiologyDictionaryClient.sendCenteredWarning(TextUtils.translate(Lang.TEXT_STEALING_DETECTED));
                ClientNetManager.sendStealingDetected(entity);
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (KeyMappings.OPEN_HANDBOOK.matches(keyEvent)
                || ClientUtils.getClient().options.keyInventory.matches(keyEvent)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public static boolean isEntityDetectedBy(LivingEntity entity, Player player) {
        if (!entity.hasLineOfSight(player)) {
            return false;
        }

        Vec3 entityEyePos = entity.getEyePosition(1F);
        Vec3 playerEyePos = player.getEyePosition(1F);

        float partialTick = ClientUtils.getPartialTick();
        Vec3 entityLookDir = entity.getViewVector(partialTick).normalize();
        Vec3 toPlayer = playerEyePos.subtract(entityEyePos).normalize();

        Vec3 entityLookHorizontal = new Vec3(entityLookDir.x, 0D, entityLookDir.z).normalize();
        Vec3 toPlayerHorizontal = new Vec3(toPlayer.x, 0D, toPlayer.z).normalize();

        double dotProduct = entityLookHorizontal.dot(toPlayerHorizontal);
        double angle = Math.acos(Mth.clamp(dotProduct, -1D, 1D));
        double angleDegrees = Math.toDegrees(angle);

        return angleDegrees < 66D;
    }

    public static boolean isPlayerCaughtByEntity(LivingEntity entity, Player player) {
        if (PlayerUtils.isCreative(player)) {
            return false;
        }
        return isEntityDetectedBy(entity, player);
    }
}
