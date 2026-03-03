package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class PlayerSelectorScreen extends AbstractBiologyDictionaryScreen {
    private final AbstractBiologyDictionaryScreen lastScreen;
    private final Consumer<AbstractClientPlayer> callback;

    public PlayerSelectorScreen(AbstractBiologyDictionaryScreen lastScreen, Consumer<AbstractClientPlayer> callback) {
        super(TextUtils.translate(Lang.SCREEN_PLAYER_SELECTOR));
        this.lastScreen = lastScreen;
        this.callback = callback;
        addBookmark(new ReturnLastScreenBookmark());

        List<Widget> list = new ArrayList<>();
        list.add(new DescriptionWidget(1, Page.COLUMNS, TextUtils.translate(Lang.SCREEN_PLAYER_SELECTOR_DESC)));
        list.add(new PlayerSelectorWidget(player));
        list.addAll(
                ClientUtils.getClientLevel().players().stream()
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
            ClientUtils.setScreen(lastScreen);
        }
    }

    public final class ReturnLastScreenBookmark extends Bookmark {
        public ReturnLastScreenBookmark() {
            super(TextUtils.translate(Lang.BOOKMARK_BACK));
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
        private final AbstractClientPlayer targetPlayer;

        public PlayerSelectorWidget(AbstractClientPlayer player) {
            super(1, Page.COLUMNS / 2);
            this.targetPlayer = player;
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                onClose();
                callback.accept(targetPlayer);
            }
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            if (isHovered(ctx.getMouseX(), ctx.getMouseY())) {
                ctx.renderRectangle(0x2b90593F, ctx.getZ(), box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
            }
            ctx.renderPlayerFace(targetPlayer, box.getLeft() + 1F, box.getTop() + 1F);
            ctx.renderText(targetPlayer.getName(), Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), box.getLeft() + 11, box.getTop() + 3 + TXT_ASCII_TO);
        }
    }
}
