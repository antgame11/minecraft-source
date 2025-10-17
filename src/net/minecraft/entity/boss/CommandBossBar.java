/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.boss;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;

public class CommandBossBar
extends ServerBossBar {
    private static final int DEFAULT_MAX_VALUE = 100;
    private final Identifier id;
    private final Set<UUID> playerUuids = Sets.newHashSet();
    private int value;
    private int maxValue = 100;

    public CommandBossBar(Identifier id, Text displayName) {
        super(displayName, BossBar.Color.WHITE, BossBar.Style.PROGRESS);
        this.id = id;
        this.setPercent(0.0f);
    }

    public Identifier getId() {
        return this.id;
    }

    @Override
    public void addPlayer(ServerPlayerEntity player) {
        super.addPlayer(player);
        this.playerUuids.add(player.getUuid());
    }

    public void addPlayer(UUID uuid) {
        this.playerUuids.add(uuid);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        super.removePlayer(player);
        this.playerUuids.remove(player.getUuid());
    }

    @Override
    public void clearPlayers() {
        super.clearPlayers();
        this.playerUuids.clear();
    }

    public int getValue() {
        return this.value;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    public void setValue(int value) {
        this.value = value;
        this.setPercent(MathHelper.clamp((float)value / (float)this.maxValue, 0.0f, 1.0f));
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        this.setPercent(MathHelper.clamp((float)this.value / (float)maxValue, 0.0f, 1.0f));
    }

    public final Text toHoverableText() {
        return Texts.bracketed(this.getName()).styled(style -> style.withColor(this.getColor().getTextFormat()).withHoverEvent(new HoverEvent.ShowText(Text.literal(this.getId().toString()))).withInsertion(this.getId().toString()));
    }

    public boolean addPlayers(Collection<ServerPlayerEntity> players) {
        boolean bl;
        HashSet<UUID> set = Sets.newHashSet();
        HashSet<ServerPlayerEntity> set2 = Sets.newHashSet();
        for (UUID uUID : this.playerUuids) {
            bl = false;
            for (ServerPlayerEntity lv : players) {
                if (!lv.getUuid().equals(uUID)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            set.add(uUID);
        }
        for (ServerPlayerEntity lv2 : players) {
            bl = false;
            for (UUID uUID2 : this.playerUuids) {
                if (!lv2.getUuid().equals(uUID2)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            set2.add(lv2);
        }
        for (UUID uUID : set) {
            for (ServerPlayerEntity lv3 : this.getPlayers()) {
                if (!lv3.getUuid().equals(uUID)) continue;
                this.removePlayer(lv3);
                break;
            }
            this.playerUuids.remove(uUID);
        }
        for (ServerPlayerEntity lv2 : set2) {
            this.addPlayer(lv2);
        }
        return !set.isEmpty() || !set2.isEmpty();
    }

    public static CommandBossBar fromSerialized(Identifier id, Serialized serialized) {
        CommandBossBar lv = new CommandBossBar(id, serialized.name);
        lv.setVisible(serialized.visible);
        lv.setValue(serialized.value);
        lv.setMaxValue(serialized.max);
        lv.setColor(serialized.color);
        lv.setStyle(serialized.overlay);
        lv.setDarkenSky(serialized.darkenScreen);
        lv.setDragonMusic(serialized.playBossMusic);
        lv.setThickenFog(serialized.createWorldFog);
        serialized.players.forEach(lv::addPlayer);
        return lv;
    }

    public Serialized toSerialized() {
        return new Serialized(this.getName(), this.isVisible(), this.getValue(), this.getMaxValue(), this.getColor(), this.getStyle(), this.shouldDarkenSky(), this.hasDragonMusic(), this.shouldThickenFog(), Set.copyOf(this.playerUuids));
    }

    public void onPlayerConnect(ServerPlayerEntity player) {
        if (this.playerUuids.contains(player.getUuid())) {
            this.addPlayer(player);
        }
    }

    public void onPlayerDisconnect(ServerPlayerEntity player) {
        super.removePlayer(player);
    }

    public record Serialized(Text name, boolean visible, int value, int max, BossBar.Color color, BossBar.Style overlay, boolean darkenScreen, boolean playBossMusic, boolean createWorldFog, Set<UUID> players) {
        public static final Codec<Serialized> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)TextCodecs.CODEC.fieldOf("Name")).forGetter(Serialized::name), Codec.BOOL.optionalFieldOf("Visible", false).forGetter(Serialized::visible), Codec.INT.optionalFieldOf("Value", 0).forGetter(Serialized::value), Codec.INT.optionalFieldOf("Max", 100).forGetter(Serialized::max), BossBar.Color.CODEC.optionalFieldOf("Color", BossBar.Color.WHITE).forGetter(Serialized::color), BossBar.Style.CODEC.optionalFieldOf("Overlay", BossBar.Style.PROGRESS).forGetter(Serialized::overlay), Codec.BOOL.optionalFieldOf("DarkenScreen", false).forGetter(Serialized::darkenScreen), Codec.BOOL.optionalFieldOf("PlayBossMusic", false).forGetter(Serialized::playBossMusic), Codec.BOOL.optionalFieldOf("CreateWorldFog", false).forGetter(Serialized::createWorldFog), Uuids.SET_CODEC.optionalFieldOf("Players", Set.of()).forGetter(Serialized::players)).apply((Applicative<Serialized, ?>)instance, Serialized::new));
    }
}

