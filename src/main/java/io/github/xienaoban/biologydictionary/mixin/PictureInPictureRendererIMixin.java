package io.github.xienaoban.biologydictionary.mixin;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PictureInPictureRenderer.class)
public interface PictureInPictureRendererIMixin {
    @Accessor
    GpuTexture getTexture();
}
