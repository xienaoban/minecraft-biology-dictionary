package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.EntityPropertyWidgets;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.List;

@Environment(EnvType.CLIENT)
public class BdEntityDetailScreen extends AbstractBiologyDictionaryScreen {
    private static final int SYNC_PROPERTIES_INTERVAL_TICK_CNT = (int) (ClientUtils.getClientTickCountPerSecond() * 1.5);
    private static final int CLOSE_SCREEN_DISTANCE = 10;

    private final Entity entity;
    private final EntityProperties<? extends Entity> properties;

    public BdEntityDetailScreen(EntityProperties<? extends Entity> properties) {
        super(properties.entity().getType().getDescription());
        this.entity = properties.entity();
        this.properties = properties;
        initBookmarks();
        initEntityPropertyWidgets();
    }

    private void initBookmarks() {
        addBookmarkFromLast(new OpenBdAboutScreenBookmark());
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initEntityPropertyWidgets() {
        List<EntityPropertyWidget<?>> widgets = EntityPropertyWidgets.getWidgets(properties);
        addAllWidgetsOneByOne(widgets);
    }

    @Override
    public void tick() {
        super.tick();

        if (!player.canInteractWithEntity(entity, CLOSE_SCREEN_DISTANCE)) {
            HighlightManager.highlightEntity(entity, 4 * 20);
            ClientUtils.sendCenteredMessage(Component.translatable(Lang.TEXT_TARGET_ENTITY_TOO_FAR).withStyle(ChatFormatting.YELLOW));
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
