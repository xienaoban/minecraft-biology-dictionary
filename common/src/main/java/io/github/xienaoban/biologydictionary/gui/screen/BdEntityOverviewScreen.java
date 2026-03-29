package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.client.EntityTypeOverviewCache;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.EntityPropertyWidgets;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class BdEntityOverviewScreen extends AbstractBiologyDictionaryScreen {
    private final EntityType<?> entityType;
    private final Entity entity;
    private final EntityProperties<?> properties;

    public BdEntityOverviewScreen(EntityType<?> entityType) {
        super(io.github.xienaoban.biologydictionary.platform.util.TextUtils.literal("[Reference] ").append(entityType.getDescription()));
        this.entityType = entityType;
        this.entity = EntityUtils.create(entityType, ClientUtils.getClientLevel());
        this.properties = new EntityProperties<>(entity);

        initBookmarks();
        initEntityPropertyWidgets();
    }

    private void initBookmarks() {
        addBookmarkFromLast(new OpenBdAboutScreenBookmark());
        addBookmarkFromLast(new OpenBdConfigScreenBookmark());
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initEntityPropertyWidgets() {
        List<EntityPropertyWidget<?>> widgets = EntityPropertyWidgets.getWidgets(properties);
        addAllWidgetsOneByOne(widgets);
    }

    public void initOrRequestProperties() {
        EntityTypeOverviewCache.CacheEntry cache = EntityTypeOverviewCache.get(entityType);
        if (cache != null) {
            updateProperties(cache.vanillaNbt, cache.extraNbt);
        } else {
            // RequestEntityOverviewPacket -> ReplyEntityOverviewPacket -> put cache & updateProperties
            ClientNetManager.requestEntityOverview(entityType);
        }
    }

    public void updateProperties(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        EntityUtils.setNbt(entity, vanillaNbt);
        properties.update(vanillaNbt, extraNbt);
    }

    public boolean matchesType(String entityTypeId) {
        return Objects.equals(entityTypeId,  EntityUtils.getEntityTypeIdName(entityType));
    }
}
