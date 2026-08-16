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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;

/**
 * @see net.minecraft.client.gui.screens.inventory.HorseInventoryScreen
 */
@ClientOnly
public class InventoryStealingScreen extends AbstractContainerScreen<InventoryStealingMenu> {
    private final LivingEntity entity;
    private final int containerSize;
    private float xMouse;
    private float yMouse;

    private int tickCounter;
    private boolean hasDetected;
    private boolean warnedEquipmentSwallow;

    private Component screenMessage = null;
    private long screenMessageEndTime = -1;

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
        extractScreenMessage(guiGraphics);
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

    @Override
    protected void slotClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput) {
        if (slotId >= 0 && slotId < InventoryStealingMenu.EQUIPMENT_SLOTS) {
            Player player = getMenu().getPlayer();
            if (!getMenu().canModifyEquipment(player)) {
                sendScreenWarning(TextUtils.translate(Lang.TEXT_SERVER_FORBIDS_EQUIPMENT_STEALING));
                return;
            }
            // First click on an equipment slot warns about the swallowing risk and (in survival) is intercepted;
            // creative only warns without blocking.
            if (!warnedEquipmentSwallow) {
                warnedEquipmentSwallow = true;
                sendScreenWarning(TextUtils.translate(Lang.TEXT_EQUIPMENT_MAY_BE_SWALLOWED));
                if (!PlayerUtils.isCreative(player)) {
                    return;
                }
            }
        }
        super.slotClicked(slot, slotId, buttonNum, containerInput);
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

    /**
     * This screen is not an element screen, so it implements its own centered message
     * instead of {@code BiologyDictionaryClient.sendCenteredWarning}, which would fall back
     * to the vanilla overlay rendered behind screens.
     */
    private void sendScreenWarning(Component text) {
        this.screenMessage = text.copy().withStyle(ChatFormatting.YELLOW);
        this.screenMessageEndTime = System.currentTimeMillis() + 7000;
    }

    private void extractScreenMessage(GuiGraphicsExtractor guiGraphics) {
        if (screenMessage == null) { return; }
        long currTime = System.currentTimeMillis();
        if (currTime > screenMessageEndTime) {
            screenMessage = null;
            return;
        }
        long beginFade = screenMessageEndTime - 2000;
        int alpha = currTime <= beginFade ? 0xFF : (int) (0xFF * (screenMessageEndTime - currTime) / 2000);
        int textWidth = font.width(screenMessage);
        // The pose is already translated to the GUI origin in extractLabels, so use image-relative coordinates.
        int x = imageWidth / 2;
        int y = imageHeight / 2 + 100;
        guiGraphics.centeredText(font, screenMessage, x, y, 0xFFFFFF | (alpha << 24));
    }
}
