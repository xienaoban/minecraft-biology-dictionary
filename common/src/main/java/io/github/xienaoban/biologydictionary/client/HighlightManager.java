package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.mixin.entity.EntityIMixin;
import io.github.xienaoban.biologydictionary.mixin.entity.FallingBlockEntityIMixin;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.xienaoban.biologydictionary.BiologyDictionaryClient.BDC;

@ClientOnly
public final class HighlightManager {
    private volatile boolean hasHighlighted = false;
    private final Map<Entity, HighlightedEntity> highlightedEntities = new ConcurrentHashMap<>();
    private final List<HighlightedBlock> highlightedBlocks = new CopyOnWriteArrayList<>();

    public void tick() {
        if (!hasHighlighted) { return; }

        int ticks = BDC.getTicks();
        ClientLevel level = ClientUtils.getClientLevel();
        if (level == null) { clear(); return; }

        Context ctx = new Context(level);
        for (Map.Entry<Entity, HighlightedEntity> entry : highlightedEntities.entrySet()) {
            if (entry.getValue().checkEnd(ticks, ctx)) {
                highlightedEntities.remove(entry.getKey());
            }
        }
        highlightedBlocks.removeIf(h -> h.checkEnd(ticks, ctx));
        if (highlightedEntities.isEmpty() && highlightedBlocks.isEmpty()) {
            hasHighlighted = false;
        }
    }

    public void clear() {
        highlightedEntities.clear();
        highlightedBlocks.clear();
        hasHighlighted = false;
    }

    public boolean hasAnyHighlighted() {
        return hasHighlighted;
    }

    public boolean isEntityHighlighted(Entity entity) {
        return highlightedEntities.containsKey(entity);
    }

    public Collection<HighlightedEntity> getHighlightedEntities() {
        return highlightedEntities.values();
    }

    public List<HighlightedBlock> getHighlightedBlocks() {
        return highlightedBlocks;
    }

    public void highlightEntity(Entity entity, int durationTicks) {
        if (!hasHighlighted) { hasHighlighted = true; }
        highlightedEntities.put(entity, new HighlightedEntity(BDC.getTicks() + durationTicks, entity));
    }

    public void highlightBlock(BlockPos blockPos, int durationTicks) {
        highlightBlock(ClientUtils.getClientLevel(), blockPos, durationTicks);
    }

    public void highlightBlock(ClientLevel level, BlockPos blockPos, int durationTicks) {
        if (!hasHighlighted) { hasHighlighted = true; }
        BlockState blockState = level.getBlockState(blockPos);
        highlightedBlocks.add(new HighlightedBlock(BDC.getTicks() + durationTicks, blockState, blockPos, level.dimension()));
    }

    public record Context(ClientLevel level) {}

    public abstract static class HighlightedInstance {
        protected final int endTicks;
        protected boolean ended;

        public HighlightedInstance(int endTicks) {
            this.endTicks = endTicks;
            this.ended = false;
        }

        public final boolean checkEnd(int ticks, Context ctx) {
            if (endTicks < ticks || onCheckEnd(ctx)) {
                ended = true;
            }
            return ended;
        }

        public final void setEnded() {
            ended = true;
        }

        protected abstract boolean onCheckEnd(Context ctx);
    }

    public static final class HighlightedEntity extends HighlightedInstance {
        private final Entity entity;

        public HighlightedEntity(int endTick, Entity entity) {
            super(endTick);
            this.entity = entity;
        }

        public Entity getEntity() {
            return entity;
        }

        @Override
        protected boolean onCheckEnd(Context ctx) {
            return (EntityUtils.getLevel(entity) != ctx.level)
                    || (!entity.isAlive());
        }
    }

    public static final class HighlightedBlock extends HighlightedInstance {
        private final BlockState blockState;
        private final BlockPos blockPos;
        private final ResourceKey<Level> dimension;
        private final FallingBlockEntity fallingBlockEntity;

        public HighlightedBlock(int endTick, BlockState blockState, BlockPos blockPos, ResourceKey<Level> dimension) {
            super(endTick);
            this.blockState = blockState;
            this.blockPos = blockPos;
            this.dimension = dimension;
            this.fallingBlockEntity = createFallingBlockEntity();
        }

        public BlockState getBlockState() {
            return blockState;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public FallingBlockEntity getFallingBlockEntity() {
            return fallingBlockEntity;
        }

        @Override
        protected boolean onCheckEnd(Context ctx) {
            return (dimension != ctx.level.dimension())
                    || (blockState.getBlock() != ctx.level.getBlockState(blockPos).getBlock());
        }

        private FallingBlockEntity createFallingBlockEntity() {
            return new ClientHighlightedBlockEntity(
                    blockPos.getX() + 0.5, blockPos.getY() - 0.001, blockPos.getZ() + 0.5, blockState);
        }
    }

    public static final class ClientHighlightedBlockEntity extends FallingBlockEntity {

        /**
         * @see net.minecraft.world.entity.item.FallingBlockEntity#FallingBlockEntity(net.minecraft.world.level.Level, double, double, double, net.minecraft.world.level.block.state.BlockState)
         */
        public ClientHighlightedBlockEntity(double x, double y, double z, BlockState blockState) {
            super(EntityType.FALLING_BLOCK, ClientUtils.getClientLevel());
            ((FallingBlockEntityIMixin) (Object) this).biologydictionary$setBlockState(blockState);
            blocksBuilding = false;
            setPos(x, y, z);
            setDeltaMovement(Vec3.ZERO);
            xo = x;
            yo = y;
            zo = z;
            setStartPos(this.blockPosition());
            setNoGravity(true);
            setGlowingTag(true);
            setSharedFlag(EntityIMixin.biologydictionary$getFlagGlowing(), true);
        }
    }
}
