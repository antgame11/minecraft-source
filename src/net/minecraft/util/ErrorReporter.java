/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public interface ErrorReporter {
    public static final ErrorReporter EMPTY = new ErrorReporter(){

        @Override
        public ErrorReporter makeChild(Context context) {
            return this;
        }

        @Override
        public void report(Error error) {
        }
    };

    public ErrorReporter makeChild(Context var1);

    public void report(Error var1);

    public static class Logging
    extends Impl
    implements AutoCloseable {
        private final Logger logger;

        public Logging(Logger logger) {
            this.logger = logger;
        }

        public Logging(Context context, Logger logger) {
            super(context);
            this.logger = logger;
        }

        @Override
        public void close() {
            if (!this.isEmpty()) {
                this.logger.warn("[{}] Serialization errors:\n{}", (Object)this.logger.getName(), (Object)this.getErrorsAsLongString());
            }
        }
    }

    public static class Impl
    implements ErrorReporter {
        public static final Context CONTEXT = () -> "";
        @Nullable
        private final Impl parent;
        private final Context context;
        private final Set<ErrorEntry> errors;

        public Impl() {
            this(CONTEXT);
        }

        public Impl(Context context) {
            this.parent = null;
            this.errors = new LinkedHashSet<ErrorEntry>();
            this.context = context;
        }

        private Impl(Impl parent, Context context) {
            this.errors = parent.errors;
            this.parent = parent;
            this.context = context;
        }

        @Override
        public ErrorReporter makeChild(Context context) {
            return new Impl(this, context);
        }

        @Override
        public void report(Error error) {
            this.errors.add(new ErrorEntry(this, error));
        }

        public boolean isEmpty() {
            return this.errors.isEmpty();
        }

        public void apply(BiConsumer<String, Error> consumer) {
            ArrayList<Context> list = new ArrayList<Context>();
            StringBuilder stringBuilder = new StringBuilder();
            for (ErrorEntry lv : this.errors) {
                Impl lv2 = lv.source;
                while (lv2 != null) {
                    list.add(lv2.context);
                    lv2 = lv2.parent;
                }
                for (int i = list.size() - 1; i >= 0; --i) {
                    stringBuilder.append(((Context)list.get(i)).getName());
                }
                consumer.accept(stringBuilder.toString(), lv.error());
                stringBuilder.setLength(0);
                list.clear();
            }
        }

        public String getErrorsAsString() {
            HashMultimap multimap = HashMultimap.create();
            this.apply(multimap::put);
            return multimap.asMap().entrySet().stream().map(entry -> " at " + (String)entry.getKey() + ": " + ((Collection)entry.getValue()).stream().map(Error::getMessage).collect(Collectors.joining("; "))).collect(Collectors.joining("\n"));
        }

        public String getErrorsAsLongString() {
            ArrayList<Context> list = new ArrayList<Context>();
            ErrorList lv = new ErrorList(this.context);
            for (ErrorEntry lv2 : this.errors) {
                Impl lv3 = lv2.source;
                while (lv3 != this) {
                    list.add(lv3.context);
                    lv3 = lv3.parent;
                }
                ErrorList lv4 = lv;
                for (int i = list.size() - 1; i >= 0; --i) {
                    lv4 = lv4.get((Context)list.get(i));
                }
                list.clear();
                lv4.errors.add(lv2.error);
            }
            return String.join((CharSequence)"\n", lv.getMessages());
        }

        record ErrorEntry(Impl source, Error error) {
        }

        record ErrorList(Context element, List<Error> errors, Map<Context, ErrorList> children) {
            public ErrorList(Context context) {
                this(context, new ArrayList<Error>(), new LinkedHashMap<Context, ErrorList>());
            }

            public ErrorList get(Context context) {
                return this.children.computeIfAbsent(context, ErrorList::new);
            }

            public List<String> getMessages() {
                int i = this.errors.size();
                int j = this.children.size();
                if (i == 0 && j == 0) {
                    return List.of();
                }
                if (i == 0 && j == 1) {
                    ArrayList<String> list = new ArrayList<String>();
                    this.children.forEach((context, errors) -> list.addAll(errors.getMessages()));
                    list.set(0, this.element.getName() + (String)list.get(0));
                    return list;
                }
                if (i == 1 && j == 0) {
                    return List.of(this.element.getName() + ": " + this.errors.getFirst().getMessage());
                }
                ArrayList<String> list = new ArrayList<String>();
                this.children.forEach((context, errors) -> list.addAll(errors.getMessages()));
                list.replaceAll(message -> "  " + message);
                for (Error lv : this.errors) {
                    list.add("  " + lv.getMessage());
                }
                list.addFirst(this.element.getName() + ":");
                return list;
            }
        }
    }

    public record ReferenceLootTableContext(RegistryKey<?> id) implements Context
    {
        @Override
        public String getName() {
            return "->{" + String.valueOf(this.id.getValue()) + "@" + String.valueOf(this.id.getRegistry()) + "}";
        }
    }

    public record ListElementContext(int index) implements Context
    {
        @Override
        public String getName() {
            return "[" + this.index + "]";
        }
    }

    public record NamedListElementContext(String key, int index) implements Context
    {
        @Override
        public String getName() {
            return "." + this.key + "[" + this.index + "]";
        }
    }

    public record MapElementContext(String key) implements Context
    {
        @Override
        public String getName() {
            return "." + this.key;
        }
    }

    public record LootTableContext(RegistryKey<?> id) implements Context
    {
        @Override
        public String getName() {
            return "{" + String.valueOf(this.id.getValue()) + "@" + String.valueOf(this.id.getRegistry()) + "}";
        }
    }

    public record CriterionContext(String name) implements Context
    {
        @Override
        public String getName() {
            return this.name;
        }
    }

    @FunctionalInterface
    public static interface Context {
        public String getName();
    }

    public static interface Error {
        public String getMessage();
    }
}

