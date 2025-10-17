/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.storage;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.lang.runtime.SwitchBootstraps;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadContext;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import org.jetbrains.annotations.Nullable;

public class NbtReadView
implements ReadView {
    private final ErrorReporter reporter;
    private final ReadContext context;
    private final NbtCompound nbt;

    private NbtReadView(ErrorReporter reporter, ReadContext context, NbtCompound nbt) {
        this.reporter = reporter;
        this.context = context;
        this.nbt = nbt;
    }

    public static ReadView create(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, NbtCompound nbt) {
        return new NbtReadView(reporter, new ReadContext(registries, NbtOps.INSTANCE), nbt);
    }

    public static ReadView.ListReadView createList(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, List<NbtCompound> elements) {
        return new NbtListReadView(reporter, new ReadContext(registries, NbtOps.INSTANCE), elements);
    }

    @Override
    public <T> Optional<T> read(String key, Codec<T> codec) {
        NbtElement lv = this.nbt.get(key);
        if (lv == null) {
            return Optional.empty();
        }
        DataResult dataResult = codec.parse(this.context.getOps(), lv);
        Objects.requireNonNull(dataResult);
        DataResult dataResult2 = dataResult;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, dataResult2, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                DataResult.Success success = (DataResult.Success)dataResult2;
                yield Optional.of(success.value());
            }
            case 1 -> {
                DataResult.Error error = (DataResult.Error)dataResult2;
                this.reporter.report(new DecodeError(key, lv, error));
                yield error.partialValue();
            }
        };
    }

    @Override
    public <T> Optional<T> read(MapCodec<T> mapCodec) {
        DynamicOps<NbtElement> dynamicOps = this.context.getOps();
        DataResult dataResult = dynamicOps.getMap(this.nbt).flatMap(map -> mapCodec.decode(dynamicOps, map));
        Objects.requireNonNull(dataResult);
        DataResult dataResult2 = dataResult;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, dataResult2, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                DataResult.Success success = (DataResult.Success)dataResult2;
                yield Optional.of(success.value());
            }
            case 1 -> {
                DataResult.Error error = (DataResult.Error)dataResult2;
                this.reporter.report(new DecodeMapError(error));
                yield error.partialValue();
            }
        };
    }

    @Nullable
    private <T extends NbtElement> T get(String key, NbtType<T> type) {
        NbtElement lv = this.nbt.get(key);
        if (lv == null) {
            return null;
        }
        NbtType<?> lv2 = lv.getNbtType();
        if (lv2 != type) {
            this.reporter.report(new ExpectedTypeError(key, type, lv2));
            return null;
        }
        return (T)lv;
    }

    @Nullable
    private AbstractNbtNumber get(String key) {
        NbtElement lv = this.nbt.get(key);
        if (lv == null) {
            return null;
        }
        if (lv instanceof AbstractNbtNumber) {
            AbstractNbtNumber lv2 = (AbstractNbtNumber)lv;
            return lv2;
        }
        this.reporter.report(new ExpectedNumberError(key, lv.getNbtType()));
        return null;
    }

    @Override
    public Optional<ReadView> getOptionalReadView(String key) {
        NbtCompound lv = this.get(key, NbtCompound.TYPE);
        return lv != null ? Optional.of(this.createChildReadView(key, lv)) : Optional.empty();
    }

    @Override
    public ReadView getReadView(String key) {
        NbtCompound lv = this.get(key, NbtCompound.TYPE);
        return lv != null ? this.createChildReadView(key, lv) : this.context.getEmptyReadView();
    }

    @Override
    public Optional<ReadView.ListReadView> getOptionalListReadView(String key) {
        NbtList lv = this.get(key, NbtList.TYPE);
        return lv != null ? Optional.of(this.createChildListReadView(key, this.context, lv)) : Optional.empty();
    }

    @Override
    public ReadView.ListReadView getListReadView(String key) {
        NbtList lv = this.get(key, NbtList.TYPE);
        return lv != null ? this.createChildListReadView(key, this.context, lv) : this.context.getEmptyListReadView();
    }

    @Override
    public <T> Optional<ReadView.TypedListReadView<T>> getOptionalTypedListView(String key, Codec<T> typeCodec) {
        NbtList lv = this.get(key, NbtList.TYPE);
        return lv != null ? Optional.of(this.createTypedListReadView(key, lv, typeCodec)) : Optional.empty();
    }

    @Override
    public <T> ReadView.TypedListReadView<T> getTypedListView(String key, Codec<T> typeCodec) {
        NbtList lv = this.get(key, NbtList.TYPE);
        return lv != null ? this.createTypedListReadView(key, lv, typeCodec) : this.context.getEmptyTypedListReadView();
    }

    @Override
    public boolean getBoolean(String key, boolean fallback) {
        AbstractNbtNumber lv = this.get(key);
        return lv != null ? lv.byteValue() != 0 : fallback;
    }

    @Override
    public byte getByte(String key, byte fallback) {
        AbstractNbtNumber lv = this.get(key);
        return lv != null ? lv.byteValue() : fallback;
    }

    @Override
    public int getShort(String key, short fallback) {
        AbstractNbtNumber lv = this.get(key);
        return lv != null ? lv.shortValue() : fallback;
    }

    @Override
    public Optional<Integer> getOptionalInt(String key) {
        AbstractNbtNumber lv = this.get(key);
        return lv != null ? Optional.of(lv.intValue()) : Optional.empty();
    }

    @Override
    public int getInt(String key, int fallback) {
        AbstractNbtNumber lv = this.get(key);
        return lv != null ? lv.intValue() : fallback;
    }

    @Override
    public long getLong(String key, long fallback) {
        AbstractNbtNumber lv = this.get(key);
        return lv != null ? lv.longValue() : fallback;
    }

    @Override
    public Optional<Long> getOptionalLong(String key) {
        AbstractNbtNumber lv = this.get(key);
        return lv != null ? Optional.of(lv.longValue()) : Optional.empty();
    }

    @Override
    public float getFloat(String key, float fallback) {
        AbstractNbtNumber lv = this.get(key);
        return lv != null ? lv.floatValue() : fallback;
    }

    @Override
    public double getDouble(String key, double fallback) {
        AbstractNbtNumber lv = this.get(key);
        return lv != null ? lv.doubleValue() : fallback;
    }

    @Override
    public Optional<String> getOptionalString(String key) {
        NbtString lv = this.get(key, NbtString.TYPE);
        return lv != null ? Optional.of(lv.value()) : Optional.empty();
    }

    @Override
    public String getString(String key, String fallback) {
        NbtString lv = this.get(key, NbtString.TYPE);
        return lv != null ? lv.value() : fallback;
    }

    @Override
    public Optional<int[]> getOptionalIntArray(String key) {
        NbtIntArray lv = this.get(key, NbtIntArray.TYPE);
        return lv != null ? Optional.of(lv.getIntArray()) : Optional.empty();
    }

    @Override
    public RegistryWrapper.WrapperLookup getRegistries() {
        return this.context.getRegistries();
    }

    private ReadView createChildReadView(String key, NbtCompound nbt) {
        return nbt.isEmpty() ? this.context.getEmptyReadView() : new NbtReadView(this.reporter.makeChild(new ErrorReporter.MapElementContext(key)), this.context, nbt);
    }

    static ReadView createReadView(ErrorReporter reporter, ReadContext context, NbtCompound nbt) {
        return nbt.isEmpty() ? context.getEmptyReadView() : new NbtReadView(reporter, context, nbt);
    }

    private ReadView.ListReadView createChildListReadView(String key, ReadContext context, NbtList list) {
        return list.isEmpty() ? context.getEmptyListReadView() : new ChildListReadView(this.reporter, key, context, list);
    }

    private <T> ReadView.TypedListReadView<T> createTypedListReadView(String key, NbtList list, Codec<T> typeCodec) {
        return list.isEmpty() ? this.context.getEmptyTypedListReadView() : new NbtTypedListReadView<T>(this.reporter, key, this.context, typeCodec, list);
    }

    static class NbtListReadView
    implements ReadView.ListReadView {
        private final ErrorReporter reporter;
        private final ReadContext context;
        private final List<NbtCompound> nbts;

        public NbtListReadView(ErrorReporter reporter, ReadContext context, List<NbtCompound> nbts) {
            this.reporter = reporter;
            this.context = context;
            this.nbts = nbts;
        }

        ReadView createReadView(int index, NbtCompound nbt) {
            return NbtReadView.createReadView(this.reporter.makeChild(new ErrorReporter.ListElementContext(index)), this.context, nbt);
        }

        @Override
        public boolean isEmpty() {
            return this.nbts.isEmpty();
        }

        @Override
        public Stream<ReadView> stream() {
            return Streams.mapWithIndex(this.nbts.stream(), (nbt, index) -> this.createReadView((int)index, (NbtCompound)nbt));
        }

        @Override
        public Iterator<ReadView> iterator() {
            final ListIterator<NbtCompound> listIterator = this.nbts.listIterator();
            return new AbstractIterator<ReadView>(){

                @Override
                @Nullable
                protected ReadView computeNext() {
                    if (listIterator.hasNext()) {
                        int i = listIterator.nextIndex();
                        NbtCompound lv = (NbtCompound)listIterator.next();
                        return this.createReadView(i, lv);
                    }
                    return (ReadView)this.endOfData();
                }

                @Override
                @Nullable
                protected /* synthetic */ Object computeNext() {
                    return this.computeNext();
                }
            };
        }
    }

    public record DecodeError(String name, NbtElement element, DataResult.Error<?> error) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Failed to decode value '" + String.valueOf(this.element) + "' from field '" + this.name + "': " + this.error.message();
        }
    }

    public record DecodeMapError(DataResult.Error<?> error) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Failed to decode from map: " + this.error.message();
        }
    }

    public record ExpectedTypeError(String name, NbtType<?> expected, NbtType<?> actual) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Expected field '" + this.name + "' to contain value of type " + this.expected.getCrashReportName() + ", but got " + this.actual.getCrashReportName();
        }
    }

    public record ExpectedNumberError(String name, NbtType<?> actual) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Expected field '" + this.name + "' to contain number, but got " + this.actual.getCrashReportName();
        }
    }

    static class ChildListReadView
    implements ReadView.ListReadView {
        private final ErrorReporter reporter;
        private final String name;
        final ReadContext context;
        private final NbtList list;

        ChildListReadView(ErrorReporter reporter, String name, ReadContext context, NbtList list) {
            this.reporter = reporter;
            this.name = name;
            this.context = context;
            this.list = list;
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        ErrorReporter createErrorReporter(int index) {
            return this.reporter.makeChild(new ErrorReporter.NamedListElementContext(this.name, index));
        }

        void reportExpectedTypeAtIndexError(int index, NbtElement element) {
            this.reporter.report(new ExpectedTypeAtIndexError(this.name, index, NbtCompound.TYPE, element.getNbtType()));
        }

        @Override
        public Stream<ReadView> stream() {
            return Streams.mapWithIndex(this.list.stream(), (element, index) -> {
                if (element instanceof NbtCompound) {
                    NbtCompound lv = (NbtCompound)element;
                    return NbtReadView.createReadView(this.createErrorReporter((int)index), this.context, lv);
                }
                this.reportExpectedTypeAtIndexError((int)index, (NbtElement)element);
                return null;
            }).filter(Objects::nonNull);
        }

        @Override
        public Iterator<ReadView> iterator() {
            final Iterator iterator = this.list.iterator();
            return new AbstractIterator<ReadView>(){
                private int index;

                @Override
                @Nullable
                protected ReadView computeNext() {
                    while (iterator.hasNext()) {
                        int i;
                        NbtElement lv = (NbtElement)iterator.next();
                        ++this.index;
                        if (lv instanceof NbtCompound) {
                            NbtCompound lv2 = (NbtCompound)lv;
                            return NbtReadView.createReadView(this.createErrorReporter(i), context, lv2);
                        }
                        this.reportExpectedTypeAtIndexError(i, lv);
                    }
                    return (ReadView)this.endOfData();
                }

                @Override
                @Nullable
                protected /* synthetic */ Object computeNext() {
                    return this.computeNext();
                }
            };
        }
    }

    static class NbtTypedListReadView<T>
    implements ReadView.TypedListReadView<T> {
        private final ErrorReporter reporter;
        private final String name;
        final ReadContext context;
        final Codec<T> typeCodec;
        private final NbtList list;

        NbtTypedListReadView(ErrorReporter reporter, String name, ReadContext context, Codec<T> typeCodec, NbtList list) {
            this.reporter = reporter;
            this.name = name;
            this.context = context;
            this.typeCodec = typeCodec;
            this.list = list;
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        void reportDecodeAtIndexError(int index, NbtElement element, DataResult.Error<?> error) {
            this.reporter.report(new DecodeAtIndexError(this.name, index, element, error));
        }

        @Override
        public Stream<T> stream() {
            return Streams.mapWithIndex(this.list.stream(), (element, index) -> {
                DataResult dataResult = this.typeCodec.parse(this.context.getOps(), element);
                Objects.requireNonNull(dataResult);
                DataResult dataResult2 = dataResult;
                int i = 0;
                return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, dataResult2, i)) {
                    default -> throw new MatchException(null, null);
                    case 0 -> {
                        DataResult.Success success = (DataResult.Success)dataResult2;
                        yield success.value();
                    }
                    case 1 -> {
                        DataResult.Error error = (DataResult.Error)dataResult2;
                        this.reportDecodeAtIndexError((int)index, (NbtElement)element, error);
                        yield error.partialValue().orElse(null);
                    }
                };
            }).filter(Objects::nonNull);
        }

        @Override
        public Iterator<T> iterator() {
            final ListIterator listIterator = this.list.listIterator();
            return new AbstractIterator<T>(){

                @Override
                @Nullable
                protected T computeNext() {
                    while (listIterator.hasNext()) {
                        DataResult dataResult;
                        int i = listIterator.nextIndex();
                        NbtElement lv = (NbtElement)listIterator.next();
                        Objects.requireNonNull(typeCodec.parse(context.getOps(), lv));
                        int n = 0;
                        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, dataResult, n)) {
                            default: {
                                throw new MatchException(null, null);
                            }
                            case 0: {
                                DataResult.Success success = (DataResult.Success)dataResult;
                                return success.value();
                            }
                            case 1: 
                        }
                        DataResult.Error error = (DataResult.Error)dataResult;
                        this.reportDecodeAtIndexError(i, lv, error);
                        if (!error.partialValue().isPresent()) continue;
                        return error.partialValue().get();
                    }
                    return this.endOfData();
                }
            };
        }
    }

    public record ExpectedTypeAtIndexError(String name, int index, NbtType<?> expected, NbtType<?> actual) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Expected list '" + this.name + "' to contain at index " + this.index + " value of type " + this.expected.getCrashReportName() + ", but got " + this.actual.getCrashReportName();
        }
    }

    public record DecodeAtIndexError(String name, int index, NbtElement element, DataResult.Error<?> error) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Failed to decode value '" + String.valueOf(this.element) + "' from field '" + this.name + "' at index " + this.index + "': " + this.error.message();
        }
    }
}

