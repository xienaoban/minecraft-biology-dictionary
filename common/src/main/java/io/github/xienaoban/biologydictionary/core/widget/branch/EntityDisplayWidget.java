package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.EntityDisplay;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * A widget that displays the target entity. <br/>
 * The entity can be rotated according to the mouse.
 */
@ClientOnly
public final class EntityDisplayWidget extends EntityPropertyWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntityDisplayWidget::new;

    private static RC calculateRowsAndColumns(Entity entity) {
        AABB box = entity.getBoundingBox();
        double x = box.getXsize(), y = box.getYsize();
        if (x > y) { return new RC(3, 6); }
        return new RC(5, 4);
    }

    private static EntityDisplay createDisplay(EntityProperties<Entity> properties) {
        Entity entity = properties.entity();
        EntityDictionaryEntry entry = WorldSession.get().getEntityManager().getEntityEntry(entity.getType());
        return new EntityDisplay(entry, entity);
    }

    private final EntityDisplay display;

    private int leftClickCount;

    public EntityDisplayWidget(EntityProperties<Entity> properties) {
        this(properties, createDisplay(properties));
    }

    private EntityDisplayWidget(EntityProperties<Entity> properties, EntityDisplay display) {
        super(properties, calculateRowsAndColumns(display.getModel()));
        this.display = display;
        p().setEntityDisplay(display);
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        if (ticks % ClientUtils.getClientTickCountPerSecond() == 15 && p().isNotInNoUpdateCooldown()) {
            display.updateNbtFrom(e());
        }
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        display.renderEntityCentered(ctx,
                box.getLeft() + 1, box.getTop() + 1, box.getRight() - 1, box.getBottom() - 1,
                (float) Math.atan(ctx.getMouseX() / 40F) / 10,
                (float) Math.atan(ctx.getMouseY() / 40F) / 20);

        ctx.renderRectangle(0x06794500, ctx.getZ(),
                box.getLeft() + 1, box.getTop() + 1, box.getRight() - 1, box.getBottom() - 1);
        int color = 0x10794500;
        ctx.renderRectangle(color, ctx.getZ(),
                box.getLeft(), box.getTop() + 1, box.getLeft() + 1, box.getBottom() - 1);
        ctx.renderRectangle(color, ctx.getZ(),
                box.getRight() - 1, box.getTop() + 1, box.getRight(), box.getBottom() - 1);
        ctx.renderRectangle(color, ctx.getZ(),
                box.getLeft() + 1, box.getTop(), box.getRight() - 1, box.getTop() + 1);
        ctx.renderRectangle(color, ctx.getZ(),
                box.getLeft() + 1, box.getBottom() - 1, box.getRight() - 1, box.getBottom());
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        Entity entity = e();
        if (isMouseLeft(button)) {
            if (entity instanceof LivingEntity livingEntity) {
                leftClickCount++;
                if (leftClickCount % 5 == 0) {
                    playDeathSound(livingEntity);
                } else {
                    playHurtSound(livingEntity);
                }
            }
        } else if (isMouseRight(button)) {
            if (entity instanceof Mob mob) {
                playAmbientSound(mob);
            }
        }
        return true;
    }

    private void playHurtSound(LivingEntity entity) {
        SoundEvent sound = EntityUtils.getHurtSound(entity);
        if (sound != null) {
            ClientUtils.playScreenSound(sound, 1.0F, 1.0F);
        }
    }

    private void playDeathSound(LivingEntity entity) {
        SoundEvent sound = EntityUtils.getDeathSound(entity);
        if (sound != null) {
            ClientUtils.playScreenSound(sound, 1.0F, 1.0F);
        }
    }

    private void playAmbientSound(Mob entity) {
        SoundEvent sound = EntityUtils.getAmbientSound(entity);
        if (sound != null) {
            ClientUtils.playScreenSound(sound, 1.0F, 1.0F);
        }
    }
}
