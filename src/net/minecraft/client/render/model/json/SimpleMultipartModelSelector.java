/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.MultipartModelCondition;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record SimpleMultipartModelSelector(Map<String, Terms> tests) implements MultipartModelCondition
{
    static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<SimpleMultipartModelSelector> CODEC = Codecs.nonEmptyMap(Codec.unboundedMap(Codec.STRING, Terms.VALUE_CODEC)).xmap(SimpleMultipartModelSelector::new, SimpleMultipartModelSelector::tests);

    @Override
    public <O, S extends State<O, S>> Predicate<S> instantiate(StateManager<O, S> arg) {
        ArrayList list = new ArrayList(this.tests.size());
        this.tests.forEach((property, terms) -> list.add(SimpleMultipartModelSelector.init(arg, property, terms)));
        return Util.allOf(list);
    }

    private static <O, S extends State<O, S>> Predicate<S> init(StateManager<O, S> stateManager, String property, Terms terms) {
        Property<?> lv = stateManager.getProperty(property);
        if (lv == null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Unknown property '%s' on '%s'", property, stateManager.getOwner()));
        }
        return terms.instantiate(stateManager.getOwner(), lv);
    }

    @Environment(value=EnvType.CLIENT)
    public record Terms(List<Term> entries) {
        private static final char DELIMITER = '|';
        private static final Joiner JOINER = Joiner.on('|');
        private static final Splitter SPLITTER = Splitter.on('|');
        private static final Codec<String> CODEC = Codec.either(Codec.INT, Codec.BOOL).flatComapMap(either -> either.map(String::valueOf, String::valueOf), string -> DataResult.error(() -> "This codec can't be used for encoding"));
        public static final Codec<Terms> VALUE_CODEC = Codec.withAlternative(Codec.STRING, CODEC).comapFlatMap(Terms::tryParse, Terms::toString);

        public Terms {
            if (entries.isEmpty()) {
                throw new IllegalArgumentException("Empty value for property");
            }
        }

        public static DataResult<Terms> tryParse(String terms) {
            List<Term> list = SPLITTER.splitToStream(terms).map(Term::parse).toList();
            if (list.isEmpty()) {
                return DataResult.error(() -> "Empty value for property");
            }
            for (Term lv : list) {
                if (!lv.value.isEmpty()) continue;
                return DataResult.error(() -> "Empty term in value '" + terms + "'");
            }
            return DataResult.success(new Terms(list));
        }

        @Override
        public String toString() {
            return JOINER.join(this.entries);
        }

        public <O, S extends State<O, S>, T extends Comparable<T>> Predicate<S> instantiate(O object, Property<T> property) {
            ArrayList list2;
            boolean bl;
            Predicate predicate = Util.anyOf(Lists.transform(this.entries, term -> this.instantiate(object, property, (Term)term)));
            ArrayList list = new ArrayList(property.getValues());
            int i = list.size();
            list.removeIf(predicate.negate());
            int j = list.size();
            if (j == 0) {
                LOGGER.warn("Condition {} for property {} on {} is always false", this, property.getName(), object);
                return state -> false;
            }
            int k = i - j;
            if (k == 0) {
                LOGGER.warn("Condition {} for property {} on {} is always true", this, property.getName(), object);
                return state -> true;
            }
            if (j <= k) {
                bl = false;
                list2 = list;
            } else {
                bl = true;
                ArrayList<T> list3 = new ArrayList<T>(property.getValues());
                list3.removeIf(predicate);
                list2 = list3;
            }
            if (list2.size() == 1) {
                Comparable comparable = (Comparable)list2.getFirst();
                return state -> {
                    Object comparable2 = state.get(property);
                    return comparable.equals(comparable2) ^ bl;
                };
            }
            return state -> {
                Object comparable = state.get(property);
                return list2.contains(comparable) ^ bl;
            };
        }

        private <T extends Comparable<T>> T parseValue(Object object, Property<T> property, String value) {
            Optional<T> optional = property.parse(value);
            if (optional.isEmpty()) {
                throw new RuntimeException(String.format(Locale.ROOT, "Unknown value '%s' for property '%s' on '%s' in '%s'", value, property, object, this));
            }
            return (T)((Comparable)optional.get());
        }

        private <T extends Comparable<T>> Predicate<T> instantiate(Object object, Property<T> property, Term term) {
            Object comparable = this.parseValue(object, property, term.value);
            if (term.negated) {
                return value -> !value.equals(comparable);
            }
            return value -> value.equals(comparable);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Term(String value, boolean negated) {
        private static final String NEGATED_PREFIX = "!";

        public Term {
            if (value.isEmpty()) {
                throw new IllegalArgumentException("Empty term");
            }
        }

        public static Term parse(String value) {
            if (value.startsWith(NEGATED_PREFIX)) {
                return new Term(value.substring(1), true);
            }
            return new Term(value, false);
        }

        @Override
        public String toString() {
            return this.negated ? NEGATED_PREFIX + this.value : this.value;
        }
    }
}

