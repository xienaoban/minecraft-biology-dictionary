package io.github.xienaoban.biologydictionary.gui;

import io.github.xienaoban.biologydictionary.client.ClientMannequin;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

@ClientOnly
public final class EntityDisplay {
    private final EntityType<?> type;
    private Entity model;
    private boolean placeholder;

    public EntityDisplay(EntityDictionaryEntry entry, Level level) {
        this(entry, level, null);
    }

    public EntityDisplay(EntityDictionaryEntry entry, Entity target) {
        this(entry, target.level(), target);
    }

    private EntityDisplay(EntityDictionaryEntry entry, Level level, Entity target) {
        this.type = entry.getType();
        if (entry.isInstanceCreationFailed() || hasRenderFailed()) {
            if (this.type == EntityType.PLAYER) {
                // not placeholder
                this.model = createPlayerPlaceholder(level, target);
                return;
            }
            this.placeholder = true;
            this.model = createPlaceholder(level);
            return;
        }
        try {
            this.model = EntityUtils.create(type, level);
            if (target == null) {
                EntityUtils.setupForDisplay(model);
            } else {
                updateNbtFrom(target);
            }
        } catch (Throwable e) {
            entry.markInstanceCreationFailed(e);
            this.placeholder = true;
            this.model = createPlaceholder(level);
        }
    }

    public Entity getModel() { return model; }
    public boolean isPlaceholder() { return placeholder; }

    public void updateNbtFrom(Entity target) {
        if (placeholder || target == null) { return; }
        updateNbt(EntityUtils.getNbtToDisplay(target));
    }

    public void updateNbt(CompoundTag nbt) {
        if (placeholder) { return; }
        EntityUtils.setNbt(model, nbt);
        EntityUtils.setupForDisplay(model);
    }

    private boolean hasRenderFailed() {
        ClientWorldSession session = ClientWorldSession.get();
        return session != null && session.hasRenderFailed(type);
    }

    private ArmorStand createPlaceholder(Level level) {
        ArmorStand placeholder = EntityUtils.create(EntityType.ARMOR_STAND, level);
        EntityUtils.setupForDisplay(placeholder);
        placeholder.setShowArms(true);
        return placeholder;
    }

    /**
     * The vanilla {@code net.minecraft.client.entity.ClientMannequin} does not exist in 1.21.1,
     * so the mod's {@link ClientMannequin} is used; its profile is set via the constructor
     * instead of the entity data sync NBT round-trip used on newer versions.
     */
    private ClientMannequin createPlayerPlaceholder(Level level, Entity target) {
        ClientMannequin m = new ClientMannequin(level, target instanceof Player player ? player.getGameProfile() : null);
        EntityUtils.setupForDisplay(m);
        m.tick();
        return m;
    }

    private void replaceWithPlaceholder() {
        model = createPlaceholder(model.level());
    }

    private void render(Consumer<Entity> renderer) {
        if (!placeholder) {
            try {
                renderer.accept(model);
                return;
            } catch (Throwable e) {
                LOGGER.error("Error in rendering entity \"{}\" on screen", EntityUtils.getEntityTypeIdName(type), e);
                ClientWorldSession session = ClientWorldSession.get();
                if (session != null) {
                    session.markRenderFailed(type);
                }
                placeholder = true;
                replaceWithPlaceholder();
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
        ((ArmorStand) model).setItemSlot(EquipmentSlot.HEAD, new ItemStack(head));
        renderer.accept(model);
    }

    public void renderEntityBottomed(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        render(entity -> ctx.renderEntityBottomed(
                entity, left, top, right, bottom, rotateX, rotateY, forceScale));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY) {
        render(entity -> ctx.renderEntityCentered(entity, left, top, right, bottom, rotateX, rotateY));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale) {
        render(entity -> ctx.renderEntityCentered(
                entity, left, top, right, bottom, rotateX, rotateY, forceScale));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, int silhouetteColor) {
        render(entity -> ctx.renderEntityCentered(
                entity, left, top, right, bottom, rotateX, rotateY, silhouetteColor));
    }

    public void renderEntityCentered(ScreenRenderingContext ctx, float left, float top, float right, float bottom,
                                     float rotateX, float rotateY, float forceScale, int silhouetteColor) {
        render(entity -> ctx.renderEntityCentered(
                entity, left, top, right, bottom, rotateX, rotateY, forceScale, silhouetteColor));
    }
}
