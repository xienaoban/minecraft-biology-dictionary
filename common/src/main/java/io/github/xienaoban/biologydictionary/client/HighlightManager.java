package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.xienaoban.biologydictionary.BiologyDictionaryClient.BDC;

@Environment(EnvType.CLIENT)
public final class HighlightManager {
    private volatile boolean hasHighlighted = false;
    private final List<HighlightedEntity> highlightedEntities = new CopyOnWriteArrayList<>();
    private final List<HighlightedBlock> highlightedBlocks = new CopyOnWriteArrayList<>();

    void tick() {
        if (!hasHighlighted) { return; }

        int ticks = BDC.getTicks();
        ClientLevel level = ClientUtils.getClientLevel();
        Context ctx = new Context(level);
        highlightedEntities.removeIf(h -> h.checkEnd(ticks, ctx));
        highlightedBlocks.removeIf(h -> h.checkEnd(ticks, ctx));
        if (highlightedEntities.isEmpty() && highlightedBlocks.isEmpty()) {
            hasHighlighted = false;
        }
    }

    public boolean hasAnyHighlighted() {
        return hasHighlighted;
    }

    public List<HighlightedEntity> getHighlightedEntities() {
        return highlightedEntities;
    }

    public List<HighlightedBlock> getHighlightedBlocks() {
        return highlightedBlocks;
    }

    public void highlightEntity(Entity entity, int durationTicks) {
        if (!hasHighlighted) { hasHighlighted = true; }
        highlightedEntities.add(new HighlightedEntity(BDC.getTicks() + durationTicks, entity));
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

    public static abstract class HighlightedInstance {
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

        public HighlightedBlock(int endTick, BlockState blockState, BlockPos blockPos, ResourceKey<Level> dimension) {
            super(endTick);
            this.blockState = blockState;
            this.blockPos = blockPos;
            this.dimension = dimension;
        }

        public BlockState getBlockState() {
            return blockState;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        @Override
        protected boolean onCheckEnd(Context ctx) {
            return (dimension != ctx.level.dimension())
                    || (blockState.getBlock() != ctx.level.getBlockState(blockPos).getBlock());
        }
    }
}
