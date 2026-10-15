package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.gui.EntityDisplay;
import io.github.xienaoban.biologydictionary.gui.EntityDisplayRotation;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An entity card of the selection mode: clicking it adds or removes the entity type of
 * the given selection.
 */
@ClientOnly
public final class EntitySelectionCardWidget extends EntityCardWidget {
    private final Set<EntityType<?>> selection;

    /**
     * Create the cards of the given entries, skipping the ones hidden by the
     * "only discovered entities" filter.
     */
    public static List<Widget> createWidgets(List<EntityDictionaryEntry> entries,
                                             EntityDisplayRotation rotation, Set<EntityType<?>> selection) {
        ClientLevel level = ClientUtils.getClientLevel();
        List<Widget> widgets = new ArrayList<>();
        for (EntityDictionaryEntry entry : entries) {
            if (!EntityCardWidget.isShown(entry)) { continue; }
            widgets.add(new EntitySelectionCardWidget(
                    entry, new EntityDisplay(entry, level), rotation, selection));
        }
        return widgets;
    }

    public EntitySelectionCardWidget(EntityDictionaryEntry entry, EntityDisplay display,
                                     EntityDisplayRotation rotation, Set<EntityType<?>> selection) {
        super(entry, display, rotation);
        this.selection = selection;
    }

    @Override
    protected boolean shouldRenderDetail() {
        return true;
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
        boolean selected = selection.add(entry.getType());
        if (!selected) {
            selection.remove(entry.getType());
        }
        ClientUtils.playScreenSound(
                selected ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                1.0F, 0.8F);
        return true;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        if (selection.contains(entry.getType())) {
            ScreenElementBox box = getBox();
            ctx.renderRectangle(0x77794500, 1F, ctx.getZ(),
                    box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        ScreenElementBox box = getBox();
        ctx.renderRectangle(0x22794500, ctx.getZ(),
                box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
        return true;
    }
}
