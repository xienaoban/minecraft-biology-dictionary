package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplate.class)
public interface StructureTemplateIMixin {
    @Accessor("entityInfoList")
    List<StructureTemplate.StructureEntityInfo> biologydictionary$getEntityInfoList();

    @Accessor("palettes")
    List<StructureTemplate.Palette> biologydictionary$getPalettes();
}
