package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.MobSpawnProperty;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyBar;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public abstract class AbstractMobSpawnWidget<E extends Mob> extends EntityPropertyStandardWidget<E> {
    private static final int L = 14, T = 4;

    private final MobSpawnProperty spawnProperty = p().getExtra(MobSpawnProperty.class);

    public AbstractMobSpawnWidget(EntityProperties<E> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new SpawnBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        List<Component> list = new ArrayList<>();
        appendTooltip(list, ctx);
        renderTooltip(ctx, list);
        return true;
    }

    protected void appendTooltip(List<Component> list, ScreenRenderingContext ctx) {
        list.add(tooltipTitle(Lang.PROPERTY_WIDGET_SPAWN));
        list.add(tooltipDescription(Lang.PROPERTY_WIDGET_SPAWN_DESC));
        list.add(TextUtils.empty());
        appendTooltipExtra(list, ctx);

        MobSpawnProperty.Data data = spawnProperty.getVal();
        if (data == null) {
            list.add(tooltipBody(Lang.TEXT_EMPTY_WITH_BRACKETS));
            return;
        }

        list.add(tooltipBody(TextUtils.translate(
                Lang.PROPERTY_WIDGET_SPAWN_BIOMES, data.biomes().size())).withStyle(ChatFormatting.YELLOW));
        if (data.biomes().isEmpty()) {
            list.add(tooltipBody(Lang.TEXT_EMPTY_WITH_BRACKETS));
        } else {
            List<Component> biomeNames = new ArrayList<>();
            for (Identifier id : data.biomes()) {
                biomeNames.add(biomeName(id));
            }
            appendWrappedItems(list, ctx, biomeNames);
        }

        list.add(TextUtils.empty());
        list.add(tooltipBody(TextUtils.translate(
                Lang.PROPERTY_WIDGET_SPAWN_STRUCTURES, data.structures().size()))
                .withStyle(ChatFormatting.YELLOW));
        if (data.structures().isEmpty()) {
            list.add(tooltipBody(Lang.TEXT_EMPTY_WITH_BRACKETS));
        } else {
            List<Component> structureNames = new ArrayList<>();
            for (Identifier id : data.structures()) {
                String key = Lang.STRUCTURE_PREFIX + id.getNamespace() + "." + id.getPath();
                structureNames.add(TextUtils.translate(key).withStyle(ChatFormatting.WHITE));
            }
            appendWrappedItems(list, ctx, structureNames);
        }
    }

    /**
     * Invoked right after the title / description header, before the spawn biomes and structures.
     * Subclasses may append extra sections here.
     */
    protected void appendTooltipExtra(List<Component> list, ScreenRenderingContext ctx) {}

    protected String barText(MobSpawnProperty.Data data) {
        return data.biomes().size() + " + " + data.structures().size();
    }

    protected static Component biomeName(Identifier id) {
        String key = Lang.BIOME_PREFIX + id.getNamespace() + "." + id.getPath();
        return TextUtils.translate(key).withStyle(ChatFormatting.WHITE);
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
            renderInnerText(ctx, TextUtils.literal(barText(data)));
        }
    }
}
