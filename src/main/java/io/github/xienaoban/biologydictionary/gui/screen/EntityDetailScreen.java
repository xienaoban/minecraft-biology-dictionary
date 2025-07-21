package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.EntityPropertyWidgets;
import io.github.xienaoban.biologydictionary.core.widget.TurnPageTriggerWidget;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EntityDetailScreen extends AbstractBiologyDictionaryScreen {
    private static final int SYNC_PROPERTIES_INTERVAL_TICK_CNT = (int) (McClientUtils.getClientTickCountPerSecond() * 1.5);
    private static final int CLOSE_SCREEN_DISTANCE = 10;

    private final Entity entity;
    private final EntityProperties<? extends Entity> properties;

    public EntityDetailScreen(EntityProperties<? extends Entity> properties) {
        super(properties.entity().getType().getDescription());
        this.entity = properties.entity();
        this.properties = properties;
        initEntityPropertyWidgets();
    }

    private void initEntityPropertyWidgets() {
        List<EntityPropertyWidget<?>> widgets = EntityPropertyWidgets.getWidgets(properties);
        boolean add = true;
        Page page = null;
        for (var widget : widgets) {
            if (widget instanceof TurnPageTriggerWidget) {
                add = true;
                continue;
            }
            if (add) {
                page = addPage();
                add = false;
            }
            if (!page.addWidget(widget)) {
                page = addPage();
                page.addWidget(widget);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (player.distanceToSqr(entity) > CLOSE_SCREEN_DISTANCE * CLOSE_SCREEN_DISTANCE) {
            HighlightManager.highlightEntity(entity, 4 * 20);
            McClientUtils.showClientCenteredMessage(Component.translatable(Lang.TEXT_TARGET_ENTITY_TOO_FAR).withStyle(ChatFormatting.YELLOW));
            onClose();
        }

        properties.tickNoUpdateCooldown();
        // Always 20 ticks per second. Not affected by "/tick rate" or "/gamerule randomTickSpeed".
        if (getTicks() % SYNC_PROPERTIES_INTERVAL_TICK_CNT == 0) {
            syncEntityProperties();
        }
    }

    private void syncEntityProperties() {
        ClientNetManager.requestEntityData(entity);
    }
}
