package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.gui.component.dialog.WarningDialog;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Set;

/**
 * Adds the currently selected entity types to the server blacklist. Because the blacklist
 * is a server-side config, a purely local change has no effect when connected to a remote
 * server; the player is warned in that case.
 */
@ClientOnly
public final class AddToBlacklistButton extends Widget {
    private final Set<EntityType<?>> selection;
    private final Runnable onBlacklistApplied;

    public AddToBlacklistButton(Set<EntityType<?>> selection, Runnable onBlacklistApplied) {
        super(1, 3);
        this.selection = selection;
        this.onBlacklistApplied = onBlacklistApplied;
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
        ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
        if (selection.isEmpty()) {
            AbstractBiologyDictionaryScreen.current()
                    .sendScreenMessage(TextUtils.translate(Lang.TEXT_BLACKLIST_NO_SELECTION));
            return true;
        }
        Component msg = ClientUtils.isLocalServer()
                ? TextUtils.translate(Lang.DIALOG_BLACKLIST_MESSAGE)
                : TextUtils.concat(
                        TextUtils.translate(Lang.DIALOG_BLACKLIST_MESSAGE),
                        TextUtils.newline(),
                        TextUtils.translate(Lang.DIALOG_BLACKLIST_MESSAGE2));
        AbstractBiologyDictionaryScreen.current().showDialog(new WarningDialog(
                TextUtils.translate(Lang.DIALOG_BLACKLIST_TITLE),
                msg)
                .addButton(TextUtils.translate(Lang.GUI_OK), LongButton.STYLE_CONFIRM, this::applyBlacklist)
                .addButton(TextUtils.translate(Lang.GUI_CANCEL), LongButton.STYLE_CANCEL, null));
        return true;
    }

    private void applyBlacklist() {
        AbstractBiologyDictionaryScreen screen = AbstractBiologyDictionaryScreen.current();
        if (selection.isEmpty()) {
            screen.sendScreenMessage(TextUtils.translate(Lang.TEXT_BLACKLIST_NO_SELECTION));
            return;
        }
        List<String> ids = selection.stream()
                .map(EntityUtils::getEntityTypeIdName)
                .toList();
        ConfigsManager.addEntityTypeBlacklist(ids);
        onBlacklistApplied.run();
        if (ClientUtils.isLocalServer()) {
            screen.sendScreenMessage(TextUtils.translate(Lang.TEXT_BLACKLIST_APPLIED, ids.size()));
        } else {
            screen.sendScreenMessage(TextUtils.translate(Lang.TEXT_BLACKLIST_SERVER_CONFIG_WARNING));
        }
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        boolean hovered = ctx.getElementScreen().getHoveredElement() == this;
        int textureTop = (hovered ? 22 : 23) * WIDGET_HEIGHT;
        ctx.renderTexture(Textures.ICONS, 8 * WIDGET_WIDTH, textureTop, ctx.getZ(),
                box.getLeft(), box.getTop(), 3 * WIDGET_WIDTH, WIDGET_HEIGHT);
        ctx.renderTexture(Textures.ICONS,
                8 * WIDGET_WIDTH, textureTop,
                11 * WIDGET_WIDTH, textureTop + WIDGET_HEIGHT,
                ctx.getZ(), box.getRight(), box.getBottom(),
                box.getRight() - 3 * WIDGET_WIDTH, box.getBottom() - WIDGET_HEIGHT);
        ctx.renderCenteredText(TextUtils.translate(Lang.WIDGET_ENTITY_SELECTION_BLACKLIST),
                hovered ? Colors.BLACK : Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(),
                (box.getLeft() + box.getRight()) / 2, box.getTop() + 3 + TXT_TO);
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        ScreenElementBox box = getBox();
        ctx.renderComponentTooltipCentered(
                List.of(TextUtils.translate(Lang.WIDGET_ENTITY_SELECTION_BLACKLIST_DESC)),
                0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
        return true;
    }
}
