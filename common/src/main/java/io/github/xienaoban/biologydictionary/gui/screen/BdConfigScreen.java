package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.config.ClothConfigScreenProvider;
import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.widget.TurnPageTriggerWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;

import java.util.*;

@Environment(EnvType.CLIENT)
public class BdConfigScreen extends AbstractBiologyDictionaryScreen {
    public BdConfigScreen() {
        super(TextUtils.translate(Lang.BOOKMARK_CONFIG));
        initBookmarks();
        initWidgets();
    }

    private void initBookmarks() {
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initWidgets() {
        List<Widget> widgets = new ArrayList<>();

        widgets.add(new DescriptionWidget(1, Page.COLUMNS, TextUtils.translate(Lang.TEXT_LOCAL_CONFIGS_DESC)));
        widgets.add(new OpenLocalConfigsScreenWidget());
        widgets.add(new ReloadLocalConfigsScreenWidget());

        widgets.add(new PlaceHolderWidget(1, Page.COLUMNS));
        widgets.add(new TurnPageTriggerWidget());
        widgets.add(new DescriptionWidget(1, Page.COLUMNS, TextUtils.translate(Lang.TEXT_SERVER_CONFIGS_DESC)));
        Configs.ServerConfigs serverConfigs = ConfigsManager.getServer();
        ConfigsManager.forEachConfigEntryInCategory(serverConfigs,
                entryInfo -> widgets.add(new ConfigWidget(entryInfo)));

        addAllWidgetsOneByOne(widgets);
    }

    /**
     * Widget that opens the local Cloth Config screen when clicked.
     */
    public static class OpenLocalConfigsScreenWidget extends Widget {
        public OpenLocalConfigsScreenWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            boolean hovered = ctx.getElementScreen().getHoveredElement() == this;

            int tt = hovered ? 22 : 23;
            int ww = Widget.WIDGET_WIDTH, wh = Widget.WIDGET_HEIGHT;
            ctx.renderTexture(Textures.ICONS, 8 * ww, tt * wh,
                    ctx.getZ(), box.getLeft(), box.getTop(), 3 * ww, wh);
            int tw = 3 * ww, th = 1 * wh;
            ctx.renderTexture(Textures.ICONS,
                    8 * ww, tt * ww, 8 * ww + tw, tt * ww + th,
                    ctx.getZ(), box.getRight(), box.getBottom(), box.getRight() - tw, box.getBottom() - th);

            int color = hovered ? Colors.BLACK : Colors.COMMON_DARK_TEXT;
            ctx.renderCenteredText(TextUtils.translate(Lang.TEXT_OPEN_LOCAL_CONFIGS_SCREEN),
                    color, 0.5F, ctx.getZ(), (box.getLeft() + box.getRight()) / 2, box.getTop() + 3 + TXT_TO);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                ClientUtils.playScreenSound(ClientUtils.getClient(), SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                ClientUtils.setScreen(ClientUtils.getClient(),
                        ClothConfigScreenProvider.provideScreen(ClientUtils.getCurrentScreen()));
                return true;
            }
            return super.onMouseDown(x, y, code);
        }
    }

    public static class ReloadLocalConfigsScreenWidget extends Widget {
        public ReloadLocalConfigsScreenWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            boolean hovered = ctx.getElementScreen().getHoveredElement() == this;

            int tt = hovered ? 21 : 24;
            int ww = Widget.WIDGET_WIDTH, wh = Widget.WIDGET_HEIGHT;
            ctx.renderTexture(Textures.ICONS, 8 * ww, tt * wh,
                    ctx.getZ(), box.getLeft(), box.getTop(), 3 * ww, wh);
            int tw = 3 * ww, th = 1 * wh;
            ctx.renderTexture(Textures.ICONS,
                    8 * ww, tt * ww, 8 * ww + tw, tt * ww + th,
                    ctx.getZ(), box.getRight(), box.getBottom(), box.getRight() - tw, box.getBottom() - th);

            int color = hovered ? Colors.BLACK : Colors.COMMON_DARK_TEXT;
            ctx.renderCenteredText(TextUtils.translate(Lang.TEXT_RELOAD_LOCAL_CONFIGS_SCREEN),
                    color, 0.5F, ctx.getZ(), (box.getLeft() + box.getRight()) / 2, box.getTop() + 3 + TXT_TO);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                synchronized (ReloadLocalConfigsScreenWidget.class) {
                    ConfigsManager.load();
                }
                if (AbstractBiologyDictionaryScreen.current() instanceof BdConfigScreen screen) {
                    screen.sendScreenMessage(TextUtils.translate(Lang.GUI_OK));
                }
                return true;
            }
            return super.onMouseDown(x, y, code);
        }
    }

    /**
     * Widget that displays an activated server config entry in read-only format.
     * Shows the translated config name on the left and the current value on the right.
     */
    public static class ConfigWidget extends Widget {
        private final ConfigsManager.ConfigEntryInfo entryInfo;
        private final MutableComponent configText;
        private final MutableComponent configTooltipText;

        public ConfigWidget(ConfigsManager.ConfigEntryInfo entryInfo) {
            super(1, Page.COLUMNS);
            this.entryInfo = entryInfo;
            this.configText = TextUtils.translate(Configs.getConfigNameTranslationKey(entryInfo.getName()));
            this.configTooltipText = TextUtils.translate(Configs.getConfigNameTranslationKey(entryInfo.getName()) + ".tooltip");
            setSelectable(false);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            int ww = Widget.WIDGET_WIDTH, wh = Widget.WIDGET_HEIGHT;
            ctx.renderTexture(Textures.ICONS, 7 * ww, 21 * wh,
                    ctx.getZ(), box.getLeft(), box.getTop(), 4 * ww, wh);
            int tw = 3 * ww, th = 1 * wh;
            ctx.renderTexture(Textures.ICONS,
                    8 * ww, 21 * ww, 8 * ww + tw, 21 * ww + th,
                    ctx.getZ(), box.getRight(), box.getBottom(), box.getRight() - tw, box.getBottom() - th);

            // Render translated config name on the left
            ctx.renderText(configText, Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(),
                    box.getLeft() + 12, box.getTop() + 3.2F);

            // Render current value on the right
            Component valueText = formatValue(entryInfo.getValue(ConfigsManager.getServer()));
            ctx.renderRightAlignedText(valueText, Colors.BLACK, 0.5F, ctx.getZ(),
                    box.getRight() - 2, box.getTop() + 3.2F);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            List<Component> list = Arrays.asList(
                    configText.withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
                    configTooltipText.withStyle(ChatFormatting.GRAY)
            );
            ctx.renderComponentTooltip(list, 0.5F, getBox().getLeft(), getBox().getBottom() + 1);
            return true;
        }

        private static Component formatValue(Object value) {
            return switch (value) {
                case null -> TextUtils.translate(Lang.TEXT_NONE_WITH_BRACKETS);
                case Boolean b -> TextUtils.translate(b ? Lang.GUI_YES : Lang.GUI_NO);
                case Enum<?> e -> TextUtils.translate(Configs.getEnumValueTranslationKey(e));
                case Collection<?> s -> TextUtils.literal(String.valueOf(s.size()));
                case Map<?, ?> s -> TextUtils.literal(String.valueOf(s.size()));
                default -> TextUtils.literal(String.valueOf(value));
            };
        }
    }
}
