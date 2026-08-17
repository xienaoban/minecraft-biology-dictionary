package io.github.xienaoban.biologydictionary.client;

import com.mojang.authlib.GameProfile;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.level.Level;

import java.util.concurrent.CompletableFuture;

/**
 * A client-only dummy player for rendering arbitrary player profiles in GUIs.
 * Equivalent of the vanilla {@code net.minecraft.client.entity.ClientMannequin},
 * which does not exist in 1.21.1: the profile is passed via the constructor
 * instead of entity data sync, and the skin is looked up asynchronously like the vanilla one.
 */
@ClientOnly
public final class ClientMannequin extends AbstractClientPlayer {
    private CompletableFuture<PlayerSkin> skinLookup;
    private PlayerSkin skin;

    public ClientMannequin(Level level, GameProfile profile) {
        super((ClientLevel) level, profile != null ? profile : new GameProfile(Util.NIL_UUID, "Mannequin"));
        this.skin = DefaultPlayerSkin.get(this.getUUID());
        if (profile != null) {
            this.skinLookup = Minecraft.getInstance().getSkinManager().getOrLoad(profile);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.skinLookup != null && this.skinLookup.isDone()) {
            try {
                PlayerSkin loaded = this.skinLookup.get();
                if (loaded != null) {
                    this.skin = loaded;
                }
            } catch (Exception e) {
                BiologyDictionary.LOGGER.error("Error when trying to look up skin", e);
            }
            this.skinLookup = null;
        }
    }

    @Override
    public PlayerSkin getSkin() {
        return this.skin;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
