/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.MultipartModelCondition;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public record MultipartModelCombinedCondition(LogicalOperator operation, List<MultipartModelCondition> terms) implements MultipartModelCondition
{
    @Override
    public <O, S extends State<O, S>> Predicate<S> instantiate(StateManager<O, S> arg) {
        return this.operation.apply(Lists.transform(this.terms, condition -> condition.instantiate(arg)));
    }

    @Environment(value=EnvType.CLIENT)
    public static enum LogicalOperator implements StringIdentifiable
    {
        AND("AND"){

            @Override
            public <V> Predicate<V> apply(List<Predicate<V>> conditions) {
                return Util.allOf(conditions);
            }
        }
        ,
        OR("OR"){

            @Override
            public <V> Predicate<V> apply(List<Predicate<V>> conditions) {
                return Util.anyOf(conditions);
            }
        };

        public static final Codec<LogicalOperator> CODEC;
        private final String name;

        LogicalOperator(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public abstract <V> Predicate<V> apply(List<Predicate<V>> var1);

        static {
            CODEC = StringIdentifiable.createCodec(LogicalOperator::values);
        }
    }
}

