package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.MobSpawnProperty;
import io.github.xienaoban.biologydictionary.core.widget.branch.AbstractMobSpawnWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public final class SlimeSurfaceSpawnWidget extends AbstractMobSpawnWidget<Slime> {
    public static final Factory<Slime> FACTORY = SlimeSurfaceSpawnWidget::new;

    private final List<Component> surfaceBiomeNames;

    public SlimeSurfaceSpawnWidget(EntityProperties<Slime> properties) {
        super(properties);
        surfaceBiomeNames = buildSurfaceBiomeNames(e());
    }

    @Override
    protected String barText(MobSpawnProperty.Data data) {
        return data.biomes().size() + "(" + surfaceBiomeNames.size() + ") + " + data.structures().size();
    }

    @Override
    protected void appendTooltipExtra(List<Component> list, ScreenRenderingContext ctx) {
        list.add(tooltipBody(TextUtils.translate(
                Lang.PROPERTY_WIDGET_SPAWN_SURFACE_BIOMES, surfaceBiomeNames.size())).withStyle(ChatFormatting.YELLOW));
        if (surfaceBiomeNames.isEmpty()) {
            list.add(tooltipBody(Lang.TEXT_EMPTY_WITH_BRACKETS));
        } else {
            appendWrappedItems(list, ctx, surfaceBiomeNames);
        }
        list.add(TextUtils.empty());
    }

    /**
     * Biomes where slimes spawn on the surface without the slime chunk constraint, i.e. the content of
     * {@link net.minecraft.tags.BiomeTags#ALLOWS_SURFACE_SLIME_SPAWNS} (swamps by default).
     */
    private static List<Component> buildSurfaceBiomeNames(Slime slime) {
        Registry<Biome> biomeRegistry = slime.registryAccess().lookupOrThrow(Registries.BIOME);
        List<Component> names = new ArrayList<>();
        for (Holder<Biome> holder : biomeRegistry.getTagOrEmpty(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS)) {
            holder.unwrapKey().map(IdentifierUtils::getId).ifPresent(id -> names.add(biomeName(id)));
        }
        return List.copyOf(names);
    }
}
