package io.github.xienaoban.minecraft.biologydictionary.gui.screen.misc;

import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.ElementScreen;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.util.MinecraftUtils;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

public class BeehiveScreen extends ElementScreen {
    private static final int[][] LATTICES = {{0, 0}, {32, 0}, {0, 50}, {32, 50}, {16, 25}, {-16, 25}, {48, 25}};
    private static final int MAX_HONEY_CNT = BeehiveBlock.MAX_HONEY_LEVELS;
    private static final int MAX_BEE_CNT = BeehiveBlockEntity.MAX_OCCUPANTS;

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
        super(MinecraftUtils.getLocalLevel().getBlockState(pos).getBlock().getName());
        this.pos = pos;
        this.level = MinecraftUtils.getLocalLevel();
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
        long lastMills = this.mills;
        this.mills = System.currentTimeMillis();
        int diff = (int) (this.mills - lastMills);
        renderBlurredBackground(ctx);
        ctx.getGuiGraphics().pose().pushPose();
        // RenderSystem.setShader(GameRenderer::getPositionTexShader);
        // RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        // RenderSystem.setShaderTexture(0, Textures.BEEHIVE);
        int w = (this.width - 128) >> 1;
        int h = (this.height - 128) >> 1;

        int beeCnt = this.blockBeeCnt;
        int honeyCnt = this.blockHoneyCnt;

        if (this.lastBeeCnt > beeCnt) {
            this.lastBeeCnt = beeCnt;
            BeeAction tmp = this.actions[0];
            for (int i = 1; i < MAX_BEE_CNT; ++i) {
                this.actions[i - 1] = this.actions[i];
            }
            this.actions[MAX_BEE_CNT - 1] = tmp;
        }
        else {
            this.lastBeeCnt = beeCnt;
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
            BeeAction action = this.actions[i];
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
            BeeInfo bee = this.bees[i];
            int beeSize = bee.entity.isBaby() ? 46 : 32;
            float t = 14.0F * Math.min(bee.ticksInHive, bee.minTicksInHive) / bee.minTicksInHive;
            ctx.renderHorizontalLine(0xFF443300, 2.2F, ctx.getZ(), y - 1, x - 7.5F, x + 7.5F);
            ctx.renderHorizontalLine(bee.entity.hasNectar() ? 0xFFFFBB00 : 0x64FFBB00, 1.2F, ctx.getZ(), y - 1, x - 7, x - 7 + t);
            ctx.renderEntity(bee.entity, x, y, beeSize, (float) Math.atan(action.mouseX / 80), (float) Math.atan(action.mouseY / 80), true);
            Component customName = bee.entity.getCustomName();
            int beeTop = y - (bee.entity.isBaby() ? 20 : 25);
            if (customName != null) {
                int wHalf = (ctx.calcTextWidth(customName) >> 2) + 1;
                ctx.renderHorizontalLine(0x55777777, 6, ctx.getZ(), beeTop, x - wHalf, x + wHalf);
                ctx.renderCenteredText(bee.entity.getCustomName(), color, 0.5F, x, beeTop - 2);
            }
            if (ctx.getMouseX() > x - 10 && ctx.getMouseX() < x + 10 && ctx.getMouseY() > beeTop && ctx.getMouseY() < y) {
                List<Component> texts = List.of(
                        bee.entity.getName(),
                        Component.translatable(TranslationKeys.TEXT_BEE_STATE_IN_BEEHIVE, Component.translatable(bee.entity.hasNectar() ? TranslationKeys.TEXT_BEE_PRODUCING_NECTAR : TranslationKeys.TEXT_BEE_RESTING)).withStyle(ChatFormatting.GRAY),
                        Component.translatable(TranslationKeys.TEXT_TIME_IN_BEEHIVE, (bee.ticksInHive / 20) + "s/" + (bee.minTicksInHive / 20) + "s").withStyle(ChatFormatting.GRAY)
                );
                int maxLength = texts.stream().mapToInt(ctx::calcTextWidth).max().getAsInt();
                ctx.getGuiGraphics().renderTooltip(ctx.getFont(), texts.stream().map(Component::getVisualOrderText).toList(), x - (maxLength + 20) / 2, y + 18);
            }
        }
        ctx.renderText(Component.literal(honeyCnt + "/" + MAX_HONEY_CNT), color, LATTICES[5][0] + lw + 16 - 8.5F, LATTICES[5][1] + lh + 8);
        ctx.renderText(Component.literal(beeCnt + "/" + MAX_BEE_CNT), color, LATTICES[6][0] + lw + 16 - 8.5F, LATTICES[6][1] + lh + 8);
        ctx.renderCenteredText(Component.translatable(TranslationKeys.TEXT_HONEY), color, LATTICES[5][0] + lw + 16.5F, LATTICES[5][1] + lh + 16);
        ctx.renderCenteredText(EntityType.BEE.getDescription(), color, LATTICES[6][0] + lw + 16.5F, LATTICES[6][1] + lh + 16);
        ctx.getGuiGraphics().pose().popPose();
    }

    private void drawLattice(ScreenRenderingContext ctx, int w, int h, int type) {
        ctx.renderTexture(Textures.BEEHIVE, 16 + type * 32, 140, 0, w, h, 32, 32);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappingManager.OPEN_BIOLOGY_DICTIONARY_SCREEN.matches(keyCode, scanCode)
                || Objects.requireNonNull(minecraft).options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        passedClientTickCount++;
        if (passedClientTickCount % 5 == 0) {
            if (!(this.level.getBlockState(pos).getBlock() instanceof BeehiveBlock)) {
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
            CompoundTag newTag = new CompoundTag();
            beeInfo.entity.saveWithoutId(newTag);
            beeInfo.entity.load(newTag.merge(occupant.entityData().copyTag()));

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

        public BeeInfo(Level level) {
            this.entity = EntityUtils.create(EntityType.BEE, level);
            this.ticksInHive = -1;
            this.minTicksInHive = -1;
        }
    }

    private static class BeeAction {
        public float mouseX, mouseY;
        private float speedMouseX, speedMouseY;
        private int mouseMoveTime, mouseCooldownTime;

        private final Random random;

        public BeeAction () {
            this.random = new Random();
            this.mouseX = this.random.nextFloat(-50, 50);
            this.mouseY = this.random.nextFloat(-1, 20);
            this.mouseCooldownTime = this.random.nextInt(3 * 1000);
        }

        public void run(int mills) {
            if (this.mouseMoveTime > 0) {
                this.mouseMoveTime -= mills;
                this.mouseX += this.speedMouseX * mills;
                this.mouseY += this.speedMouseY * mills;
                if (Math.abs(this.mouseX) > 50) this.speedMouseX = -0.2F * this.speedMouseX;
                if (this.mouseY > 20 || this.mouseY < 2) this.speedMouseY = -0.2F * this.speedMouseY;
            }
            else {
                this.mouseCooldownTime -= mills;
                if (this.mouseCooldownTime < 0) {
                    this.mouseCooldownTime = this.random.nextInt(5 * 1000);
                    this.mouseMoveTime = this.random.nextInt(1000);
                    this.speedMouseX = this.random.nextFloat(-0.3F, 0.3F);
                    this.speedMouseY = this.random.nextFloat(-0.1F, 0.1F);
                }
            }
        }
    }
}
