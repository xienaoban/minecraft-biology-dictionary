package io.github.xienaoban.biologydictionary.common.util;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class RenderUtils {
    public static void renderBodyOnly(EntityRenderState entityRenderState) {
        entityRenderState.lightCoords = 15728880;
        entityRenderState.hitboxesRenderState = null;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        entityRenderState.leashStates = null;
        entityRenderState.nameTag = null;
    }
}
