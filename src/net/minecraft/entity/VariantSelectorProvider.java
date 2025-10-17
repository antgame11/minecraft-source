/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

public interface VariantSelectorProvider<Context, Condition extends SelectorCondition<Context>> {
    public List<Selector<Context, Condition>> getSelectors();

    public static <C, T> Stream<T> select(Stream<T> entries, Function<T, VariantSelectorProvider<C, ?>> providerGetter, C context) {
        ArrayList list = new ArrayList();
        entries.forEach(entry -> {
            VariantSelectorProvider lv = (VariantSelectorProvider)providerGetter.apply(entry);
            for (Selector lv2 : lv.getSelectors()) {
                list.add(new UnwrappedSelector(entry, lv2.priority(), DataFixUtils.orElseGet(lv2.condition(), SelectorCondition::alwaysTrue)));
            }
        });
        list.sort(UnwrappedSelector.PRIORITY_COMPARATOR);
        Iterator iterator = list.iterator();
        int i = Integer.MIN_VALUE;
        while (iterator.hasNext()) {
            UnwrappedSelector lv = (UnwrappedSelector)iterator.next();
            if (lv.priority < i) {
                iterator.remove();
                continue;
            }
            if (lv.condition.test(context)) {
                i = lv.priority;
                continue;
            }
            iterator.remove();
        }
        return list.stream().map(UnwrappedSelector::entry);
    }

    public static <C, T> Optional<T> select(Stream<T> entries, Function<T, VariantSelectorProvider<C, ?>> providerGetter, Random random, C context) {
        List<T> list = VariantSelectorProvider.select(entries, providerGetter, context).toList();
        return Util.getRandomOrEmpty(list, random);
    }

    public static <Context, Condition extends SelectorCondition<Context>> List<Selector<Context, Condition>> createSingle(Condition condition, int priority) {
        return List.of(new Selector(condition, priority));
    }

    public static <Context, Condition extends SelectorCondition<Context>> List<Selector<Context, Condition>> createFallback(int priority) {
        return List.of(new Selector(Optional.empty(), priority));
    }

    public record UnwrappedSelector<C, T>(T entry, int priority, SelectorCondition<C> condition) {
        public static final Comparator<UnwrappedSelector<?, ?>> PRIORITY_COMPARATOR = Comparator.comparingInt(UnwrappedSelector::priority).reversed();
    }

    @FunctionalInterface
    public static interface SelectorCondition<C>
    extends Predicate<C> {
        public static <C> SelectorCondition<C> alwaysTrue() {
            return context -> true;
        }
    }

    public record Selector<Context, Condition extends SelectorCondition<Context>>(Optional<Condition> condition, int priority) {
        public Selector(Condition condition, int priority) {
            this(Optional.of(condition), priority);
        }

        public Selector(int priority) {
            this(Optional.empty(), priority);
        }

        public static <Context, Condition extends SelectorCondition<Context>> Codec<Selector<Context, Condition>> createCodec(Codec<Condition> conditionCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(conditionCodec.optionalFieldOf("condition").forGetter(Selector::condition), ((MapCodec)Codec.INT.fieldOf("priority")).forGetter(Selector::priority)).apply((Applicative<Selector, ?>)instance, Selector::new));
        }
    }
}

