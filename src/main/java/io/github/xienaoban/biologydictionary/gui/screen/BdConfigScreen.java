package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.config.ClothConfigScreenProvider;
import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BdConfigScreen extends AbstractBiologyDictionaryScreen {
    public BdConfigScreen() {
        super(Component.translatable(Lang.BOOKMARK_CONFIG));
        initBookmarks();
        initWidgets();
    }

    private void initBookmarks() {
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initWidgets() {
        List<Widget> widgets = new ArrayList<>();

        widgets.add(new DescriptionWidget(1, Page.COLUMNS, Component.translatable(Lang.TEXT_LOCAL_CONFIGS_DESC)));
        widgets.add(new OpenLocalConfigsScreenWidget());

        widgets.add(new PlaceHolderWidget(1, Page.COLUMNS));
        widgets.add(new DescriptionWidget(1, Page.COLUMNS, Component.translatable(Lang.TEXT_SERVER_CONFIGS_DESC)));
        Configs.ServerConfigs serverConfigs = ConfigsManager.getServer();
        Configs.forEachConfigEntryInCategory(serverConfigs,
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
            ctx.renderCenteredText(Component.translatable(Lang.TEXT_OPEN_LOCAL_CONFIGS_SCREEN),
                    color, 0.5F, ctx.getZ(), (box.getLeft() + box.getRight()) / 2, box.getTop() + 3);
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

    /**
     * Widget that displays an activated server config entry in read-only format.
     * Shows the translated config name on the left and the current value on the right.
     */
    public static class ConfigWidget extends Widget {
        private final Configs.ConfigEntryInfo entryInfo;
        private final Component configText;

        public ConfigWidget(Configs.ConfigEntryInfo entryInfo) {
            super(1, Page.COLUMNS);
            this.entryInfo = entryInfo;
            this.configText = Component.translatable(Configs.getConfigNameTranslationKey(entryInfo.getName()));
            setSelectable(false);
            setHoverable(false);
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
                    box.getLeft() + 12, box.getTop() + 3);

            // Render current value on the right
            Component valueText = formatValue(entryInfo.getValue(ConfigsManager.getServer()));
            ctx.renderRightAlignedText(valueText, Colors.BLACK, 0.5F, ctx.getZ(),
                    box.getRight() - 2, box.getTop() + 3);
        }

        private static Component formatValue(Object value) {
            return switch (value) {
                case null -> Component.translatable(Lang.TEXT_NONE_WITH_BRACKETS);
                case Boolean b -> Component.translatable(b ? Lang.GUI_YES : Lang.GUI_NO);
                case Enum<?> e -> Component.translatable(Configs.getEnumValueTranslationKey(e));
                case Collection<?> s -> Component.literal(String.valueOf(s.size()));
                default -> Component.literal(String.valueOf(value));
            };
        }
    }
}
