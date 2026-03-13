package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * @see net.minecraft.client.gui.screens.inventory.HorseInventoryScreen
 */
@Environment(EnvType.CLIENT)
public class InventoryStealingScreen extends AbstractContainerScreen<InventoryStealingMenu> {
    private final LivingEntity entity;
    private final int containerSize;
    private float xMouse;
    private float yMouse;

    private int tickCounter;
    private boolean hasDetected;

    public InventoryStealingScreen(InventoryStealingMenu menu, Inventory inventory, LivingEntity entity) {
        super(menu, inventory, TextUtils.translate(Lang.SCREEN_STEALING));
        this.imageWidth = 234;
        this.imageHeight = 194;
        this.entity = entity;
        this.containerSize = Math.min(menu.container.getContainerSize(), InventoryStealingMenu.MAX_SLOTS);

        this.tickCounter = 0;
        this.hasDetected = false;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(Textures.STEALING_INVENTORY.location(), left, top, 0, 0, this.imageWidth, this.imageHeight, (int) Textures.STEALING_INVENTORY.width(), (int) Textures.STEALING_INVENTORY.height());

        final int wh = 18;
        final int mod = InventoryStealingMenu.calculateMod(this.containerSize);
        for (int i = 0; i < this.containerSize; i++) {
            guiGraphics.blit(Textures.STEALING_INVENTORY.location(), left + 65 + (i % mod) * wh, top + 17 + (i / mod) * wh, (int) Textures.STEALING_INVENTORY.width() - wh, (int) Textures.STEALING_INVENTORY.height() - wh, wh, wh, (int) Textures.STEALING_INVENTORY.width(), (int) Textures.STEALING_INVENTORY.height());
        }

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, left + 34, top + 53, 30,
                (left + 34) - this.xMouse, (top + 53 - 50) - this.yMouse, this.entity);
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

    @Override
    protected void containerTick() {
        // Only check in survival mode and not already detected.
        if (hasDetected) {
            return;
        }

        tickCounter++;
        if (tickCounter % 10 == 0) {
            if (isPlayerCaughtByEntity(entity, getMenu().getPlayer())) {
                hasDetected = true;
                // Close screen and show message on client side immediately.
                ClientUtils.getClientPlayer().closeContainer();
                BiologyDictionaryClient.sendCenteredWarning(TextUtils.translate(Lang.TEXT_STEALING_DETECTED));
                // Send packet to server to deal damage.
                ClientNetManager.sendStealingDetected(entity);
            }
        }
    }

    /**
     * Static method to check if an entity is looking at a player.
     * This can be reused in other places (e.g., before opening the stealing screen).
     *
     * @param entity the entity to check
     * @param player the player to check against
     * @return true if the entity can see and is looking at the player
     */
    public static boolean isEntityDetectedBy(LivingEntity entity, Player player) {
        // First check line of sight.
        if (!entity.hasLineOfSight(player)) {
            return false;
        }

        // Then check if the entity is looking towards the player horizontally.
        Vec3 entityEyePos = entity.getEyePosition(1F);
        Vec3 playerEyePos = player.getEyePosition(1F);

        // Use getViewVector with partial tick to get the interpolated look direction.
        // This matches the entity's visually rendered rotation, not the server-synced value.
        // Do not use getLookAngle as it is the sight from server.
        // Vec3 entityLookDir = entity.getLookAngle().normalize();
        float partialTick = ClientUtils.getPartialTick();
        Vec3 entityLookDir = entity.getViewVector(partialTick).normalize();
        Vec3 toPlayer = playerEyePos.subtract(entityEyePos).normalize();

        // Project both vectors onto the horizontal plane (y=0).
        Vec3 entityLookHorizontal = new Vec3(entityLookDir.x, 0D, entityLookDir.z).normalize();
        Vec3 toPlayerHorizontal = new Vec3(toPlayer.x, 0D, toPlayer.z).normalize();

        // Calculate the horizontal angle between entity's look direction and direction to player.
        double dotProduct = entityLookHorizontal.dot(toPlayerHorizontal);
        double angle = Math.acos(Mth.clamp(dotProduct, -1D, 1D));
        double angleDegrees = Math.toDegrees(angle);

        // If the horizontal angle is within 66 degrees, consider the entity is looking at the player.
        return angleDegrees < 66D;
    }

    /**
     * Check if the player is caught by the entity (only in survival mode).
     * This wraps {@link #isEntityDetectedBy(LivingEntity, Player)} with a creative mode check.
     *
     * @param entity the entity to check
     * @param player the player to check against
     * @return true if the player is in survival mode and the entity detected them
     */
    public static boolean isPlayerCaughtByEntity(LivingEntity entity, Player player) {
        if (PlayerUtils.isCreative(player)) {
            return false;
        }
        return isEntityDetectedBy(entity, player);
    }
}
