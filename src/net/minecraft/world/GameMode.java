/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.function.IntFunction;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum GameMode implements StringIdentifiable
{
    SURVIVAL(0, "survival"),
    CREATIVE(1, "creative"),
    ADVENTURE(2, "adventure"),
    SPECTATOR(3, "spectator");

    public static final GameMode DEFAULT;
    public static final StringIdentifiable.EnumCodec<GameMode> CODEC;
    private static final IntFunction<GameMode> INDEX_MAPPER;
    public static final PacketCodec<ByteBuf, GameMode> PACKET_CODEC;
    @Deprecated
    public static final Codec<GameMode> INDEX_CODEC;
    private static final int UNKNOWN = -1;
    private final int index;
    private final String id;
    private final Text simpleTranslatableName;
    private final Text translatableName;

    private GameMode(int index, String id) {
        this.index = index;
        this.id = id;
        this.simpleTranslatableName = Text.translatable("selectWorld.gameMode." + id);
        this.translatableName = Text.translatable("gameMode." + id);
    }

    public int getIndex() {
        return this.index;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    public Text getTranslatableName() {
        return this.translatableName;
    }

    public Text getSimpleTranslatableName() {
        return this.simpleTranslatableName;
    }

    public void setAbilities(PlayerAbilities abilities) {
        if (this == CREATIVE) {
            abilities.allowFlying = true;
            abilities.creativeMode = true;
            abilities.invulnerable = true;
        } else if (this == SPECTATOR) {
            abilities.allowFlying = true;
            abilities.creativeMode = false;
            abilities.invulnerable = true;
            abilities.flying = true;
        } else {
            abilities.allowFlying = false;
            abilities.creativeMode = false;
            abilities.invulnerable = false;
            abilities.flying = false;
        }
        abilities.allowModifyWorld = !this.isBlockBreakingRestricted();
    }

    public boolean isBlockBreakingRestricted() {
        return this == ADVENTURE || this == SPECTATOR;
    }

    public boolean isCreative() {
        return this == CREATIVE;
    }

    public boolean isSurvivalLike() {
        return this == SURVIVAL || this == ADVENTURE;
    }

    public static GameMode byIndex(int index) {
        return INDEX_MAPPER.apply(index);
    }

    public static GameMode byId(String id) {
        return GameMode.byId(id, SURVIVAL);
    }

    @Nullable
    @Contract(value="_,!null->!null;_,null->_")
    public static GameMode byId(String id, @Nullable GameMode fallback) {
        GameMode lv = CODEC.byId(id);
        return lv != null ? lv : fallback;
    }

    public static int getId(@Nullable GameMode gameMode) {
        return gameMode != null ? gameMode.index : -1;
    }

    @Nullable
    public static GameMode getOrNull(int index) {
        if (index == -1) {
            return null;
        }
        return GameMode.byIndex(index);
    }

    public static boolean isValid(int index) {
        return Arrays.stream(GameMode.values()).anyMatch(gameMode -> gameMode.index == index);
    }

    static {
        DEFAULT = SURVIVAL;
        CODEC = StringIdentifiable.createCodec(GameMode::values);
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(GameMode::getIndex, GameMode.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, GameMode::getIndex);
        INDEX_CODEC = Codec.INT.xmap(GameMode::byIndex, GameMode::getIndex);
    }
}

