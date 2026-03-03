package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.biologydictionary.common.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

public class BeehiveScreen extends ElementScreen {
    private static final int[][] LATTICES = {{0, 0}, {32, 0}, {0, 50}, {32, 50}, {16, 25}, {-16, 25}, {48, 25}};
    private static final int MAX_HONEY_CNT = BeehiveBlock.MAX_HONEY_LEVELS;
    private static final int MAX_BEE_CNT = BeehiveBlockEntity.MAX_OCCUPANTS;

    protected final Minecraft client = ClientUtils.getClient();

    private final BlockPos pos;
    private final Level level;
    private final BeehiveBlockEntity entity;

    private final BeeInfo[] bees;
    protected int blockBeeCnt;
    protected int blockHoneyCnt;

    private final BeeAction[] actions;
    private int lastBeeCnt;
    private long mills;

    private int passedClientTickCount = 0;

    public BeehiveScreen(BlockPos pos) {
        super(ClientUtils.getClientLevel().getBlockState(pos).getBlock().getName());
        this.pos = pos;
        this.level = ClientUtils.getClientLevel();
        this.entity = (BeehiveBlockEntity) this.level.getBlockEntity(pos);
        Objects.requireNonNull(this.entity);

        this.bees = IntStream.range(0, MAX_BEE_CNT).mapToObj(v -> new BeeInfo(this.level)).toArray(BeeInfo[]::new);
        this.blockBeeCnt = 0; // this.entity.getOccupantCount() is not useless on client
        this.blockHoneyCnt = BeehiveBlockEntity.getHoneyLevel(this.entity.getBlockState());

        this.actions = IntStream.range(0, MAX_BEE_CNT).mapToObj(v -> new BeeAction()).toArray(BeeAction[]::new);
        this.lastBeeCnt = 0;
        this.mills = System.currentTimeMillis();
    }

    @Override
    protected void resizeBox(int width, int height) {}

    @Override
    protected void render(ScreenRenderingContext ctx) {
        super.render(ctx);
        long lastMills = mills;
        mills = System.currentTimeMillis();
        int diff = (int) (mills - lastMills);
        renderTransparentBackground(ctx);
        ctx.getGuiGraphics().pose().pushMatrix();
        int w = (width - 128) >> 1;
        int h = (height - 128) >> 1;

        int beeCnt = blockBeeCnt;
        int honeyCnt = blockHoneyCnt;

        if (lastBeeCnt > beeCnt) {
            lastBeeCnt = beeCnt;
            BeeAction tmp = actions[0];
            for (int i = 1; i < MAX_BEE_CNT; ++i) {
                actions[i - 1] = actions[i];
            }
            actions[MAX_BEE_CNT - 1] = tmp;
        }
        else {
            lastBeeCnt = beeCnt;
        }

        int lw = w + 32, lh = h + 23;
        for (int i = 0; i < MAX_HONEY_CNT; ++i) {
            drawLattice(ctx, LATTICES[i][0] + lw, LATTICES[i][1] + lh, i < honeyCnt ? 2 : 0);
        }
        drawLattice(ctx, LATTICES[5][0] + lw, LATTICES[5][1] + lh, 1);
        drawLattice(ctx, LATTICES[6][0] + lw, LATTICES[6][1] + lh, 1);
        ctx.renderTexture(Textures.BEEHIVE, 0, 0, 0, w, h, 128, 128);
        int color = 0xBCFFFFFF;
        for (int i = 0; i < beeCnt; ++i) {
            BeeAction action = actions[i];
            action.run(diff);
            int x, y;
            if (honeyCnt + i < MAX_HONEY_CNT) {
                x = LATTICES[honeyCnt + i][0] + lw + 16;
                y = LATTICES[honeyCnt + i][1] + lh + 29;
            }
            else {
                int p = i + honeyCnt - MAX_HONEY_CNT + 1;
                x = w + p * 32;
                y = h + 24 + ((p & 1) == 0 ? 0 : 8);
            }
            BeeInfo bee = bees[i];
            float beeScale = bee.entity.isBaby() ? 0.6F : 1F;
            float t = 14.0F * Math.min(bee.ticksInHive, bee.minTicksInHive) / bee.minTicksInHive;
            ctx.renderHorizontalLine(0xFF443300, 2.2F, ctx.getZ(), y - 1, x - 7.5F, x + 7.5F);
            ctx.renderHorizontalLine(bee.entity.hasNectar() ? 0xFFFFBB00 : 0x64FFBB00,
                    1.2F, ctx.getZ(), y - 1, x - 7, x - 7 + t);
            ctx.renderEntityCentered(bee.entity, bee.entityRenderingCache,
                    x - 14F, y - 26F, x + 14F, y,
                    (float) Math.atan(action.mouseX / 80), (float) Math.atan(action.mouseY / 80), beeScale);
            Component customName = bee.entity.getCustomName();
            int beeTop = y - (bee.entity.isBaby() ? 20 : 25);
            if (customName != null) {
                int wHalf = (ctx.calcTextWidth(customName) >> 2) + 1;
                ctx.renderHorizontalLine(0x55777777, 6, ctx.getZ(), beeTop, x - wHalf, x + wHalf);
                ctx.renderCenteredText(bee.entity.getCustomName(), color, 0.5F, ctx.getZ(), x, beeTop - 2);
            }
            if (ctx.getMouseX() > x - 10 && ctx.getMouseX() < x + 10 && ctx.getMouseY() > beeTop && ctx.getMouseY() < y) {
                List<Component> texts = Arrays.asList(
                        bee.entity.getName(),
                        TextUtils.translate(Lang.TEXT_BEE_STATE_IN_BEEHIVE, TextUtils.translate(bee.entity.hasNectar() ? Lang.TEXT_BEE_PRODUCING_NECTAR : Lang.TEXT_BEE_RESTING)).withStyle(ChatFormatting.GRAY),
                        TextUtils.translate(Lang.TEXT_TIME_IN_BEEHIVE, (bee.ticksInHive / 20) + "s/" + (bee.minTicksInHive / 20) + "s").withStyle(ChatFormatting.GRAY)
                );
                ctx.renderComponentTooltipCenteredForNextFrameVanilla(texts, x, y + 18F);
            }
        }
        ctx.renderText(TextUtils.literal(honeyCnt + "/" + MAX_HONEY_CNT), color, ctx.getZ(), LATTICES[5][0] + lw + 16 - 8.5F, LATTICES[5][1] + lh + 8);
        ctx.renderText(TextUtils.literal(beeCnt + "/" + MAX_BEE_CNT), color, ctx.getZ(), LATTICES[6][0] + lw + 16 - 8.5F, LATTICES[6][1] + lh + 8);
        ctx.renderCenteredText(TextUtils.translate(Lang.TEXT_HONEY), color, ctx.getZ(), LATTICES[5][0] + lw + 16.5F, LATTICES[5][1] + lh + 16);
        ctx.renderCenteredText(EntityType.BEE.getDescription(), color, ctx.getZ(), LATTICES[6][0] + lw + 16.5F, LATTICES[6][1] + lh + 16);
        ctx.getGuiGraphics().pose().popMatrix();
    }

