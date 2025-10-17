/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerLikeEntity;
import net.minecraft.client.network.ClientPlayerLikeState;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractClientPlayerEntity
extends PlayerEntity
implements ClientPlayerLikeEntity {
    @Nullable
    private PlayerListEntry playerListEntry;
    private final boolean deadmau5;
    private final ClientPlayerLikeState state = new ClientPlayerLikeState();

    public AbstractClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
        this.deadmau5 = "deadmau5".equals(this.getGameProfile().name());
    }

    @Override
    @Nullable
    public GameMode getGameMode() {
        PlayerListEntry lv = this.getPlayerListEntry();
        return lv != null ? lv.getGameMode() : null;
    }

    @Nullable
    protected PlayerListEntry getPlayerListEntry() {
        if (this.playerListEntry == null) {
            this.playerListEntry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(this.getUuid());
        }
        return this.playerListEntry;
    }

    @Override
    public void tick() {
        this.state.tick(this.getEntityPos(), this.getVelocity());
        super.tick();
    }

    protected void addDistanceMoved(float distanceMoved) {
        this.state.addDistanceMoved(distanceMoved);
    }

    @Override
    public ClientPlayerLikeState getState() {
        return this.state;
    }

    @Override
    @Nullable
    public Text getMannequinName() {
        Scoreboard lv = this.getEntityWorld().getScoreboard();
        ScoreboardObjective lv2 = lv.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
        if (lv2 != null) {
            ReadableScoreboardScore lv3 = lv.getScore(this, lv2);
            MutableText lv4 = ReadableScoreboardScore.getFormattedScore(lv3, lv2.getNumberFormatOr(StyledNumberFormat.EMPTY));
            return Text.empty().append(lv4).append(ScreenTexts.SPACE).append(lv2.getDisplayName());
        }
        return null;
    }

    @Override
    public SkinTextures getSkin() {
        PlayerListEntry lv = this.getPlayerListEntry();
        return lv == null ? DefaultSkinHelper.getSkinTextures(this.getUuid()) : lv.getSkinTextures();
    }

    @Override
    @Nullable
    public ParrotEntity.Variant getShoulderParrotVariant(boolean leftShoulder) {
        return (leftShoulder ? this.getLeftShoulderParrotVariant() : this.getRightShoulderParrotVariant()).orElse(null);
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        this.getState().tickRiding();
    }

    @Override
    public void tickMovement() {
        this.tickPlayerMovement();
        super.tickMovement();
    }

    protected void tickPlayerMovement() {
        float f = !this.isOnGround() || this.isDead() || this.isSwimming() ? 0.0f : Math.min(0.1f, (float)this.getVelocity().horizontalLength());
        this.getState().tickMovement(f);
    }

    public float getFovMultiplier(boolean firstPerson, float fovEffectScale) {
        float i;
        float h;
        float g = 1.0f;
        if (this.getAbilities().flying) {
            g *= 1.1f;
        }
        if ((h = this.getAbilities().getWalkSpeed()) != 0.0f) {
            i = (float)this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) / h;
            g *= (i + 1.0f) / 2.0f;
        }
        if (this.isUsingItem()) {
            if (this.getActiveItem().isOf(Items.BOW)) {
                i = Math.min((float)this.getItemUseTime() / 20.0f, 1.0f);
                g *= 1.0f - MathHelper.square(i) * 0.15f;
            } else if (firstPerson && this.isUsingSpyglass()) {
                return 0.1f;
            }
        }
        return MathHelper.lerp(fovEffectScale, 1.0f, g);
    }

    @Override
    public boolean hasExtraEars() {
        return this.deadmau5;
    }
}

