package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@ClientOnly
public class BdAboutScreen extends AbstractBiologyDictionaryScreen {
    public BdAboutScreen() {
        super(TextUtils.translate(Lang.BOOKMARK_ABOUT));
        initBookmarks();
        initWidgets();
    }

    private void initBookmarks() {
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initWidgets() {
        List<Widget> widgets = Arrays.asList(
                new ModNameAuthorNameWidget(),
                new ShowFailedCreatedEntityTypesWidget(),
                new GetBookItemWidget(),
                new ToggleDemoModeWidget(),
                new ShowGuiSizeWidget()
        );

        addAllWidgetsOneByOne(widgets);
    }

    private static class ModNameAuthorNameWidget extends Widget {
        protected ModNameAuthorNameWidget() {
            super(2, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            ctx.renderCenteredText(
                    TextUtils.concat(Arrays.asList(TextUtils.translate(Lang.TEXT_MOD_NAME_IS).withStyle(ChatFormatting.BOLD), TextUtils.translate(Lang.MOD_NAME_TWO_LANG))),
                    Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
            ctx.renderCenteredText(
                    TextUtils.concat(Arrays.asList(TextUtils.translate(Lang.TEXT_MOD_AUTHOR_IS).withStyle(ChatFormatting.BOLD), TextUtils.translate(Lang.AUTHOR_NAME_TWO_LANG))),
                    Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 10);
            ctx.renderCenteredText(
                    TextUtils.concat(Arrays.asList(TextUtils.translate(Lang.TEXT_MOD_VERSION_IS).withStyle(ChatFormatting.BOLD), TextUtils.literal(DevUtils.getModVersion()))),
                    Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 18);
        }
    }

    private static class ShowFailedCreatedEntityTypesWidget extends Widget {
        protected ShowFailedCreatedEntityTypesWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            int size = WorldSession.get().getEntityManager().getFailedCreatedEntityTypes().size();
            ctx.renderCenteredText(TextUtils.literal("Failed Created Entity Types: " + size),
                    Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(),
                    (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 3.2F);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            List<Component> tooltip = new ArrayList<>();

            WorldSession.get().getEntityManager().getFailedCreatedEntityTypes().stream()
                    .sorted(Comparator.comparing(EntityUtils::getEntityTypeIdName))
                    .map(type -> TextUtils.concat(Arrays.asList(
                            TextUtils.literal(EntityUtils.getEntityTypeIdName(type)).withStyle(ChatFormatting.GRAY),
                            type.getDescription().copy().withStyle(ChatFormatting.WHITE)
                    ), TextUtils.literal(" - ")))
                    .forEach(tooltip::add);

            if (tooltip.isEmpty()) {
                tooltip.add(TextUtils.translate(Lang.TEXT_EMPTY_WITH_BRACKETS).withStyle(ChatFormatting.GRAY));
            }

            List<FormattedCharSequence> lines = tooltip.stream()
                    .flatMap(c -> {
                        List<FormattedCharSequence> splitLines = ctx.getFont().split(c, Widget.TOOLTIP_WIDTH);
                        return splitLines.isEmpty() ? Stream.of(TextUtils.empty().getVisualOrderText()) : splitLines.stream();
                    })
                    .toList();
            ctx.renderLinedTooltipCentered(lines, 0.5F,
                    (getBox().getLeft() + getBox().getRight()) / 2, getBox().getBottom());
            return true;
        }
    }

    private class GetBookItemWidget extends Widget {
        protected GetBookItemWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (BiologyDictionaryClient.isDebugMode() && PlayerUtils.isCreative(player)) {
                ctx.renderCenteredText(TextUtils.literal("Get Book Item"), 0xFF000000, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
            }
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (BiologyDictionaryClient.isDebugMode() && PlayerUtils.isCreative(player)) {
                onClose();
                ClientNetManager.requestBookItem();
                return true;
            }
            return false;
        }
    }

    private class ToggleDemoModeWidget extends Widget {
        protected ToggleDemoModeWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (BiologyDictionaryClient.isDebugMode()) {
                ctx.renderCenteredText(TextUtils.literal("Toggle Demo Mode"), 0xFF000000, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
            }
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (BiologyDictionaryClient.isDebugMode() && isMouseLeft(button)) {
                boolean enabled = BiologyDictionaryClient.toggleDemoMode();
                sendScreenMessage(TextUtils.literal("Demo mode " + (enabled ? "on" : "off")));
                return true;
            }
            return false;
        }
    }

    private static class ShowGuiSizeWidget extends Widget {
        protected ShowGuiSizeWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (BiologyDictionaryClient.isDebugMode()) {
                ctx.renderCenteredText(TextUtils.literal(ctx.getScreenWidth() + " , " + ctx.getScreenHeight()), 0xFF000000, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 1);
                ctx.renderCenteredText(TextUtils.literal(ctx.getMouseX() + " , " + ctx.getMouseY()), 0xFF000000, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 5.5F);
            }
        }
    }
}
