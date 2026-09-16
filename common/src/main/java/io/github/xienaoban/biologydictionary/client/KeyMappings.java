package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.client.KeyMapping;

@ClientOnly
public final class KeyMappings {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            IdentifierUtils.bd(BiologyDictionary.MOD_ID));

    @PlatformEntry
    public static final KeyMapping OPEN_HANDBOOK = new KeyMapping(
            Lang.KEY_OPEN_HANDBOOK,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_GRAVE,
            CATEGORY,
            0);

    public static final KeyMapping STEAL_INVENTORY = new KeyMapping(
            Lang.KEY_STEAL_INVENTORY,
            InputConstants.Type.KEYBOARD,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY,
            1);

    public static final KeyMapping DEBUG = new KeyMapping(
            Lang.KEY_DEBUG,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_RALT,
            CATEGORY,
            2);

    private KeyMappings() {}
}
