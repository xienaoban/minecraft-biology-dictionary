package io.github.xienaoban.biologydictionary.gui;

import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.Math;
import java.util.function.Consumer;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Wraps entity rendering calls with try-catch. Once rendering fails for an entity,
 * all subsequent calls render a rotating-head ArmorStand placeholder instead,
 * avoiding repeated expensive exception throwing per frame.
 */
public class PlaceholderFallbackEntityRenderer {
    private final Entity entity;
    private ArmorStand placeholder;

    public PlaceholderFallbackEntityRenderer(Entity entity) {
        this.entity = entity;
        this.placeholder = null;
    }

    private void renderEntityOrArmorStand(Consumer<Entity> renderer) {
        if (placeholder == null) {
            try {
                renderer.accept(entity);
                return;
            } catch (Throwable e) {
                LOGGER.error("Error in rendering entity \"{}\" on screen", EntityUtils.getEntityTypeIdName(entity), e);
                placeholder = new ArmorStand(EntityType.ARMOR_STAND, entity.level());
            }
        }

        int chosen = (Math.abs((int) System.currentTimeMillis()) / 400) % 4;
        Item head = switch (chosen) {
            case 0 -> Items.CREEPER_HEAD;
            case 1 -> Items.ZOMBIE_HEAD;
            case 2 -> Items.SKELETON_SKULL;
            case 3 -> Items.WITHER_SKELETON_SKULL;
            default -> throw new AssertionError(chosen);
        };
        placeholder.setItemSlot(EquipmentSlot.HEAD, new ItemStack(head));
        renderer.accept(placeholder);
    }

    public void renderEntityBottomed(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        renderEntityOrArmorStand(e -> ctx.renderEntityBottomed(e, left, top, right, bottom, rotateX, rotateY, forceScale));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY) {
        renderEntityOrArmorStand(e -> ctx.renderEntityCentered(e, left, top, right, bottom, rotateX, rotateY));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        renderEntityOrArmorStand(e -> ctx.renderEntityCentered(e, left, top, right, bottom, rotateX, rotateY, forceScale));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, int silhouetteColor) {
        renderEntityOrArmorStand(e -> ctx.renderEntityCentered(e, left, top, right, bottom, rotateX, rotateY, silhouetteColor));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale, int silhouetteColor) {
        renderEntityOrArmorStand(e -> ctx.renderEntityCentered(e, left, top, right, bottom, rotateX, rotateY, forceScale, silhouetteColor));
    }
}
