package io.github.xienaoban.minecraft.biologydictionary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Looks like the max/min x/y/z of ModelPart.Cube are the same as:
 * {@snippet :
 *      float minX = Integer.MAX_VALUE;
 *      float minY = Integer.MAX_VALUE;
 *      float minZ = Integer.MAX_VALUE;
 *      float maxX = Integer.MIN_VALUE;
 *      float maxY = Integer.MIN_VALUE;
 *      float maxZ = Integer.MIN_VALUE;
 *      for (ModelPart.Polygon polygon : cube.polygons) {
 *          for (ModelPart.Vertex v : polygon.vertices()) {
 *              float x = v.pos().x();
 *              float y = v.pos().y();
 *              float z = v.pos().z();
 *              minX = Math.min(minX, x);
 *              minY = Math.min(minY, y);
 *              minZ = Math.min(minZ, z);
 *              maxX = Math.max(maxX, x);
 *              maxY = Math.max(maxY, y);
 *              maxZ = Math.max(maxZ, z);
 *          }
 *      }
 * }
 *
 *
 * @see net.minecraft.client.model.geom.LayerDefinitions
 * @see net.minecraft.client.model.geom.ModelLayers
 * @see net.minecraft.client.model.geom.ModelLayers#DEFAULT_LAYER
 */
@Environment(EnvType.CLIENT)
public final class EntityModelManager {
    public static void getModel(Entity entity) {
        ResourceLocation entityTypeName = EntityUtils.getEntityTypeName(entity);
        ModelLayerLocation modelLayerLocation = new ModelLayerLocation(entityTypeName, "main");

        ModelPart mainModel;
        try {
            mainModel = Minecraft.getInstance().getEntityModels().bakeLayer(modelLayerLocation);

            EntityRenderer<?, ?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            EntityRenderState state = entityRenderer.createRenderState();
            // entityRenderer.render();
            if (entityRenderer instanceof LivingEntityRenderer<?,?,?> livingEntityRenderer) {
                EntityModel<?> model = livingEntityRenderer.getModel();
                AABB box = entity.getBoundingBox();
                AABB box2 = getModelBox(model.root());
                System.out.println("box = "
                        + box.minX + ", " + box.maxX + "; "
                        + box.minY + ", " + box.maxY + "; "
                        + box.minZ + ", " + box.maxZ + "; "
                        + "> " + (box.maxX - box.minX) + ", " + (box.maxY - box.minY) + ", " + (box.maxZ - box.minZ));
                System.out.println("box = "
                        + box2.minX + ", " + box2.maxX + "; "
                        + box2.minY + ", " + box2.maxY + "; "
                        + box2.minZ + ", " + box2.maxZ + "; "
                        + "> " + (box2.maxX - box2.minX) + ", " + (box2.maxY - box2.minY) + ", " + (box2.maxZ - box2.minZ));
                System.out.println("---------1");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        AABB box = entity.getBoundingBox();
        AABB box2 = getModelBox(mainModel);
        System.out.println("box = "
                + box.minX + ", " + box.maxX + "; "
                + box.minY + ", " + box.maxY + "; "
                + box.minZ + ", " + box.maxZ + "; "
                + "> " + (box.maxX - box.minX) + ", " + (box.maxY - box.minY) + ", " + (box.maxZ - box.minZ));
        System.out.println("box = "
                + box2.minX + ", " + box2.maxX + "; "
                + box2.minY + ", " + box2.maxY + "; "
                + box2.minZ + ", " + box2.maxZ + "; "
                + "> " + (box2.maxX - box2.minX) + ", " + (box2.maxY - box2.minY) + ", " + (box2.maxZ - box2.minZ));
        System.out.println("---------");
    }

    private static AABB getModelBox(ModelPart model) {
        PoseStack poseStack = new PoseStack();
        Vector3f min = new Vector3f(Integer.MAX_VALUE);
        Vector3f max = new Vector3f(Integer.MIN_VALUE);

        model.visit(poseStack, (pose, string, i, cube) -> {
            System.out.println("cube \"" + string + "\"");
            Matrix4f matrix = pose.pose();

            Vector3f[] vertices = {
                    new Vector3f(cube.minX, cube.minY, cube.minZ),
                    new Vector3f(cube.maxX, cube.minY, cube.minZ),
                    new Vector3f(cube.minX, cube.maxY, cube.minZ),
                    new Vector3f(cube.maxX, cube.maxY, cube.minZ),
                    new Vector3f(cube.minX, cube.minY, cube.maxZ),
                    new Vector3f(cube.maxX, cube.minY, cube.maxZ),
                    new Vector3f(cube.minX, cube.maxY, cube.maxZ),
                    new Vector3f(cube.maxX, cube.maxY, cube.maxZ),
            };

            for (Vector3f vertex : vertices) {
                Vector3f v = vertex.mulPositionTranslation(matrix);
                min.x = Math.min(min.x, v.x);
                min.y = Math.min(min.y, v.y);
                min.z = Math.min(min.z, v.z);
                max.x = Math.max(max.x, v.x);
                max.y = Math.max(max.y, v.y);
                max.z = Math.max(max.z, v.z);
            }

            System.out.println("cube box = "
                    + cube.minX + ", " + cube.maxX + "; "
                    + cube.minY + ", " + cube.maxY + "; "
                    + cube.minZ + ", " + cube.maxZ);
        });
        System.out.println("final box = "
                + min.x + ", " + max.x + "; "
                + min.y + ", " + max.y + "; "
                + min.z + ", " + max.z);
        System.out.println("final box = "
                + (min.x / 16) + ", " + (max.x / 16) + "; "
                + (min.y / 16) + ", " + (max.y / 16) + "; "
                + (min.z / 16) + ", " + (max.z / 16));
        return new AABB(min.x / 16, min.y / 16, min.z / 16, max.x / 16, max.y / 16, max.z / 16);
    }
}
