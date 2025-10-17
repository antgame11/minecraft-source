/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTeam {
    public boolean isEqual(@Nullable AbstractTeam team) {
        if (team == null) {
            return false;
        }
        return this == team;
    }

    public abstract String getName();

    public abstract MutableText decorateName(Text var1);

    public abstract boolean shouldShowFriendlyInvisibles();

    public abstract boolean isFriendlyFireAllowed();

    public abstract VisibilityRule getNameTagVisibilityRule();

    public abstract Formatting getColor();

    public abstract Collection<String> getPlayerList();

    public abstract VisibilityRule getDeathMessageVisibilityRule();

    public abstract CollisionRule getCollisionRule();

    public static enum CollisionRule implements StringIdentifiable
    {
        ALWAYS("always", 0),
        NEVER("never", 1),
        PUSH_OTHER_TEAMS("pushOtherTeams", 2),
        PUSH_OWN_TEAM("pushOwnTeam", 3);

        public static final Codec<CollisionRule> CODEC;
        private static final IntFunction<CollisionRule> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, CollisionRule> PACKET_CODEC;
        public final String name;
        public final int index;

        private CollisionRule(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public Text getDisplayName() {
            return Text.translatable("team.collision." + this.name);
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(CollisionRule::values);
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(collisionRule -> collisionRule.index, CollisionRule.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, collisionRule -> collisionRule.index);
        }
    }

    public static enum VisibilityRule implements StringIdentifiable
    {
        ALWAYS("always", 0),
        NEVER("never", 1),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

        public static final Codec<VisibilityRule> CODEC;
        private static final IntFunction<VisibilityRule> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, VisibilityRule> PACKET_CODEC;
        public final String name;
        public final int index;

        private VisibilityRule(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public Text getDisplayName() {
            return Text.translatable("team.visibility." + this.name);
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(VisibilityRule::values);
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(visibilityRule -> visibilityRule.index, VisibilityRule.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, visibilityRule -> visibilityRule.index);
        }
    }
}

