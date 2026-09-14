package io.github.xienaoban.biologydictionary.core.skill;

import net.minecraft.network.chat.MutableComponent;

public class NoPermissionException extends RuntimeException {
    private final MutableComponent gameMessage;

    public NoPermissionException(MutableComponent gameMessage, String javaMessage) {
        super("No permission to set the property: " + javaMessage);
        this.gameMessage = gameMessage;
    }

    public MutableComponent getGameMessage() {
        return gameMessage;
    }
}
