package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiTextRenderState.class)
public interface GuiTextRenderStateIMixin {
    @Accessor
    Font getFont();

    @Accessor
    FormattedCharSequence getText();

    @Accessor
    Matrix3x2fc getPose();

    @Accessor
    int getColor();

    @Accessor
    int getBackgroundColor();

    @Accessor
    boolean getDropShadow();

    @Accessor
    boolean getIncludeEmpty();

    @Accessor
    ScreenRectangle getScissor();

    @Accessor
    Font.PreparedText getPreparedText();

    @Accessor
    void setPreparedText(Font.PreparedText text);

    @Accessor
    ScreenRectangle getBounds();

    @Accessor
    void setBounds(ScreenRectangle bounds);
}
