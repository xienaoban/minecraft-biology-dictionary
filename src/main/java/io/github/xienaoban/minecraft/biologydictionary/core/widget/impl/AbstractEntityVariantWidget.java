package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.common.util.MinecraftUtils;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Page;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Colors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public abstract class AbstractEntityVariantWidget<E extends Entity, V> extends EntityPropertyWidget<E> {
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
    private final List<VariantElement> variants;
    private final int[] displayCntPerRow;
    private final float variantWidth, variantHeight;

    private int chosenIndex;

    private final LocalPlayer player;

    public AbstractEntityVariantWidget(EntityProperties<E> properties, int variantCnt, int maxDisplayCntPerLine, int rowsPerVariant) {
        super(properties, calcRowsAndColumns(variantCnt, maxDisplayCntPerLine, rowsPerVariant));
        player = Objects.requireNonNull(MinecraftUtils.getLocalPlayer());

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
        variantWidth = getBox().getWidth() / maxDisplayCntPerLine - 2;
        variantHeight = getBox().getHeight() / lines - 2;
        this.variants = new ArrayList<>(size());
        for (int i = 0; i < size(); ++ i) {
            VariantElement e = new VariantElement(i, variants.get(i));
            e.setParent(this);
            this.variants.add(e);
        }

        chosenIndex = 0;
        updateChosenVariant();
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

    protected abstract void writeVariantToNbt(CompoundTag vanillaNbt, CompoundTag extraNbt);

    protected abstract V readVariantFromNbt(CompoundTag vanillaNbt, CompoundTag extraNbt);

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
        return variants.get(chosenIndex).variant;
    }

    public final boolean isChosen(int variantIndex) {
        return variantIndex == getChosenIndex();
    }

    public final boolean isChosen(V variant) {
        return equals(variant, getChosenVariant());
    }

    private void updateChosenVariant() {
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
            if (equals(variant, variants.get(i).variant)) {
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
            int cnt = displayCntPerRow[i];
            final float mWidth = wWidth / cnt;
            for (int j = 0; j < cnt; ++j) {
                float left = wLeft + j * mWidth + mWidth / 2 - variantWidth / 2;
                float top = wTop + i * mHeight + mHeight / 2 - variantHeight / 2;
                variants.get(vIdx).getBox().setPosition(left, top);
                ++vIdx;
            }
        }
    }

    public class VariantElement extends ScreenElement {
        private final int index;

        private final V variant;
        private final E model;
        private final Component name;

        public VariantElement(int index, V variant) {
            this.index = index;
            this.variant = variant;
            this.model = EntityUtils.create(EntityUtils.getEntityType(e()), e().level());
            setVariantClient(model, variant);
            this.name = getVariantName(variant);

            getBox().setSize(variantWidth, variantHeight);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            final float left = getBox().getLeft();
            final float top = getBox().getTop();
            final float right = getBox().getRight();
            final float bottom = getBox().getBottom();

            if (isAllowedToChoose() && contains(ctx.getElementScreen().getFocusedElement())) {
                ctx.renderRectangle(0xAAE8C8B1, ctx.getZ(), left, top, right, bottom);
            }

            renderEntity(ctx, model, left , top, right, bottom - 6);
            ctx.renderCenteredText(name, Colors.COMMON_DARK_TEXT, 0.5F, (left + right) / 2, bottom - 6);

            if (isChosen(index)) {
                ctx.renderCenteredText(name, Colors.COMMON_DARK_TEXT, 0.5F, (left + right) / 2, bottom - 3);
            }
        }

        private void renderEntity(ScreenRenderingContext ctx, E entity, float left, float top, float right, float bottom) {
            ctx.renderEntity(entity, left, top, right, bottom, 0F, 0.25F, true);
        }
    }
}
