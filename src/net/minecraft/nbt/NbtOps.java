/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class NbtOps
implements DynamicOps<NbtElement> {
    public static final NbtOps INSTANCE = new NbtOps();

    private NbtOps() {
    }

    @Override
    public NbtElement empty() {
        return NbtEnd.INSTANCE;
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public <U> U convertTo(DynamicOps<U> dynamicOps, NbtElement arg) {
        U u;
        NbtElement nbtElement = arg;
        Objects.requireNonNull(nbtElement);
        NbtElement nbtElement2 = nbtElement;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{NbtEnd.class, NbtByte.class, NbtShort.class, NbtInt.class, NbtLong.class, NbtFloat.class, NbtDouble.class, NbtByteArray.class, NbtString.class, NbtList.class, NbtCompound.class, NbtIntArray.class, NbtLongArray.class}, (Object)nbtElement2, n)) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                NbtEnd lv = (NbtEnd)nbtElement2;
                u = dynamicOps.empty();
                return u;
            }
            case 1: {
                NbtByte nbtByte = (NbtByte)nbtElement2;
                try {
                    byte by;
                    byte b = by = nbtByte.value();
                    u = dynamicOps.createByte(b);
                    return u;
                } catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 2: {
                NbtShort nbtShort = (NbtShort)nbtElement2;
                {
                    short s;
                    short s2 = s = nbtShort.value();
                    u = dynamicOps.createShort(s2);
                    return u;
                }
            }
            case 3: {
                NbtInt nbtInt = (NbtInt)nbtElement2;
                {
                    int n2;
                    int i = n2 = nbtInt.value();
                    u = dynamicOps.createInt(i);
                    return u;
                }
            }
            case 4: {
                NbtLong nbtLong = (NbtLong)nbtElement2;
                {
                    long l;
                    long l2 = l = nbtLong.value();
                    u = dynamicOps.createLong(l2);
                    return u;
                }
            }
            case 5: {
                NbtFloat nbtFloat = (NbtFloat)nbtElement2;
                {
                    float f;
                    float f2 = f = nbtFloat.value();
                    u = dynamicOps.createFloat(f2);
                    return u;
                }
            }
            case 6: {
                NbtDouble nbtDouble = (NbtDouble)nbtElement2;
                {
                    double d;
                    double d2 = d = nbtDouble.value();
                    u = dynamicOps.createDouble(d2);
                    return u;
                }
            }
            case 7: {
                NbtByteArray lv2 = (NbtByteArray)nbtElement2;
                u = dynamicOps.createByteList(ByteBuffer.wrap(lv2.getByteArray()));
                return u;
            }
            case 8: {
                NbtString nbtString = (NbtString)nbtElement2;
                {
                    String string;
                    String string2 = string = nbtString.value();
                    u = dynamicOps.createString(string2);
                    return u;
                }
            }
            case 9: {
                NbtList lv3 = (NbtList)nbtElement2;
                u = this.convertList(dynamicOps, lv3);
                return u;
            }
            case 10: {
                NbtCompound lv4 = (NbtCompound)nbtElement2;
                u = this.convertMap(dynamicOps, lv4);
                return u;
            }
            case 11: {
                NbtIntArray lv5 = (NbtIntArray)nbtElement2;
                u = dynamicOps.createIntList(Arrays.stream(lv5.getIntArray()));
                return u;
            }
            case 12: 
        }
        NbtLongArray lv6 = (NbtLongArray)nbtElement2;
        u = dynamicOps.createLongList(Arrays.stream(lv6.getLongArray()));
        return u;
    }

    @Override
    public DataResult<Number> getNumberValue(NbtElement arg) {
        return arg.asNumber().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Not a number"));
    }

    @Override
    public NbtElement createNumeric(Number number) {
        return NbtDouble.of(number.doubleValue());
    }

    @Override
    public NbtElement createByte(byte b) {
        return NbtByte.of(b);
    }

    @Override
    public NbtElement createShort(short s) {
        return NbtShort.of(s);
    }

    @Override
    public NbtElement createInt(int i) {
        return NbtInt.of(i);
    }

    @Override
    public NbtElement createLong(long l) {
        return NbtLong.of(l);
    }

    @Override
    public NbtElement createFloat(float f) {
        return NbtFloat.of(f);
    }

    @Override
    public NbtElement createDouble(double d) {
        return NbtDouble.of(d);
    }

    @Override
    public NbtElement createBoolean(boolean bl) {
        return NbtByte.of(bl);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public DataResult<String> getStringValue(NbtElement arg) {
        String string2;
        if (!(arg instanceof NbtString)) return DataResult.error(() -> "Not a string");
        NbtString nbtString = (NbtString)arg;
        try {
            String string;
            string2 = string = nbtString.value();
        } catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        return DataResult.success(string2);
    }

    @Override
    public NbtElement createString(String string) {
        return NbtString.of(string);
    }

    @Override
    public DataResult<NbtElement> mergeToList(NbtElement arg, NbtElement arg2) {
        return NbtOps.createMerger(arg).map(merger -> DataResult.success(merger.merge(arg2).getResult())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(arg), arg));
    }

    @Override
    public DataResult<NbtElement> mergeToList(NbtElement arg, List<NbtElement> list) {
        return NbtOps.createMerger(arg).map(merger -> DataResult.success(merger.merge(list).getResult())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(arg), arg));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public DataResult<NbtElement> mergeToMap(NbtElement arg, NbtElement arg2, NbtElement arg3) {
        NbtCompound nbtCompound;
        String string;
        if (!(arg instanceof NbtCompound) && !(arg instanceof NbtEnd)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(arg), arg);
        }
        if (!(arg2 instanceof NbtString)) return DataResult.error(() -> "key is not a string: " + String.valueOf(arg2), arg);
        NbtString nbtString = (NbtString)arg2;
        try {
            String string2;
            string = string2 = nbtString.value();
        } catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            nbtCompound = lv.shallowCopy();
        } else {
            nbtCompound = new NbtCompound();
        }
        NbtCompound lv2 = nbtCompound;
        lv2.put(string, arg3);
        return DataResult.success(lv2);
    }

    @Override
    public DataResult<NbtElement> mergeToMap(NbtElement arg, MapLike<NbtElement> mapLike) {
        NbtCompound nbtCompound;
        if (!(arg instanceof NbtCompound) && !(arg instanceof NbtEnd)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(arg), arg);
        }
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            nbtCompound = lv.shallowCopy();
        } else {
            nbtCompound = new NbtCompound();
        }
        NbtCompound lv2 = nbtCompound;
        ArrayList list = new ArrayList();
        mapLike.entries().forEach(pair -> {
            String string2;
            NbtElement lv = (NbtElement)pair.getFirst();
            if (!(lv instanceof NbtString)) {
                list.add(lv);
                return;
            }
            NbtString lv2 = (NbtString)lv;
            try {
                String string;
                string2 = string = lv2.value();
            } catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
            lv2.put(string2, (NbtElement)pair.getSecond());
        });
        if (!list.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + String.valueOf(list), lv2);
        }
        return DataResult.success(lv2);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public DataResult<NbtElement> mergeToMap(NbtElement arg, Map<NbtElement, NbtElement> map) {
        NbtCompound nbtCompound;
        if (!(arg instanceof NbtCompound) && !(arg instanceof NbtEnd)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(arg), arg);
        }
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            nbtCompound = lv.shallowCopy();
        } else {
            nbtCompound = new NbtCompound();
        }
        NbtCompound lv2 = nbtCompound;
        ArrayList<NbtElement> list = new ArrayList<NbtElement>();
        for (Map.Entry<NbtElement, NbtElement> entry : map.entrySet()) {
            NbtElement lv3 = entry.getKey();
            if (lv3 instanceof NbtString) {
                NbtString nbtString = (NbtString)lv3;
                try {
                    String string;
                    String string2 = string = nbtString.value();
                    lv2.put(string2, entry.getValue());
                    continue;
                } catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            list.add(lv3);
        }
        if (!list.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + String.valueOf(list), lv2);
        }
        return DataResult.success(lv2);
    }

    @Override
    public DataResult<Stream<Pair<NbtElement, NbtElement>>> getMapValues(NbtElement arg) {
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            return DataResult.success(lv.entrySet().stream().map(entry -> Pair.of(this.createString((String)entry.getKey()), (NbtElement)entry.getValue())));
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(arg));
    }

    @Override
    public DataResult<Consumer<BiConsumer<NbtElement, NbtElement>>> getMapEntries(NbtElement arg) {
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            return DataResult.success(biConsumer -> {
                for (Map.Entry<String, NbtElement> entry : lv.entrySet()) {
                    biConsumer.accept(this.createString(entry.getKey()), entry.getValue());
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(arg));
    }

    @Override
    public DataResult<MapLike<NbtElement>> getMap(NbtElement arg) {
        if (arg instanceof NbtCompound) {
            final NbtCompound lv = (NbtCompound)arg;
            return DataResult.success(new MapLike<NbtElement>(){

                /*
                 * Enabled force condition propagation
                 * Lifted jumps to return sites
                 */
                @Override
                @Nullable
                public NbtElement get(NbtElement arg) {
                    if (!(arg instanceof NbtString)) throw new UnsupportedOperationException("Cannot get map entry with non-string key: " + String.valueOf(arg));
                    NbtString nbtString = (NbtString)arg;
                    try {
                        String string;
                        String string2 = string = nbtString.value();
                        return lv.get(string2);
                    } catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                }

                @Override
                @Nullable
                public NbtElement get(String string) {
                    return lv.get(string);
                }

                @Override
                public Stream<Pair<NbtElement, NbtElement>> entries() {
                    return lv.entrySet().stream().map(entry -> Pair.of(NbtOps.this.createString((String)entry.getKey()), (NbtElement)entry.getValue()));
                }

                public String toString() {
                    return "MapLike[" + String.valueOf(lv) + "]";
                }

                @Override
                @Nullable
                public /* synthetic */ Object get(String key) {
                    return this.get(key);
                }

                @Override
                @Nullable
                public /* synthetic */ Object get(Object nbt) {
                    return this.get((NbtElement)nbt);
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(arg));
    }

    @Override
    public NbtElement createMap(Stream<Pair<NbtElement, NbtElement>> stream) {
        NbtCompound lv = new NbtCompound();
        stream.forEach(entry -> {
            NbtElement lv = (NbtElement)entry.getFirst();
            NbtElement lv2 = (NbtElement)entry.getSecond();
            if (!(lv instanceof NbtString)) throw new UnsupportedOperationException("Cannot create map with non-string key: " + String.valueOf(lv));
            NbtString lv3 = (NbtString)lv;
            try {
                String string;
                String string2 = string = lv3.value();
                lv.put(string2, lv2);
            } catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
        });
        return lv;
    }

    @Override
    public DataResult<Stream<NbtElement>> getStream(NbtElement arg) {
        if (arg instanceof AbstractNbtList) {
            AbstractNbtList lv = (AbstractNbtList)arg;
            return DataResult.success(lv.stream());
        }
        return DataResult.error(() -> "Not a list");
    }

    @Override
    public DataResult<Consumer<Consumer<NbtElement>>> getList(NbtElement arg) {
        if (arg instanceof AbstractNbtList) {
            AbstractNbtList lv = (AbstractNbtList)arg;
            return DataResult.success(lv::forEach);
        }
        return DataResult.error(() -> "Not a list: " + String.valueOf(arg));
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(NbtElement arg) {
        if (arg instanceof NbtByteArray) {
            NbtByteArray lv = (NbtByteArray)arg;
            return DataResult.success(ByteBuffer.wrap(lv.getByteArray()));
        }
        return DynamicOps.super.getByteBuffer(arg);
    }

    @Override
    public NbtElement createByteList(ByteBuffer byteBuffer) {
        ByteBuffer byteBuffer2 = byteBuffer.duplicate().clear();
        byte[] bs = new byte[byteBuffer.capacity()];
        byteBuffer2.get(0, bs, 0, bs.length);
        return new NbtByteArray(bs);
    }

    @Override
    public DataResult<IntStream> getIntStream(NbtElement arg) {
        if (arg instanceof NbtIntArray) {
            NbtIntArray lv = (NbtIntArray)arg;
            return DataResult.success(Arrays.stream(lv.getIntArray()));
        }
        return DynamicOps.super.getIntStream(arg);
    }

    @Override
    public NbtElement createIntList(IntStream intStream) {
        return new NbtIntArray(intStream.toArray());
    }

    @Override
    public DataResult<LongStream> getLongStream(NbtElement arg) {
        if (arg instanceof NbtLongArray) {
            NbtLongArray lv = (NbtLongArray)arg;
            return DataResult.success(Arrays.stream(lv.getLongArray()));
        }
        return DynamicOps.super.getLongStream(arg);
    }

    @Override
    public NbtElement createLongList(LongStream longStream) {
        return new NbtLongArray(longStream.toArray());
    }

    @Override
    public NbtElement createList(Stream<NbtElement> stream) {
        return new NbtList(stream.collect(Util.toArrayList()));
    }

    @Override
    public NbtElement remove(NbtElement arg, String string) {
        if (arg instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)arg;
            NbtCompound lv2 = lv.shallowCopy();
            lv2.remove(string);
            return lv2;
        }
        return arg;
    }

    public String toString() {
        return "NBT";
    }

    @Override
    public RecordBuilder<NbtElement> mapBuilder() {
        return new MapBuilder(this);
    }

    private static Optional<Merger> createMerger(NbtElement nbt) {
        if (nbt instanceof NbtEnd) {
            return Optional.of(new CompoundListMerger());
        }
        if (nbt instanceof AbstractNbtList) {
            AbstractNbtList lv = (AbstractNbtList)nbt;
            if (lv.isEmpty()) {
                return Optional.of(new CompoundListMerger());
            }
            AbstractNbtList abstractNbtList = lv;
            Objects.requireNonNull(abstractNbtList);
            AbstractNbtList abstractNbtList2 = abstractNbtList;
            int n = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{NbtList.class, NbtByteArray.class, NbtIntArray.class, NbtLongArray.class}, (Object)abstractNbtList2, n)) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    NbtList lv2 = (NbtList)abstractNbtList2;
                    yield Optional.of(new CompoundListMerger(lv2));
                }
                case 1 -> {
                    NbtByteArray lv3 = (NbtByteArray)abstractNbtList2;
                    yield Optional.of(new ByteArrayMerger(lv3.getByteArray()));
                }
                case 2 -> {
                    NbtIntArray lv4 = (NbtIntArray)abstractNbtList2;
                    yield Optional.of(new IntArrayMerger(lv4.getIntArray()));
                }
                case 3 -> {
                    NbtLongArray lv5 = (NbtLongArray)abstractNbtList2;
                    yield Optional.of(new LongArrayMerger(lv5.getLongArray()));
                }
            };
        }
        return Optional.empty();
    }

    @Override
    public /* synthetic */ Object remove(Object element, String key) {
        return this.remove((NbtElement)element, key);
    }

    @Override
    public /* synthetic */ Object createLongList(LongStream stream) {
        return this.createLongList(stream);
    }

    @Override
    public /* synthetic */ DataResult getLongStream(Object element) {
        return this.getLongStream((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createIntList(IntStream stream) {
        return this.createIntList(stream);
    }

    @Override
    public /* synthetic */ DataResult getIntStream(Object element) {
        return this.getIntStream((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createByteList(ByteBuffer buf) {
        return this.createByteList(buf);
    }

    @Override
    public /* synthetic */ DataResult getByteBuffer(Object element) {
        return this.getByteBuffer((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    @Override
    public /* synthetic */ DataResult getList(Object element) {
        return this.getList((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult getStream(Object element) {
        return this.getStream((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult getMap(Object element) {
        return this.getMap((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createMap(Stream entries) {
        return this.createMap(entries);
    }

    @Override
    public /* synthetic */ DataResult getMapEntries(Object element) {
        return this.getMapEntries((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult getMapValues(Object element) {
        return this.getMapValues((NbtElement)element);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object element, MapLike map) {
        return this.mergeToMap((NbtElement)element, (MapLike<NbtElement>)map);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object nbt, Map map) {
        return this.mergeToMap((NbtElement)nbt, (Map<NbtElement, NbtElement>)map);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object map, Object key, Object value) {
        return this.mergeToMap((NbtElement)map, (NbtElement)key, (NbtElement)value);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object list, List values) {
        return this.mergeToList((NbtElement)list, (List<NbtElement>)values);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object list, Object value) {
        return this.mergeToList((NbtElement)list, (NbtElement)value);
    }

    @Override
    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    @Override
    public /* synthetic */ DataResult getStringValue(Object element) {
        return this.getStringValue((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object createBoolean(boolean value) {
        return this.createBoolean(value);
    }

    @Override
    public /* synthetic */ Object createDouble(double value) {
        return this.createDouble(value);
    }

    @Override
    public /* synthetic */ Object createFloat(float value) {
        return this.createFloat(value);
    }

    @Override
    public /* synthetic */ Object createLong(long value) {
        return this.createLong(value);
    }

    @Override
    public /* synthetic */ Object createInt(int value) {
        return this.createInt(value);
    }

    @Override
    public /* synthetic */ Object createShort(short value) {
        return this.createShort(value);
    }

    @Override
    public /* synthetic */ Object createByte(byte value) {
        return this.createByte(value);
    }

    @Override
    public /* synthetic */ Object createNumeric(Number value) {
        return this.createNumeric(value);
    }

    @Override
    public /* synthetic */ DataResult getNumberValue(Object element) {
        return this.getNumberValue((NbtElement)element);
    }

    @Override
    public /* synthetic */ Object convertTo(DynamicOps ops, Object element) {
        return this.convertTo(ops, (NbtElement)element);
    }

    @Override
    public /* synthetic */ Object empty() {
        return this.empty();
    }

    class MapBuilder
    extends RecordBuilder.AbstractStringBuilder<NbtElement, NbtCompound> {
        protected MapBuilder(NbtOps ops) {
            super(ops);
        }

        @Override
        protected NbtCompound initBuilder() {
            return new NbtCompound();
        }

        @Override
        protected NbtCompound append(String string, NbtElement arg, NbtCompound arg2) {
            arg2.put(string, arg);
            return arg2;
        }

        @Override
        protected DataResult<NbtElement> build(NbtCompound arg, NbtElement arg2) {
            if (arg2 == null || arg2 == NbtEnd.INSTANCE) {
                return DataResult.success(arg);
            }
            if (arg2 instanceof NbtCompound) {
                NbtCompound lv = (NbtCompound)arg2;
                NbtCompound lv2 = lv.shallowCopy();
                for (Map.Entry<String, NbtElement> entry : arg.entrySet()) {
                    lv2.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success(lv2);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(arg2), arg2);
        }

        @Override
        protected /* synthetic */ Object append(String key, Object value, Object nbt) {
            return this.append(key, (NbtElement)value, (NbtCompound)nbt);
        }

        @Override
        protected /* synthetic */ DataResult build(Object nbt, Object mergedValue) {
            return this.build((NbtCompound)nbt, (NbtElement)mergedValue);
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    static class CompoundListMerger
    implements Merger {
        private final NbtList list = new NbtList();

        CompoundListMerger() {
        }

        CompoundListMerger(NbtList arg) {
            this.list.addAll(arg);
        }

        public CompoundListMerger(IntArrayList list) {
            list.forEach(value -> this.list.add(NbtInt.of(value)));
        }

        public CompoundListMerger(ByteArrayList list) {
            list.forEach(value -> this.list.add(NbtByte.of(value)));
        }

        public CompoundListMerger(LongArrayList list) {
            list.forEach(value -> this.list.add(NbtLong.of(value)));
        }

        @Override
        public Merger merge(NbtElement nbt) {
            this.list.add(nbt);
            return this;
        }

        @Override
        public NbtElement getResult() {
            return this.list;
        }
    }

    static class ByteArrayMerger
    implements Merger {
        private final ByteArrayList list = new ByteArrayList();

        public ByteArrayMerger(byte[] values) {
            this.list.addElements(0, values);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtByte) {
                NbtByte lv = (NbtByte)nbt;
                this.list.add(lv.byteValue());
                return this;
            }
            return new CompoundListMerger(this.list).merge(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtByteArray(this.list.toByteArray());
        }
    }

    static class IntArrayMerger
    implements Merger {
        private final IntArrayList list = new IntArrayList();

        public IntArrayMerger(int[] values) {
            this.list.addElements(0, values);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtInt) {
                NbtInt lv = (NbtInt)nbt;
                this.list.add(lv.intValue());
                return this;
            }
            return new CompoundListMerger(this.list).merge(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtIntArray(this.list.toIntArray());
        }
    }

    static class LongArrayMerger
    implements Merger {
        private final LongArrayList list = new LongArrayList();

        public LongArrayMerger(long[] values) {
            this.list.addElements(0, values);
        }

        @Override
        public Merger merge(NbtElement nbt) {
            if (nbt instanceof NbtLong) {
                NbtLong lv = (NbtLong)nbt;
                this.list.add(lv.longValue());
                return this;
            }
            return new CompoundListMerger(this.list).merge(nbt);
        }

        @Override
        public NbtElement getResult() {
            return new NbtLongArray(this.list.toLongArray());
        }
    }

    static interface Merger {
        public Merger merge(NbtElement var1);

        default public Merger merge(Iterable<NbtElement> nbts) {
            Merger lv = this;
            for (NbtElement lv2 : nbts) {
                lv = lv.merge(lv2);
            }
            return lv;
        }

        default public Merger merge(Stream<NbtElement> nbts) {
            return this.merge(nbts::iterator);
        }

        public NbtElement getResult();
    }
}

