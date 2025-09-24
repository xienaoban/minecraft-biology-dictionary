package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class PlayerSelectorScreen extends AbstractBiologyDictionaryScreen {
    private final AbstractBiologyDictionaryScreen lastScreen;
    private final Consumer<AbstractClientPlayer> callback;

    public PlayerSelectorScreen(AbstractBiologyDictionaryScreen lastScreen, Consumer<AbstractClientPlayer> callback) {
        super(Component.translatable(Lang.SCREEN_PLAYER_SELECTOR));
        this.lastScreen = lastScreen;
        this.callback = callback;
        addBookmark(new ReturnLastScreenBookmark());

        List<Widget> list = new ArrayList<>();
        list.add(new DescriptionWidget(1, Page.COLUMNS, Component.translatable(Lang.TEXT_TIME_IN_BEEHIVE)));
        list.addAll(
                McClientUtils.getClientLevel().players().stream()
                        .filter(p -> p != player)
                        .sorted(Comparator.comparing(o -> o.getName().getString()))
                        .map(PlayerSelectorWidget::new)
                        .toList()
        );
        addAllWidgetsOneByOne(list);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (lastScreen != null) {
            McClientUtils.setScreen(lastScreen);
        }
    }

    public final class ReturnLastScreenBookmark extends Bookmark {
        public ReturnLastScreenBookmark() {
            super(Component.translatable(Lang.BOOKMARK_BACK));
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                onClose();
            }
            return true;
        }
    }

    public final class PlayerSelectorWidget extends Widget {
        private final AbstractClientPlayer plr;

        public PlayerSelectorWidget(AbstractClientPlayer player) {
            super(1, Page.COLUMNS / 2);
            this.plr = player;
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            onClose();
            callback.accept(plr);
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ctx.renderText(plr.getName(), Colors.COMMON_DARK_TEXT, ctx.getZ(), getBox().getLeft() + 1, getBox().getTop() + 2.25F);
        }
    }
}
