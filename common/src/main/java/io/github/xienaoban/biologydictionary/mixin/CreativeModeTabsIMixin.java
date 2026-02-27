package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsIMixin {
    @Accessor("TOOLS_AND_UTILITIES")
    static ResourceKey<CreativeModeTab> getToolsAndUtilities() {
        throw new AssertionError();
    }

}