    private void drawLattice(ScreenRenderingContext ctx, int w, int h, int type) {
        ctx.renderTexture(Textures.BEEHIVE, 16 + type * 32, 140, 0, w, h, 32, 32);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (KeyMappingManager.OPEN_BIOLOGY_DICTIONARY_SCREEN.matches(keyEvent)
                || client.options.keyInventory.matches(keyEvent)) {
            onClose();
            return true;
        } else if (KeyMappingManager.TOGGLE_DEBUG.matches(keyEvent)) {
            screenRenderingContext.setDebug(!screenRenderingContext.isDebug());
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public void tick() {
        super.tick();
        passedClientTickCount++;
        if (passedClientTickCount % 5 == 0) {
            if (!(level.getBlockState(pos).getBlock() instanceof BeehiveBlock)) {
                onClose();
                return;
            }
            if (passedClientTickCount % 10 == 5) {
                ClientNetManager.requestBeehiveInfo(pos);
            }
        }
    }

    public void updateBeeInfo(List<BeehiveBlockEntity.Occupant> occupants) {
        for (int i = 0; i < occupants.size(); ++i) {
            BeehiveBlockEntity.Occupant occupant = occupants.get(i);
            BeeInfo beeInfo = bees[i];

            // @see net.minecraft.server.commands.data.DataCommands.register
            beeInfo.entity.setCustomName(null);
            CompoundTag newTag = EntityUtils.getNbt(beeInfo.entity);
            EntityUtils.setNbt(beeInfo.entity, newTag.merge(occupant.entityData().copyTagWithoutId()));

            beeInfo.ticksInHive = occupant.ticksInHive();
            beeInfo.minTicksInHive = occupant.minTicksInHive();
        }
        blockBeeCnt = occupants.size();
        blockHoneyCnt = BeehiveBlockEntity.getHoneyLevel(entity.getBlockState());
    }

    private static class BeeInfo {
        public final Bee entity;
        public int ticksInHive;
        public int minTicksInHive;

        private final ScreenRenderingContext.EntityRenderingCache entityRenderingCache
                = new ScreenRenderingContext.EntityRenderingCache();

        public BeeInfo(Level level) {
            entity = EntityUtils.create(EntityType.BEE, level);
            ticksInHive = -1;
            minTicksInHive = -1;
        }
    }

    private static class BeeAction {
        public float mouseX, mouseY;
        private float speedMouseX, speedMouseY;
        private int mouseMoveTime, mouseCooldownTime;

        private final Random random;

        public BeeAction () {
            random = new Random();
            mouseX = random.nextFloat(-50, 50);
            mouseY = random.nextFloat(-1, 20);
            mouseCooldownTime = random.nextInt(3 * 1000);
        }

        public void run(int mills) {
            if (mouseMoveTime > 0) {
                mouseMoveTime -= mills;
                mouseX += speedMouseX * mills;
                mouseY += speedMouseY * mills;
                if (Math.abs(mouseX) > 50) speedMouseX = -0.2F * speedMouseX;
                if (mouseY > 20 || mouseY < 2) speedMouseY = -0.2F * speedMouseY;
            }
            else {
                mouseCooldownTime -= mills;
                if (mouseCooldownTime < 0) {
                    mouseCooldownTime = random.nextInt(5 * 1000);
                    mouseMoveTime = random.nextInt(1000);
                    speedMouseX = random.nextFloat(-0.3F, 0.3F);
                    speedMouseY = random.nextFloat(-0.1F, 0.1F);
                }
            }
        }
    }
}
