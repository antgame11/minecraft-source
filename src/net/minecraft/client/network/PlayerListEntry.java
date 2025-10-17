/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.message.MessageVerifier;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerListEntry {
    private final GameProfile profile;
    @Nullable
    private Supplier<SkinTextures> texturesSupplier;
    private GameMode gameMode = GameMode.DEFAULT;
    private int latency;
    @Nullable
    private Text displayName;
    private boolean showHat = true;
    @Nullable
    private PublicPlayerSession session;
    private MessageVerifier messageVerifier;
    private int listOrder;

    public PlayerListEntry(GameProfile profile, boolean secureChatEnforced) {
        this.profile = profile;
        this.messageVerifier = PlayerListEntry.getInitialVerifier(secureChatEnforced);
    }

    private static Supplier<SkinTextures> texturesSupplier(GameProfile profile) {
        MinecraftClient lv = MinecraftClient.getInstance();
        boolean bl = !lv.uuidEquals(profile.id());
        return lv.getSkinProvider().supplySkinTextures(profile, bl);
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    @Nullable
    public PublicPlayerSession getSession() {
        return this.session;
    }

    public MessageVerifier getMessageVerifier() {
        return this.messageVerifier;
    }

    public boolean hasPublicKey() {
        return this.session != null;
    }

    protected void setSession(PublicPlayerSession session) {
        this.session = session;
        this.messageVerifier = session.createVerifier(PlayerPublicKey.EXPIRATION_GRACE_PERIOD);
    }

    protected void resetSession(boolean secureChatEnforced) {
        this.session = null;
        this.messageVerifier = PlayerListEntry.getInitialVerifier(secureChatEnforced);
    }

    private static MessageVerifier getInitialVerifier(boolean secureChatEnforced) {
        return secureChatEnforced ? MessageVerifier.UNVERIFIED : MessageVerifier.NO_SIGNATURE;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    protected void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public int getLatency() {
        return this.latency;
    }

    protected void setLatency(int latency) {
        this.latency = latency;
    }

    public SkinTextures getSkinTextures() {
        if (this.texturesSupplier == null) {
            this.texturesSupplier = PlayerListEntry.texturesSupplier(this.profile);
        }
        return this.texturesSupplier.get();
    }

    @Nullable
    public Team getScoreboardTeam() {
        return MinecraftClient.getInstance().world.getScoreboard().getScoreHolderTeam(this.getProfile().name());
    }

    public void setDisplayName(@Nullable Text displayName) {
        this.displayName = displayName;
    }

    @Nullable
    public Text getDisplayName() {
        return this.displayName;
    }

    public void setShowHat(boolean showHat) {
        this.showHat = showHat;
    }

    public boolean shouldShowHat() {
        return this.showHat;
    }

    public void setListOrder(int listOrder) {
        this.listOrder = listOrder;
    }

    public int getListOrder() {
        return this.listOrder;
    }
}

