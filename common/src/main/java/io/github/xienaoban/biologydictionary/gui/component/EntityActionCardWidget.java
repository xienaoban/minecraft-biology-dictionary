package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.general.GetSpawnEggSkill;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.gui.EntityDisplay;
import io.github.xienaoban.biologydictionary.gui.EntityDisplayRotation;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.gui.screen.BdEntityOverviewScreen;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity card of the plain entity lists: left click opens the overview, right
 * click (or middle click) highlights the entity, the lower part offers a spawn egg.
 */
@ClientOnly
public final class EntityActionCardWidget extends EntityCardWidget {
    private static final int BUTTONS_TOTAL = 2 * Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN;
    private static final int BUTTONS_CUT = BUTTONS_TOTAL * 2 / 3;

    /**
     * Create the cards of the given entries, skipping the ones hidden by the
     * "only discovered entities" filter.
     */
    public static List<Widget> createWidgets(List<EntityDictionaryEntry> entries, EntityDisplayRotation rotation) {
        ClientLevel level = ClientUtils.getClientLevel();
        List<Widget> widgets = new ArrayList<>();
        for (EntityDictionaryEntry entry : entries) {
            if (!EntityCardWidget.isShown(entry)) { continue; }
            widgets.add(new EntityActionCardWidget(entry, new EntityDisplay(entry, level), rotation));
        }
        return widgets;
    }

    private final ItemStack spawnEgg;

    public EntityActionCardWidget(EntityDictionaryEntry entry, EntityDisplay display, EntityDisplayRotation rotation) {
        super(entry, display, rotation);
        Item item = EntityUtils.getSpawnEggItem(entry.getType());
        this.spawnEgg = item == null ? null : new ItemStack(item);
    }

    private boolean isClickable() {
        return BdEntityOverviewScreen.canOpen(entry);
    }

    @Override
    protected boolean shouldRenderDetail() {
        return isClickable();
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (!isClickable()) {
            AbstractBiologyDictionaryScreen.current()
                    .sendScreenMessage(TextUtils.translate(Lang.TEXT_ENTITY_NOT_DISCOVERED));
            return true;
        }

        ScreenElementBox box = getBox();
        float relativeMouseY = mouseY - box.getTop();

        if (relativeMouseY < BUTTONS_CUT) {
            if (isMouseLeft(button)) {
                // Overview button
                ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                BdEntityOverviewScreen screen = new BdEntityOverviewScreen(entry);
                screen.setLastScreen(AbstractBiologyDictionaryScreen.current());
                ClientUtils.setScreen(screen);
                screen.initOrRequestProperties();
            } else {
                // Highlight button
                int distance = isMouseRight(button)
                        ? HighlightEntitiesSkill.getNearRadius()
                        : HighlightEntitiesSkill.getFarRadius();
                ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 0.8F);
                if (BiologySkills.activate(
                        new HighlightEntitiesSkill(entry.getType(), distance))) {
                    AbstractBiologyDictionaryScreen.current().onClose();
                }
            }
        } else {
            // Spawn egg button - bottom section
            BiologySkills.activate(new GetSpawnEggSkill(entry.getType()));
        }

        return true;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        int color = 0x0B794500;
        ctx.renderRectangle(color, 1F, ctx.getZ(),
                box.getLeft() - 1, box.getTop() - 1, box.getRight() + 1, box.getBottom() + 1);
        ctx.renderRectangle(color, ctx.getZ(),
                box.getLeft(), box.getBottom() - 6, box.getRight(), box.getBottom());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        ScreenElementBox box = getBox();
        float midX = (box.getLeft() + box.getRight()) / 2;
        float mouseY = ctx.getMouseY() - box.getTop();

        ctx.renderRectangle(0x77794500, ctx.getZ(),
                box.getLeft() - 1, box.getTop() - 1, box.getRight() + 1, box.getBottom() + 1);

        // Render button sections
        int colorHighlight, colorEgg;
        if (mouseY < BUTTONS_CUT) {
            colorHighlight = 0xd4ffffff;
            colorEgg = 0xaaffffff;
        } else {
            colorHighlight = 0xaaffffff;
            colorEgg = 0xd4ffffff;
        }
        ctx.renderRectangle(colorHighlight, ctx.getZ(),
                box.getLeft(), box.getTop(), box.getRight(), box.getTop() + BUTTONS_CUT);
        ctx.renderRectangle(colorEgg, ctx.getZ(),
                box.getLeft(), box.getTop() + BUTTONS_CUT, box.getRight(), box.getBottom());

        // Render icons for each section
        final int wh = 10;

        // Highlight icon (top) - animated
        final int wink = 600, cycle = 2400;
        long time = System.currentTimeMillis() % cycle;
        int u;
        if (time < wink) { u = -1; }
        else if (time < cycle / 2) { u = 0; }
        else if (time < cycle / 2 + wink) { u = 1; }
        else { u = 0; }
        ctx.renderTexture(Textures.ICONS, (23 + u) * wh, 24 * wh, ctx.getZ(),
                midX - wh / 2F, box.getTop() + (BUTTONS_CUT - wh) / 2F,
                Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);

        if (shouldRenderDetail()) {
            // Spawn egg icon (bottom)
            if (spawnEgg != null) {
                ctx.renderItem(spawnEgg, 0.5F, midX - 4F, box.getTop() + BUTTONS_CUT);
            }
        }

        // Tooltip
        List<Component> tooltips;
        if (mouseY < BUTTONS_CUT) {
            int nearRadius = HighlightEntitiesSkill.getNearRadius();
            int farRadius = HighlightEntitiesSkill.getFarRadius();
            tooltips = new ArrayList<>();
            tooltips.add(tooltipTitle(Lang.WIDGET_ENTITY_OVERVIEW));
            tooltips.add(tooltipDescription(Lang.WIDGET_ENTITY_OVERVIEW_DESC));
            tooltips.add(TextUtils.empty());
            tooltips.add(TextUtils.translate(Lang.WIDGET_ENTITY_OVERVIEW_LEFT_DESC)
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
            tooltips.add(TextUtils.empty());
            tooltips.add(TextUtils.translate(
                            Lang.WIDGET_ENTITY_HIGHLIGHT_RIGHT_DESC, nearRadius)
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
            tooltips.addAll(new HighlightEntitiesSkill(
                    entry.getType(), nearRadius)
                    .getRealCost().toTooltipText());
            tooltips.add(TextUtils.empty());
            tooltips.add(TextUtils.translate(
                            Lang.WIDGET_ENTITY_HIGHLIGHT_MIDDLE_DESC, farRadius)
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
            tooltips.addAll(new HighlightEntitiesSkill(
                    entry.getType(), farRadius)
                    .getRealCost().toTooltipText());
            tooltips.add(TextUtils.empty());
            tooltips.add(TextUtils.literal(EntityUtils.getEntityTypeIdName(entry.getType()))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltips = new ArrayList<>();
            tooltips.add(tooltipTitle(Lang.WIDGET_ENTITY_OFFER_SPAWN_EGG));
            tooltips.add(tooltipDescription(Lang.WIDGET_ENTITY_OFFER_SPAWN_EGG_DESC));
            tooltips.add(TextUtils.empty());
            tooltips.addAll(new GetSpawnEggSkill(entry.getType()).getRealCost().toTooltipText());
            tooltips.add(TextUtils.empty());
            tooltips.add(TextUtils.literal(EntityUtils.getEntityTypeIdName(entry.getType()))
                    .withStyle(ChatFormatting.GRAY));
        }

        ctx.renderComponentTooltipCentered(tooltips, 0.5F, midX, box.getBottom() + 2);
        return true;
    }
}
