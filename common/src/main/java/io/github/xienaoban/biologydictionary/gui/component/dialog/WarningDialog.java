package io.github.xienaoban.biologydictionary.gui.component.dialog;

import io.github.xienaoban.biologydictionary.gui.component.LongButton;
import io.github.xienaoban.biologydictionary.gui.component.TextBlock;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.dialog.Dialog;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A modal warning dialog: a title, a multi-line message, a row of action
 * buttons and a close button at the top-right corner. The panel background is
 * a flat rect placeholder until the book-style texture is drawn.
 * <p>
 * Buttons auto-close the dialog after running their action; clicking the
 * backdrop, the close button, or any button with a {@code null} action is the
 * "cancel" path.
 */
@ClientOnly
public class WarningDialog extends Dialog {
    private static final int PANEL_COLOR        = 0xFF2B2B2B;
    private static final int PANEL_BORDER_COLOR = 0xFF8B6B3D;

    private static final float PANEL_WIDTH        = 160;
    private static final float PANEL_HEIGHT       = 90;
    private static final float BUTTON_GAP         = 4;
    private static final float CLOSE_BUTTON_SIZE  = 10;

    private final TextBlock titleElement;
    private final TextBlock messageElement;
    private final List<LongButton> buttons = new ArrayList<>();
    private final LongButton closeButton;

    public WarningDialog(Component title, Component message) {
        titleElement = new TextBlock(title, 0.6F, Colors.WHITE);
        titleElement.setParent(this);

        messageElement = TextBlock.create()
                .text(message)
                .scale(0.5F)
                .color(Colors.COMMON_LIGHT_TEXT)
                .splitLines(true)
                .ellipsis(true)
                .showFullOnHover(true)
                .build();
        messageElement.setParent(this);

        closeButton = new LongButton(TextUtils.literal("x"), LongButton.STYLE_CANCEL, this::close);
        closeButton.setParent(this);
        closeButton.getBox().setSize(CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
    }

    public WarningDialog addButton(Component label, int style, Runnable action) {
        LongButton btn = new LongButton(label, style, () -> {
            close();
            if (action != null) {
                action.run();
            }
        });
        btn.setParent(this);
        buttons.add(btn);
        return this;
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);

        float pl = (width - PANEL_WIDTH) / 2F;
        float pt = (height - PANEL_HEIGHT) / 2F;
        panelBox.set(pl, pt, pl + PANEL_WIDTH, pt + PANEL_HEIGHT);

        titleElement.getBox().set(pl + 12, pt + 4, pl + PANEL_WIDTH - 12, pt + 16);
        messageElement.getBox().set(pl + 6, pt + 20, pl + PANEL_WIDTH - 6, pt + PANEL_HEIGHT - 24);

        closeButton.getBox().setPosition(panelBox.getRight() - CLOSE_BUTTON_SIZE - 2,
                                          panelBox.getTop() + 2);

        if (buttons.isEmpty()) { return; }
        float totalW = 0;
        for (LongButton b : buttons) {
            totalW += b.getBox().getWidth();
        }
        totalW += BUTTON_GAP * (buttons.size() - 1);
        float bx = panelBox.getLeft() + (panelBox.getWidth() - totalW) / 2F;
        float by = panelBox.getBottom() - LongButton.HEIGHT - 8;
        for (LongButton b : buttons) {
            b.getBox().setPosition(bx, by);
            bx += b.getBox().getWidth() + BUTTON_GAP;
        }
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);  // backdrop
        ctx.renderRectangle(PANEL_COLOR, ctx.getZ(),
                panelBox.getLeft(), panelBox.getTop(), panelBox.getRight(), panelBox.getBottom());
        ctx.renderRectangle(PANEL_BORDER_COLOR, 1F, ctx.getZ(),
                panelBox.getLeft(), panelBox.getTop(), panelBox.getRight(), panelBox.getBottom());
        // title/message/buttons are sub-elements, rendered after this by ScreenElement.render
    }
}
