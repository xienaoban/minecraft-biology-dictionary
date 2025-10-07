package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public abstract class AbstractEntityVariantWidget<E extends Entity, V> extends EntityPropertyWidget<E> {
    protected static final int BG_BAR1_LEFT = 11, BG_BAR1_TOP = 24;
    protected static final int BG_BAR2_LEFT = 13, BG_BAR2_TOP = 24;

    private static RC calcRowsAndColumns(int variantCnt, int maxDisplayCntPerLine, int rowsPerVariant) {
        int rows = rowsPerVariant * ((variantCnt + maxDisplayCntPerLine - 1) / maxDisplayCntPerLine);
        int cols;
        if (variantCnt <= maxDisplayCntPerLine / 2) {
            cols = Page.COLUMNS / 2;
        } else {
            cols = Page.COLUMNS;
        }
        return new RC(rows, cols);
    }

    private final int size;
    private final List<VariantElement> variantElements;
    private final int[] displayCntPerRow;
    private final float variantWidth, variantHeight;

    private int chosenIndex;

    private final List<BackgroundBar> backgroundBars = new ArrayList<>();
    private final LocalPlayer player = Objects.requireNonNull(ClientUtils.getClientPlayer());

    public AbstractEntityVariantWidget(EntityProperties<E> properties, int variantCnt) {
        this(properties, variantCnt, 7, 2);
    }

    public AbstractEntityVariantWidget(EntityProperties<E> properties, int variantCnt, int maxDisplayCntPerLine, int rowsPerVariant) {
        super(properties, calcRowsAndColumns(variantCnt, maxDisplayCntPerLine, rowsPerVariant));

        size = variantCnt;
        List<V> variants = getAllVariants();
        if (variantCnt != variants.size()) {
            throw new RuntimeException("mismatch");
        }

        int lines = (variantCnt + maxDisplayCntPerLine - 1) / maxDisplayCntPerLine;
        int minCnt = variantCnt / lines;
        int mod = variantCnt % lines;
        displayCntPerRow = new int[lines];
        for (int i = 0; i < lines; ++i, --mod) {
            displayCntPerRow[i] = minCnt + (mod > 0 ? 1 : 0);
        }
        variantWidth = getBox().getWidth() / displayCntPerRow[0] - 1;
        variantHeight = getBox().getHeight() / lines - 1;
        variantElements = new ArrayList<>(size());
        for (int i = 0; i < size(); ++ i) {
            VariantElement e = new VariantElement(i, variants.get(i));
            e.setParent(this);
            variantElements.add(e);
        }

        chosenIndex = 0;
        checkChosenVariant();
    }

    /**
     * Attention: when you call this method you may not have initialized you subclass. So the
     * implementation of this method should not depend on the member variables of the subclass.
     */
    protected abstract List<V> getAllVariants();

    /**
     * Attention: when you call this method you may not have initialized you subclass. So the
     * implementation of this method should not depend on the member variables of the subclass.
     */
    protected abstract V getVariantClient(E entity);

    /**
     * Attention: when you call this method you may not have initialized you subclass. So the
     * implementation of this method should not depend on the member variables of the subclass.
     */
    protected abstract void setVariantClient(E entity, V variant);

    /**
     * Attention: when you call this method you may not have initialized you subclass. So the
     * implementation of this method should not depend on the member variables of the subclass.
     */
    protected abstract Component getVariantName(V variant);

    /**
     * No attention.
     */
    protected abstract void writeVariantToNbt(VariantElement element, CompoundTag vanillaNbt, CompoundTag extraNbt);

    protected boolean isAllowedToChoose() { return player.isCreative(); }

    protected boolean equals(V v1, V v2) {
        return Objects.equals(v1, v2);
    }

    public final int size() {
        return size;
    }

    public final int getChosenIndex() {
        return chosenIndex;
    }

    public final V getChosenVariant() {
        return variantElements.get(chosenIndex).variant;
    }

    public final boolean isChosen(int variantIndex) {
        return variantIndex == getChosenIndex();
    }

    public final boolean isChosen(V variant) {
        return equals(variant, getChosenVariant());
    }

    public final String getVariantNameKeyPrefix() {
        // variant.minecraft.cat.xxxx
        ResourceLocation rl = EntityUtils.getEntityTypeId(e());
        return "variant." + rl.getNamespace() + "." + rl.getPath() + ".";
    }

    protected final void setVariantElementWidthFix(float widthFix) {
        for (VariantElement ve : variantElements) {
            ve.setWidthFix(widthFix);
        }
    }

    protected final void setVariantElementHeightFix(float heightFix) {
        for (VariantElement ve : variantElements) {
            ve.setHeightFix(heightFix);
        }
    }

    protected final void setBackgroundBars(TextureInfo texture, int textureLeft, int textureTop) {
        for (BackgroundBar bar : backgroundBars) {
            bar.setParent(null);
        }
        backgroundBars.clear();
        for (int i = 0; i < displayCntPerRow.length; ++i) {
            BackgroundBar bar = new BackgroundBar(texture, textureLeft, textureTop);
            bar.getBox().setSize(getBox().getWidth(), Widget.WIDGET_HEIGHT);
            bar.setPriority(-1);
            bar.setParent(this);
            backgroundBars.add(bar);
        }
    }

    private void checkChosenVariant() {
        V curr = getVariantClient(e());
        V last = getChosenVariant();
        if (equals(curr, last)) {
            return;
        }
        chosenIndex = getVariantIndex(curr);
    }

    private int getVariantIndex(V variant) {
        int idx = 0;
        boolean hit = false;
        for (int i = 0; i < size; ++i) {
            if (equals(variant, variantElements.get(i).variant)) {
                if (hit) {
                    throw new RuntimeException("Multiple hit variants!");
                }
                hit = true;
                idx = i;
            }
        }
        return idx;
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        if (ticks % 10 == 5) {
            checkChosenVariant();
        }
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);

        final float wWidth = getBox().getWidth();
        final float wHeight = getBox().getHeight();
        final float wLeft = getBox().getLeft();
        final float wTop = getBox().getTop();

        final int lines = displayCntPerRow.length;
        final float mHeight = wHeight / lines;
        int vIdx = 0;
        for (int i = 0; i < lines; ++i) {
            float top = wTop + i * mHeight + mHeight / 2 - variantHeight / 2;

            int cnt = displayCntPerRow[i];
            final float mWidth = wWidth / cnt;
            for (int j = 0; j < cnt; ++j) {
                float left = wLeft + j * mWidth + mWidth / 2 - variantWidth / 2;
                variantElements.get(vIdx).getBox().setPosition(left, top);
                ++vIdx;
            }

            if (i < backgroundBars.size()) {
                BackgroundBar bar = backgroundBars.get(i);
                ScreenElementBox box = bar.getBox();
                box.setPosition(wLeft, top + 3);
            }
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_VARIANT),
                tooltipDescription(Lang.PROPERTY_WIDGET_VARIANT_DESC)
        );
        return true;
    }

    public final class VariantElement extends ScreenElement {
        private static final float FONT_SIZE = 0.5F;

        private final int index;

        private final V variant;
        private final E model;
        private final Component name;
        private float nameWidth = -1;

        private float widthFix;
        private float heightFix;

        private final ScreenRenderingContext.EntityRenderingCache entityRenderingCache
                = new ScreenRenderingContext.EntityRenderingCache();

        public VariantElement(int index, V variant) {
            this.index = index;
            this.variant = variant;
            this.model = EntityUtils.create(EntityUtils.getEntityType(e()), e().level());
            this.model.setYRot(0);
            this.model.setYHeadRot(0);
            this.model.setYBodyRot(0);
            setVariantClient(model, variant);
            this.name = getVariantName(variant);

            getBox().setSize(variantWidth, variantHeight);
        }

        public int getIndex() {
            return index;
        }

        public V getVariant() {
            return variant;
        }

        public E getModel() {
            return model;
        }

        public void setWidthFix(float widthFix) {
            this.widthFix = widthFix;
        }

        public void setHeightFix(float heightFix) {
            this.heightFix = heightFix;
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                if (isAllowedToChoose()) {
                    chosenIndex = index;
                    E m = p().getModel();
                    if (m != null) {
                        p().setNoUpdateCooldown();
                        setVariantClient(m, getChosenVariant());
                    }
                    CompoundTag v = new CompoundTag();
                    CompoundTag e = new CompoundTag();
                    writeVariantToNbt(this, v, e);
                    ClientNetManager.sendUpdatedEntityPropertiesOld(e(), v, e);
                }
            }
            return super.onMouseDown(x, y, code);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);

            if (nameWidth == -1) {
                nameWidth = ctx.calcTextWidth(name) * FONT_SIZE;
            }

            renderEntity(ctx);

            if (isChosen(index)) {
                renderCheckMark(ctx, true);
                if (isInBox(ctx.getElementScreen().getHoveredElement())) {
                    renderVariantName(ctx);
                } else {
                    renderVariantNameAuto(ctx);
                }
            } else if (isInBox(ctx.getElementScreen().getHoveredElement())) {
                if (isAllowedToChoose()) {
                    renderCheckMark(ctx, false);
                }
                renderVariantName(ctx);
            } else {
                renderVariantNameAuto(ctx);
            }
        }

        private void renderCheckMark(ScreenRenderingContext ctx, boolean chosenTrueHoveredFalse) {
            int textureLeft = chosenTrueHoveredFalse ? 23 : 24;
            ctx.renderTexture(Textures.ICONS,
                    textureLeft * Widget.WIDGET_WIDTH, 3 * Widget.WIDGET_HEIGHT,
                    ctx.getZ() + 100,
                    (getBox().getLeft() + getBox().getRight() - Widget.WIDGET_WIDTH) / 2F,
                    getBox().getBottom() - 7 - Widget.WIDGET_HEIGHT,
                    Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
        }

        private void renderVariantNameAuto(ScreenRenderingContext ctx) {
            if (nameWidth > getBox().getWidth()) {
                renderEllipsis(ctx);
            } else {
                renderVariantName(ctx);
            }
        }

        private void renderVariantName(ScreenRenderingContext ctx) {
            renderTheText(ctx, name);
        }

        private void renderEllipsis(ScreenRenderingContext ctx) {
            renderTheText(ctx, Component.literal("..."));
        }

        private void renderTheText(ScreenRenderingContext ctx, Component text) {
            ctx.renderCenteredText(text, Colors.COMMON_DARK_TEXT, FONT_SIZE, ctx.getZ(),
                    (getBox().getLeft() + getBox().getRight()) / 2, getBox().getBottom() - 5);
        }

        private void renderEntity(ScreenRenderingContext ctx) {
            ctx.renderEntityCentered(model, entityRenderingCache,
                    getBox().getLeft() - 1- widthFix / 2,
                    getBox().getTop() + 1 - heightFix,
                    getBox().getRight() + 1 + widthFix / 2,
                    getBox().getBottom() - 6,
                    0F, 0.3F, 1F);
        }
    }

    private static class BackgroundBar extends ScreenElement {
        private final TextureInfo texture;
        private final int textureLeft, textureTop;

        public BackgroundBar(TextureInfo texture, int textureLeft, int textureTop) {
            super(false, false);
            this.texture = texture;
            this.textureLeft = textureLeft;
            this.textureTop = textureTop;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            float currPos = 0;
            float widthLeft = 5;
            ctx.renderTexture(texture, textureLeft, textureTop, ctx.getZ(), getBox().getLeft(), getBox().getTop(), widthLeft, getBox().getHeight());
            currPos += widthLeft;

            float widthMid = getBox().getWidth() - 10;
            while (widthMid > 10) {
                ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop(), 10, getBox().getHeight());
                currPos += 10;
                widthMid -= 10;
            }
            ctx.renderTexture(texture, textureLeft + 5, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop(), widthMid, getBox().getHeight());
            currPos += widthMid;

            float widthRight = 5;
            ctx.renderTexture(texture, textureLeft + 15, textureTop, ctx.getZ(), getBox().getLeft() + currPos, getBox().getTop(), widthRight, getBox().getHeight());
        }
    }
}
