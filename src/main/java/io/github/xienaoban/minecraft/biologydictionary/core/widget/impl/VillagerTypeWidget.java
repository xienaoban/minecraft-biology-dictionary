package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;

import java.util.List;

public final class VillagerTypeWidget extends AbstractEntityVariantWidget<Villager, Holder<VillagerType>> {
    private static final List<Holder<VillagerType>> types = BuiltInRegistries.VILLAGER_TYPE.listElements().map(ref -> (Holder<VillagerType>) ref).toList();

    public VillagerTypeWidget(EntityProperties<Villager> properties) {
        super(properties, types.size(), 7, 3);
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected List<Holder<VillagerType>> getAllVariants() {
        return types;
    }

    @Override
    protected Holder<VillagerType> getVariantClient(Villager entity) {
        return entity.getVillagerData().type();
    }

    @Override
    protected void setVariantClient(Villager entity, Holder<VillagerType> variant) {
        entity.setVillagerData(entity.getVillagerData().withType(variant));
    }

    @Override
    protected Component getVariantName(Holder<VillagerType> variant) {
        String name = variant.unwrapKey().map(resourceKey -> {
            ResourceLocation rl = resourceKey.location();
            String res;
            if (ResourceLocation.DEFAULT_NAMESPACE.equals(rl.getNamespace())) {
                res = rl.getPath();
            } else {
                res = rl.getNamespace() + '.' + rl.getPath();
            }
            return res;
        }).orElse("unknown");
        return Component.translatable(getVariantNameKeyPrefix() + name);
    }

    @Override
    protected void writeVariantToNbt(VariantElement element, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        CodecProperty<Villager, VillagerData> property = EntityVanillaProperties.OfVillager.createVillagerDataProperty();
        property.set(e().getVillagerData().withType(element.getVariant()));
        property.writeTo(vanillaNbt);
    }
}
