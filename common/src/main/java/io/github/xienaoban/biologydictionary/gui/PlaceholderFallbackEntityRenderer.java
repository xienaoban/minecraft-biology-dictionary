package io.github.xienaoban.biologydictionary.gui;

import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Wraps entity rendering calls with try-catch. Once rendering fails for an entity,
 * all subsequent calls render a rotating-head ArmorStand placeholder instead,
 * avoiding repeated expensive exception throwing per frame.
 * <p>
 * This method is primarily for 1.20.1 & 1.21.1.
 * In 1.21.11, the rendering task is simply added to a list here and rendered later.
 */
@ClientOnly
public class PlaceholderFallbackEntityRenderer {
    private final Entity entity;
    private ArmorStand placeholder;
    private ScreenRenderingContext.EntityRenderingCache cache;

    public PlaceholderFallbackEntityRenderer(Entity entity) {
        this.entity = entity;
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws != null && cws.hasRenderFailed(entity.getType())) {
            this.placeholder = EntityUtils.create(EntityTypes.ARMOR_STAND, entity.level());
        } else {
            this.placeholder = null;
        }
        this.cache = new ScreenRenderingContext.EntityRenderingCache();
    }

    private void renderEntityOrArmorStand(Consumer<Entity> renderer) {
        if (placeholder == null) {
            try {
                renderer.accept(entity);
                return;
            } catch (Throwable e) {
                LOGGER.error("Error in rendering entity \"{}\" on screen", EntityUtils.getEntityTypeIdName(entity), e);
                ClientWorldSession cws = ClientWorldSession.get();
                if (cws != null) {
                    cws.markRenderFailed(entity.getType());
                }
                placeholder = EntityUtils.create(EntityTypes.ARMOR_STAND, entity.level());
                cache = new ScreenRenderingContext.EntityRenderingCache();
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
        renderEntityOrArmorStand(e -> ctx.renderEntityBottomed(
                e, cache, left, top, right, bottom, rotateX, rotateY, forceScale));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY) {
        renderEntityOrArmorStand(e -> ctx.renderEntityCentered(e, cache, left, top, right, bottom, rotateX, rotateY));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        renderEntityOrArmorStand(e -> ctx.renderEntityCentered(
                e, cache, left, top, right, bottom, rotateX, rotateY, forceScale));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, int silhouetteColor) {
        renderEntityOrArmorStand(e -> ctx.renderEntityCentered(
                e, cache, left, top, right, bottom, rotateX, rotateY, silhouetteColor));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale, int silhouetteColor) {
        renderEntityOrArmorStand(e -> ctx.renderEntityCentered(
                e, cache, left, top, right, bottom, rotateX, rotateY, forceScale, silhouetteColor));
    }
}
