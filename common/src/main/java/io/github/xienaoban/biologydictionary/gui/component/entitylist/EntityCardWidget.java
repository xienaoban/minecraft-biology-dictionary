package io.github.xienaoban.biologydictionary.gui.component.entitylist;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.gui.EntityDisplay;
import io.github.xienaoban.biologydictionary.gui.EntityDisplayRotation;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.FontUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;

/**
 * Base of the entity cards: an entity model with its name below it. Subclasses
 * decide whether the name may be shown and what the card does on click.
 */
@ClientOnly
public abstract class EntityCardWidget extends Widget {
    protected final EntityDictionaryEntry entry;
    private final Component name;
    private final boolean isDiscovered;

    private final EntityDisplay display;
    private final EntityDisplayRotation rotation;

    protected EntityCardWidget(EntityDictionaryEntry entry, EntityDisplay display, EntityDisplayRotation rotation) {
        super(2, 2);
        this.entry = entry;
        this.name = EntityUtils.getEntityTypeNameText(entry.getType());
        this.isDiscovered = ClientWorldSession.get().getDiscoveryCacheManager().isDiscovered(entry.getType());
        this.display = display;
        this.rotation = rotation;
    }

    /**
     * Whether the card of this entry is listed at all, honoring the in-memory
     * "only discovered entities" filter shared by every entity list.
     */
    public static boolean isShown(EntityDictionaryEntry entry) {
        if (!BiologyDictionaryClient.shouldShowOnlyDiscoveredEntities()) { return true; }
        return ClientWorldSession.get().getDiscoveryCacheManager().isDiscovered(entry.getType());
    }

    protected final boolean isDiscoveredOrCreative() {
        return isDiscovered || PlayerUtils.isCreative(ClientUtils.getClientPlayer());
    }

    protected abstract boolean shouldRenderDetail();

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        display.tick();
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);

        int silhouetteColor = isDiscoveredOrCreative() ? 0 : Colors.UNDISCOVERED_ENTITY_COLOR;
        ScreenElementBox box = getBox();
        display.renderEntityCentered(ctx,
                box.getLeft(), box.getTop(), box.getRight(), box.getBottom() - 6,
                rotation.getRotateX(), rotation.getRotateY(),
                silhouetteColor);

        Component text = shouldRenderDetail()
                ? FontUtils.truncateByWidth(name, getBox().getWidth() + 2, 0.5F)
                : TextUtils.literal("??");
        ctx.renderCenteredText(text, Colors.BROWN, 0.5F, ctx.getZ(),
                (box.getLeft() + box.getRight()) / 2, box.getBottom() - 5);
    }
}
