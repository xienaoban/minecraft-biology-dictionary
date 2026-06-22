package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.vanilla.EntityReferenceProperty;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntityGiftPetSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyTextBar;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.gui.screen.misc.PlayerSelectorScreen;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ClientOnly
public class EntityOwnerWidget extends EntityPropertyStandardWidget<Entity> {
    public static final Factory<Entity> FACTORY = properties -> {
        if (properties.entity() instanceof OwnableEntity) {
            return new EntityOwnerWidget(properties);
        }
        return null;
    };

    private static final int L = 11, T = 5;
    private static final int L_GIFT = 22, T_GIFT = 4;

    private static final String OWNER_KEY = VanillaEntityProperties.OfTamableAnimal.createOwnerProperty().name();

    private final EntityReferenceProperty<AbstractHorse> ownerProperty = p().getVanilla(OWNER_KEY);

    private UUID lastUuid = null;
    private Entity lastEntity = null;

    public EntityOwnerWidget(EntityProperties<Entity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new OwnerBar());
        addElementButton(new GiftButton());
    }

    private void updateOwnerRef() {
        EntityReference<Entity> ref = ownerProperty.getVal();
        if (ref == null) {
            if (lastUuid != null) {
                lastUuid = null;
                lastEntity = null;
            }
        } else {
            UUID uuid = ref.getUUID();
            if (!Objects.equals(uuid, lastUuid)) {
                lastUuid = uuid;
                lastEntity = ref.getEntity(ClientUtils.getClientLevel(), Entity.class);
            }
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        List<Component> list = new ArrayList<>();
        list.add(tooltipTitle(Lang.PROPERTY_WIDGET_OWNER));
        list.add(tooltipDescription(Lang.PROPERTY_WIDGET_OWNER_DESC));
        list.add(tooltipEmpty());
        if (lastUuid == null) {
            list.add(tooltipBody(Lang.PROPERTY_WIDGET_OWNER_NONE));
        } else {
            list.add(tooltipBody(Lang.PROPERTY_WIDGET_OWNER_UUID, lastUuid.toString()));
            if (lastEntity == null) {
                list.add(tooltipBody(Lang.PROPERTY_WIDGET_OWNER_NOT_ONLINE));
            } else {
                list.add(tooltipBody(Lang.PROPERTY_WIDGET_OWNER_NAME, lastEntity.getName()));
            }
        }
        renderTooltip(ctx, list);
        return true;
    }

    private final class OwnerBar extends EntityPropertyTextBar {
        public OwnerBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
            // Do not update here as getOwnerRef() may be null (not initialized yet).
            // updateOwnerRef();
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            updateOwnerRef();
            if (lastUuid == null) {
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NONE_WITH_BRACKETS), Colors.COMMON_LIGHT_TEXT);
            } else if (lastEntity == null) {
                renderInnerText(ctx, TextUtils.literal(lastUuid.toString()), Colors.COMMON_LIGHT_TEXT);
            } else {
                renderInnerText(ctx, lastEntity.getName(), Colors.COMMON_LIGHT_TEXT);
            }
        }
    }

    public final class GiftButton extends EntityPropertyButton {
        public GiftButton() {
            super(Textures.ICONS, L_GIFT * WIDGET_WIDTH, T_GIFT * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                if (((OwnableEntity) e()).getOwnerReference() == null) {
                    AbstractBiologyDictionaryScreen.current()
                            .sendScreenMessage(TextUtils.translate(Lang.TEXT_ENTITY_NOT_TAMED));
                    return true;
                }

                LocalPlayer player = ClientUtils.getClientPlayer();
                if (lastEntity != player && !(PlayerUtils.isCreative(player) && PlayerUtils.isOp(player))) {
                    AbstractBiologyDictionaryScreen.current()
                            .sendScreenMessage(TextUtils.translate(Lang.TEXT_NOT_OWNER_NO_PERMISSION_TO_GIFT));
                    return true;
                }

                ClientUtils.setScreen(new PlayerSelectorScreen(ClientUtils.getCurrentScreen(), targetPlayer -> {
                    AbstractBiologyDictionaryScreen.current().sendScreenMessage(null);
                    BiologySkills.activate(e(), new EntityGiftPetSkill(targetPlayer));
                    ownerProperty.setVal(EntityReference.of(targetPlayer.getUUID()));
                }
                ));
            }
            return true;
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            // Target player is selected at runtime, so we use a placeholder UUID
            SkillCost cost = new EntityGiftPetSkill(new UUID(0, 0)).getRealCost(e());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_OWNER_GIFT));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_OWNER_GIFT_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}
