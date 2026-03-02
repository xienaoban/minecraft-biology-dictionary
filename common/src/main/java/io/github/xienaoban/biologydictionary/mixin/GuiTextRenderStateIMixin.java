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
    @Accessor("font")
    Font biologydictionary$getFont();

    @Accessor("text")
    FormattedCharSequence biologydictionary$getText();

    @Accessor("pose")
    Matrix3x2fc biologydictionary$getPose();

    @Accessor("color")
    int biologydictionary$getColor();

    @Accessor("backgroundColor")
    int biologydictionary$getBackgroundColor();

    @Accessor("dropShadow")
    boolean biologydictionary$getDropShadow();

    @Accessor("includeEmpty")
    boolean biologydictionary$getIncludeEmpty();

    @Accessor("scissor")
    ScreenRectangle biologydictionary$getScissor();

    @Accessor("preparedText")
    Font.PreparedText biologydictionary$getPreparedText();

    @Accessor("preparedText")
    void biologydictionary$setPreparedText(Font.PreparedText text);

    @Accessor("bounds")
    ScreenRectangle biologydictionary$getBounds();

    @Accessor("bounds")
    void biologydictionary$setBounds(ScreenRectangle bounds);
}
