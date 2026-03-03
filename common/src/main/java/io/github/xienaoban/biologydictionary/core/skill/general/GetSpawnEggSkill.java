package io.github.xienaoban.biologydictionary.core.skill.general;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public record GetSpawnEggSkill(EntityType<?> entityType) implements GeneralSkill {
    public static final Meta<GetSpawnEggSkill> META = new Meta<>() {
        @Override
        public GetSpawnEggSkill create(FriendlyByteBuf buf) {
            return new GetSpawnEggSkill(EntityUtils.getEntityType(buf.readUtf()));
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.creativeOnly();
        }

        @Override
        public String shortName() {
            return "get_spawn_egg";
        }
    };

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityType == null ? "" : EntityUtils.getEntityTypeIdString(entityType));
    }

    @Override
    public void serverDo(ServerContext ctx) {
        if (entityType == null) {
            BiologyDictionary.sendCenteredWarning(ctx.player(), TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_TYPE));
        } else {
            SpawnEggItem item = SpawnEggItem.byId(entityType);
            if (item == null) {
                BiologyDictionary.sendCenteredWarning(ctx.player(), TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS));
            } else {
                ItemStack stack = new ItemStack(item);
                PlayerUtils.getInventory(ctx.player()).add(stack);
                // @see net.minecraft.server.commands.GiveCommand.giveItem
                PlayerUtils.playLocalSound(ctx.player(), SoundEvents.ITEM_PICKUP, 1F, ((ctx.player().getRandom().nextFloat() - ctx.player().getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                BiologyDictionary.sendCenteredWarning(ctx.player(),
                        TextUtils.translate(Lang.TEXT_OFFER_OR_DROP, TextUtils.translate(item.getDescriptionId())));
            }
        }
    }
}
