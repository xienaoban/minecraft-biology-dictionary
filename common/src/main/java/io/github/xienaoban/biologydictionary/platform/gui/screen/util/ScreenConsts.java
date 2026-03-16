package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

public interface ScreenConsts {
    /**
     * Text Top Offset for texts scaled to 0.5.
     * To center a line of text vertically within a background that is 2*n pixels high.
     * <p>
     * For Chinese, this value is suitable to be 0; for English and numbers, it is suitable to be 0.25.
     * To strike a balance, take a middle value so that the text is positioned appropriately when rendered.
     */
    float TXT_TO = 0.2F;

    float TXT_ASCII_TO = 0.25F;
    float TXT_CHN_TO = 0F;
}
