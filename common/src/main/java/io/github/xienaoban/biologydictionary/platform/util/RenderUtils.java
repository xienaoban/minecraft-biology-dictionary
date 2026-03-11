package io.github.xienaoban.biologydictionary.platform.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public final class RenderUtils {

    public static <E extends Entity, S extends EntityRenderState> EntityRenderer<E, S> getRenderer(EntityRenderDispatcher renderDispatcher, E entity) {
        return Misc.cast(renderDispatcher.getRenderer(entity));
    }

    public static <E extends Entity, S extends EntityRenderState> EntityRenderState createRenderState(EntityRenderer<E, S> renderer) {
        return renderer.createRenderState();
    }

    public static <E extends Entity, S extends EntityRenderState> void extractRenderState(EntityRenderer<E, S> renderer, E entity, S renderState) {
        extractRenderState(renderer, entity, renderState, 1F);
    }

    public static <E extends Entity, S extends EntityRenderState> void extractRenderState(EntityRenderer<E, S> renderer, E entity, S renderState, float tickDelta) {
        renderer.extractRenderState(entity, renderState, tickDelta);
    }

    public static <E extends Entity, S extends EntityRenderState> S createRenderState(EntityRenderDispatcher renderDispatcher, E entity) {
        return createRenderState(renderDispatcher, entity, 1F);
    }

    public static <E extends Entity, S extends EntityRenderState> S createRenderState(EntityRenderDispatcher renderDispatcher, E entity, float tickDelta) {
        return Misc.cast(renderDispatcher.extractEntity(entity, tickDelta));
    }

    public static void renderBodyOnly(EntityRenderState entityRenderState) {
        entityRenderState.lightCoords = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        entityRenderState.leashStates = null;
        entityRenderState.nameTag = null;
    }
}
