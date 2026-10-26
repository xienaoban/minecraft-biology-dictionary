package io.github.xienaoban.biologydictionary.gui.component.entitylist;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityClassificationManager.Category;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen.Catalog;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * One row of a classification's category tree. Clicking the row selects the category; when
 * the host screen provides a preview action, a button appears inside the right end of the
 * row while the row is hovered.
 */
@ClientOnly
public final class CategoryCatalog extends Catalog {
    private final Category category;

    private final PreviewButton previewButton;

    private final Runnable onSelect;
    private final Runnable onPreview;

    public CategoryCatalog(int depth, Category category, Runnable onSelect, Runnable onPreview) {
        super(depth, TextUtils.concat(
                Arrays.asList(category.getText(), TextUtils.literal("(" + category.getEntities().size() + ")")),
                TextUtils.space()));
        this.category = category;
        this.previewButton = onPreview == null ? null : new PreviewButton();
        this.onSelect = onSelect;
        this.onPreview = onPreview;
        if (previewButton != null) {
            previewButton.setParent(this);
        }
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        if (previewButton != null) {
            ScreenElementBox rowBox = getBox();
            ScreenElementBox buttonBox = previewButton.getBox();
            buttonBox.setPosition(rowBox.getRight() - buttonBox.getWidth() - 2,
                    rowBox.getTop() + (rowBox.getHeight() - buttonBox.getHeight()) / 2);
        }
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (isMouseLeft(button)) {
            ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 1.5F);
            onSelect.run();
            return true;
        }
        return super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        if (category.getDescription() == null) { return true; }
        int d = depth * 8;
        ScreenElementBox box = getBox();
        List<Component> texts = new ArrayList<>();
        texts.add(category.getDescription());
        ctx.renderComponentTooltip(texts, 0.5F, box.getLeft() + d + Widget.WIDGET_WIDTH, box.getBottom() + 1);
        return true;
    }

    private final class PreviewButton extends Widget {
        private PreviewButton() {
            super(1, 1);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
            ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
            onPreview.run();
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            ScreenElementBox rowBox = CategoryCatalog.this.getBox();
            if (!rowBox.isInBox(ctx.getMouseX(), ctx.getMouseY())) { return; }
            ScreenElementBox box = getBox();
            ctx.renderButtonBackground(ctx.getElementScreen().getHoveredElement() == this,
                    box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
            ctx.renderTexture(Textures.SEARCH, box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            ctx.renderComponentTooltipCentered(
                    List.of(TextUtils.translate(Lang.WIDGET_SEARCH_CATEGORY_PREVIEW_DESC)),
                    0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
            return true;
        }
    }
}
