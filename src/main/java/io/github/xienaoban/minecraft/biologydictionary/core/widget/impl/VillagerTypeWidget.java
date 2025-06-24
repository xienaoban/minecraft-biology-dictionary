package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;

import java.util.List;

public final class VillagerTypeWidget extends AbstractEntityVariantWidget<Villager, VillagerType> {
    private static final List<VillagerType> types = BuiltInRegistries.VILLAGER_TYPE.stream().toList();

    public VillagerTypeWidget(EntityProperties<Villager> properties) {
        super(properties, types.size(), 7, 3);
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected List<VillagerType> getAllVariants() {
        return types;
    }

    @Override
    protected VillagerType getVariantClient(Villager entity) {
        // return entity.getVariant();
        return null;
    }

    @Override
    protected void setVariantClient(Villager entity, VillagerType variant) {
        // entity.setVariant(variant);
    }

    @Override
    protected Component getVariantName(VillagerType variant) {
        return Component.translatable(getVariantNameKeyPrefix() + variant.toString());
    }

    @Override
    protected void writeVariantToNbt(VariantElement element, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        // VillagerVillagerDataProperty property = EntityVanillaProperties.OfVillager.createVillagerDataProperty();
        // VillagerType villagerType = element.getVariant();
        // VillagerData rawData = e().getVillagerData();
        // property.set(new VillagerData(villagerType, rawData.profession(), rawData.level()));
        // ClientNetManager.sendUpdatedEntityProperties(e(), property.toNbt(), null);
    }
}
