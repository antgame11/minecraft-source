/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.dispatch;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.GameRules;

public class GameRuleRpcDispatcher {
    public static List<TypedRule> get(ManagementHandlerDispatcher dispatcher) {
        List<GameRules.Key> list = dispatcher.getGameRuleHandler().getRules().map(Map.Entry::getKey).toList();
        ArrayList<TypedRule> list2 = new ArrayList<TypedRule>();
        for (GameRules.Key lv : list) {
            Object lv2 = dispatcher.getGameRuleHandler().getRule(lv);
            list2.add(GameRuleRpcDispatcher.toTypedRule(dispatcher, lv.getName(), lv2));
        }
        return list2;
    }

    public static TypedRule toTypedRule(ManagementHandlerDispatcher dispatcher, String name, GameRules.Rule<?> rule) {
        return dispatcher.getGameRuleHandler().toTypedRule(name, rule);
    }

    public static TypedRule updateRule(ManagementHandlerDispatcher dispatcher, UntypedRule untypedRule, ManagementConnectionId remote) {
        return dispatcher.getGameRuleHandler().updateRule(untypedRule, remote);
    }

    public record TypedRule(String key, String value, GameRuleType type) {
        public static final MapCodec<TypedRule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("key")).forGetter(TypedRule::key), ((MapCodec)Codec.STRING.fieldOf("value")).forGetter(TypedRule::value), ((MapCodec)StringIdentifiable.createCodec(GameRuleType::values).fieldOf("type")).forGetter(TypedRule::type)).apply((Applicative<TypedRule, ?>)instance, TypedRule::new));
    }

    public record UntypedRule(String key, String value) {
        public static final MapCodec<UntypedRule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("key")).forGetter(UntypedRule::key), ((MapCodec)Codec.STRING.fieldOf("value")).forGetter(UntypedRule::value)).apply((Applicative<UntypedRule, ?>)instance, UntypedRule::new));
    }

    public static enum GameRuleType implements StringIdentifiable
    {
        INT("integer"),
        BOOL("boolean");

        private final String name;

        private GameRuleType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}

