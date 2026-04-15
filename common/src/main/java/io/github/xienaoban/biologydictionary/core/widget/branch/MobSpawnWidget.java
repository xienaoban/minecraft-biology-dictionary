package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.MobSpawnProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class  MobSpawnWidget extends EntityPropertyStandardWidget<Mob> {
    public static final Factory<Mob> FACTORY = MobSpawnWidget::new;

    private static final int L = 14, T = 4;

    private final MobSpawnProperty spawnProperty = p().getExtra(MobSpawnProperty.class);

    public MobSpawnWidget(EntityProperties<Mob> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new SpawnBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        List<Component> list = new ArrayList<>();
        list.add(tooltipTitle(Lang.PROPERTY_WIDGET_SPAWN));
        list.add(tooltipDescription(Lang.PROPERTY_WIDGET_SPAWN_DESC));
        list.add(TextUtils.empty());

        MobSpawnProperty.Data data = spawnProperty.getVal();
        if (data == null || (data.biomes().isEmpty() && data.structures().isEmpty())) {
            list.add(tooltipBody(Lang.TEXT_EMPTY_WITH_BRACKETS));
        } else {
            if (!data.biomes().isEmpty()) {
                list.add(tooltipBody(TextUtils.translate(
                        Lang.PROPERTY_WIDGET_SPAWN_BIOMES, data.biomes().size())).withStyle(ChatFormatting.YELLOW));
                List<Component> biomeNames = new ArrayList<>();
                for (Identifier id : data.biomes()) {
                    String key = Lang.BIOME_PREFIX + id.getNamespace() + "." + id.getPath();
                    biomeNames.add(TextUtils.translate(key).withStyle(ChatFormatting.WHITE));
                }
                appendWrappedItems(list, ctx, biomeNames);
            }
            if (!data.structures().isEmpty()) {
                if (!data.biomes().isEmpty()) {
                    list.add(TextUtils.empty());
                }
                list.add(tooltipBody(TextUtils.translate(
                        Lang.PROPERTY_WIDGET_SPAWN_STRUCTURES, data.structures().size())).withStyle(ChatFormatting.YELLOW));
                List<Component> structureNames = new ArrayList<>();
                for (Identifier id : data.structures()) {
                    String key = Lang.STRUCTURE_PREFIX + id.getNamespace() + "." + id.getPath();
                    structureNames.add(TextUtils.translate(key).withStyle(ChatFormatting.WHITE));
                }
                appendWrappedItems(list, ctx, structureNames);
            }
        }

        renderTooltip(ctx, list);
        return true;
    }

    private final class SpawnBar extends EntityPropertyBar {
        public SpawnBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            renderFullBar(ctx);

            MobSpawnProperty.Data data = spawnProperty.getVal();
            if (data == null) {
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }
            String text = data.biomes().size() + " + " + data.structures().size();
            renderInnerText(ctx, TextUtils.literal(text));
        }
    }
}
