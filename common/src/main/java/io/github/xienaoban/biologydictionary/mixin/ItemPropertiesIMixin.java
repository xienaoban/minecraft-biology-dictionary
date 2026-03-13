package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.client.renderer.item.ItemProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemProperties.class)
public interface ItemPropertiesIMixin {
    @Accessor("TAG_CUSTOM_MODEL_DATA")
    static String biologydictionary$getCustomModelDataTag() {
        throw new AssertionError();
    }
}
