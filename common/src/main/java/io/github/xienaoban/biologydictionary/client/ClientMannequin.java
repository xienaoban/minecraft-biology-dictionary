package io.github.xienaoban.biologydictionary.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * A client-only dummy player for rendering arbitrary player profiles in GUIs.
 * The vanilla {@code net.minecraft.client.entity.ClientMannequin} does not exist
 * in 1.20.1; the profile is passed via the constructor instead of entity data
 * sync, and the skin is looked up asynchronously like the vanilla one.
 */
@ClientOnly
public final class ClientMannequin extends AbstractClientPlayer {
    private ResourceLocation skinLocation;
    private String modelName;

    public ClientMannequin(Level level, GameProfile profile) {
        super((ClientLevel) level, profile != null ? profile : new GameProfile(Util.NIL_UUID, "Mannequin"));
        this.skinLocation = DefaultPlayerSkin.getDefaultSkin(this.getUUID());
        this.modelName = DefaultPlayerSkin.getSkinModelName(this.getUUID());
        if (profile != null) {
            Minecraft.getInstance().getSkinManager().registerSkins(profile, (type, location, texture) -> {
                if (type == MinecraftProfileTexture.Type.SKIN) {
                    this.skinLocation = location;
                }
            }, true);
        }
    }

    @Override
    public ResourceLocation getSkinTextureLocation() {
        return this.skinLocation;
    }

    @Override
    public String getModelName() {
        return this.modelName;
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
