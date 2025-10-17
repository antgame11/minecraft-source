/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.dynamic;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.util.dynamic.AbstractListBuilder;

public class HashCodeOps
implements DynamicOps<HashCode> {
    private static final byte field_58094 = 1;
    private static final byte field_58095 = 2;
    private static final byte field_58096 = 3;
    private static final byte field_58097 = 4;
    private static final byte field_58098 = 5;
    private static final byte field_58099 = 6;
    private static final byte field_58100 = 7;
    private static final byte field_58101 = 8;
    private static final byte field_58102 = 9;
    private static final byte field_58103 = 10;
    private static final byte field_58104 = 11;
    private static final byte field_58105 = 12;
    private static final byte field_58106 = 13;
    private static final byte field_58107 = 14;
    private static final byte field_58108 = 15;
    private static final byte field_58109 = 16;
    private static final byte field_58110 = 17;
    private static final byte field_58111 = 18;
    private static final byte field_58112 = 19;
    private static final byte[] emptyByteArray = new byte[]{1};
    private static final byte[] falseByteArray = new byte[]{13, 0};
    private static final byte[] trueByteArray = new byte[]{13, 1};
    public static final byte[] emptyMapByteArray = new byte[]{2, 3};
    public static final byte[] emptyListByteArray = new byte[]{4, 5};
    private static final DataResult<Object> ERROR = DataResult.error(() -> "Unsupported operation");
    private static final Comparator<HashCode> HASH_CODE_COMPARATOR = Comparator.comparingLong(HashCode::padToLong);
    private static final Comparator<Map.Entry<HashCode, HashCode>> ENTRY_COMPARATOR = Map.Entry.comparingByKey(HASH_CODE_COMPARATOR).thenComparing(Map.Entry.comparingByValue(HASH_CODE_COMPARATOR));
    private static final Comparator<Pair<HashCode, HashCode>> PAIR_COMPARATOR = Comparator.comparing(Pair::getFirst, HASH_CODE_COMPARATOR).thenComparing(Pair::getSecond, HASH_CODE_COMPARATOR);
    public static final HashCodeOps INSTANCE = new HashCodeOps(Hashing.crc32c());
    final HashFunction function;
    final HashCode empty;
    private final HashCode emptyMap;
    private final HashCode emptyList;
    private final HashCode hashTrue;
    private final HashCode hashFalse;

    public HashCodeOps(HashFunction function) {
        this.function = function;
        this.empty = function.hashBytes(emptyByteArray);
        this.emptyMap = function.hashBytes(emptyMapByteArray);
        this.emptyList = function.hashBytes(emptyListByteArray);
        this.hashFalse = function.hashBytes(falseByteArray);
        this.hashTrue = function.hashBytes(trueByteArray);
    }

    @Override
    public HashCode empty() {
        return this.empty;
    }

    @Override
    public HashCode emptyMap() {
        return this.emptyMap;
    }

    @Override
    public HashCode emptyList() {
        return this.emptyList;
    }

    @Override
    public HashCode createNumeric(Number number) {
        Number number2 = number;
        Objects.requireNonNull(number2);
        Number number3 = number2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Byte.class, Short.class, Integer.class, Long.class, Double.class, Float.class}, (Object)number3, n)) {
            case 0 -> {
                Byte byte_ = (Byte)number3;
                yield this.createByte(byte_);
            }
            case 1 -> {
                Short short_ = (Short)number3;
                yield this.createShort(short_);
            }
            case 2 -> {
                Integer integer = (Integer)number3;
                yield this.createInt(integer);
            }
            case 3 -> {
                Long long_ = (Long)number3;
                yield this.createLong(long_);
            }
            case 4 -> {
                Double double_ = (Double)number3;
                yield this.createDouble(double_);
            }
            case 5 -> {
                Float float_ = (Float)number3;
                yield this.createFloat(float_.floatValue());
            }
            default -> this.createDouble(number.doubleValue());
        };
    }

    @Override
    public HashCode createByte(byte b) {
        return this.function.newHasher(2).putByte((byte)6).putByte(b).hash();
    }

    @Override
    public HashCode createShort(short s) {
        return this.function.newHasher(3).putByte((byte)7).putShort(s).hash();
    }

    @Override
    public HashCode createInt(int i) {
        return this.function.newHasher(5).putByte((byte)8).putInt(i).hash();
    }

    @Override
    public HashCode createLong(long l) {
        return this.function.newHasher(9).putByte((byte)9).putLong(l).hash();
    }

    @Override
    public HashCode createFloat(float f) {
        return this.function.newHasher(5).putByte((byte)10).putFloat(f).hash();
    }

    @Override
    public HashCode createDouble(double d) {
        return this.function.newHasher(9).putByte((byte)11).putDouble(d).hash();
    }

    @Override
    public HashCode createString(String string) {
        return this.function.newHasher().putByte((byte)12).putInt(string.length()).putUnencodedChars(string).hash();
    }

    @Override
    public HashCode createBoolean(boolean bl) {
        return bl ? this.hashTrue : this.hashFalse;
    }

    private static Hasher hash(Hasher hasher, Map<HashCode, HashCode> map) {
        hasher.putByte((byte)2);
        map.entrySet().stream().sorted(ENTRY_COMPARATOR).forEach(entry -> hasher.putBytes(((HashCode)entry.getKey()).asBytes()).putBytes(((HashCode)entry.getValue()).asBytes()));
        hasher.putByte((byte)3);
        return hasher;
    }

    static Hasher hash(Hasher hasher, Stream<Pair<HashCode, HashCode>> pairs) {
        hasher.putByte((byte)2);
        pairs.sorted(PAIR_COMPARATOR).forEach(pair -> hasher.putBytes(((HashCode)pair.getFirst()).asBytes()).putBytes(((HashCode)pair.getSecond()).asBytes()));
        hasher.putByte((byte)3);
        return hasher;
    }

    @Override
    public HashCode createMap(Stream<Pair<HashCode, HashCode>> stream) {
        return HashCodeOps.hash(this.function.newHasher(), stream).hash();
    }

    @Override
    public HashCode createMap(Map<HashCode, HashCode> map) {
        return HashCodeOps.hash(this.function.newHasher(), map).hash();
    }

    @Override
    public HashCode createList(Stream<HashCode> stream) {
        Hasher hasher = this.function.newHasher();
        hasher.putByte((byte)4);
        stream.forEach(hashCode -> hasher.putBytes(hashCode.asBytes()));
        hasher.putByte((byte)5);
        return hasher.hash();
    }

    @Override
    public HashCode createByteList(ByteBuffer byteBuffer) {
        Hasher hasher = this.function.newHasher();
        hasher.putByte((byte)14);
        hasher.putBytes(byteBuffer);
        hasher.putByte((byte)15);
        return hasher.hash();
    }

    @Override
    public HashCode createIntList(IntStream intStream) {
        Hasher hasher = this.function.newHasher();
        hasher.putByte((byte)16);
        intStream.forEach(hasher::putInt);
        hasher.putByte((byte)17);
        return hasher.hash();
    }

    @Override
    public HashCode createLongList(LongStream longStream) {
        Hasher hasher = this.function.newHasher();
        hasher.putByte((byte)18);
        longStream.forEach(hasher::putLong);
        hasher.putByte((byte)19);
        return hasher.hash();
    }

    @Override
    public HashCode remove(HashCode hashCode, String string) {
        return hashCode;
    }

    @Override
    public RecordBuilder<HashCode> mapBuilder() {
        return new Builder();
    }

    @Override
    public com.mojang.serialization.ListBuilder<HashCode> listBuilder() {
        return new ListBuilder();
    }

    public String toString() {
        return "Hash " + String.valueOf(this.function);
    }

    @Override
    public <U> U convertTo(DynamicOps<U> dynamicOps, HashCode hashCode) {
        throw new UnsupportedOperationException("Can't convert from this type");
    }

    @Override
    public Number getNumberValue(HashCode hashCode, Number number) {
        return number;
    }

    @Override
    public HashCode set(HashCode hashCode, String string, HashCode hashCode2) {
        return hashCode;
    }

    @Override
    public HashCode update(HashCode hashCode, String string, Function<HashCode, HashCode> function) {
        return hashCode;
    }

    @Override
    public HashCode updateGeneric(HashCode hashCode, HashCode hashCode2, Function<HashCode, HashCode> function) {
        return hashCode;
    }

    private static <T> DataResult<T> error() {
        return ERROR;
    }

    @Override
    public DataResult<HashCode> get(HashCode hashCode, String string) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<HashCode> getGeneric(HashCode hashCode, HashCode hashCode2) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<Number> getNumberValue(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<Boolean> getBooleanValue(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<String> getStringValue(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<HashCode> mergeToList(HashCode hashCode, HashCode hashCode2) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<HashCode> mergeToList(HashCode hashCode, List<HashCode> list) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<HashCode> mergeToMap(HashCode hashCode, HashCode hashCode2, HashCode hashCode3) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<HashCode> mergeToMap(HashCode hashCode, Map<HashCode, HashCode> map) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<HashCode> mergeToMap(HashCode hashCode, MapLike<HashCode> mapLike) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<Stream<Pair<HashCode, HashCode>>> getMapValues(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<Consumer<BiConsumer<HashCode, HashCode>>> getMapEntries(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<Stream<HashCode>> getStream(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<Consumer<Consumer<HashCode>>> getList(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<MapLike<HashCode>> getMap(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<IntStream> getIntStream(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public DataResult<LongStream> getLongStream(HashCode hashCode) {
        return HashCodeOps.error();
    }

    @Override
    public /* synthetic */ Object updateGeneric(Object object, Object object2, Function function) {
        return this.updateGeneric((HashCode)object, (HashCode)object2, (Function<HashCode, HashCode>)function);
    }

    @Override
    public /* synthetic */ Object update(Object object, String string, Function function) {
        return this.update((HashCode)object, string, (Function<HashCode, HashCode>)function);
    }

    @Override
    public /* synthetic */ Object set(Object object, String string, Object object2) {
        return this.set((HashCode)object, string, (HashCode)object2);
    }

    @Override
    public /* synthetic */ DataResult getGeneric(Object object, Object object2) {
        return this.getGeneric((HashCode)object, (HashCode)object2);
    }

    @Override
    public /* synthetic */ DataResult get(Object object, String string) {
        return this.get((HashCode)object, string);
    }

    @Override
    public /* synthetic */ Object remove(Object object, String string) {
        return this.remove((HashCode)object, string);
    }

    @Override
    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    @Override
    public /* synthetic */ DataResult getLongStream(Object object) {
        return this.getLongStream((HashCode)object);
    }

    @Override
    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    @Override
    public /* synthetic */ DataResult getIntStream(Object object) {
        return this.getIntStream((HashCode)object);
    }

    @Override
    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    @Override
    public /* synthetic */ DataResult getByteBuffer(Object object) {
        return this.getByteBuffer((HashCode)object);
    }

    @Override
    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    @Override
    public /* synthetic */ DataResult getList(Object object) {
        return this.getList((HashCode)object);
    }

    @Override
    public /* synthetic */ DataResult getStream(Object object) {
        return this.getStream((HashCode)object);
    }

    @Override
    public /* synthetic */ Object createMap(Map map) {
        return this.createMap(map);
    }

    @Override
    public /* synthetic */ DataResult getMap(Object object) {
        return this.getMap((HashCode)object);
    }

    @Override
    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap(stream);
    }

    @Override
    public /* synthetic */ DataResult getMapEntries(Object object) {
        return this.getMapEntries((HashCode)object);
    }

    @Override
    public /* synthetic */ DataResult getMapValues(Object object) {
        return this.getMapValues((HashCode)object);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object object, MapLike mapLike) {
        return this.mergeToMap((HashCode)object, (MapLike<HashCode>)mapLike);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object object, Map map) {
        return this.mergeToMap((HashCode)object, (Map<HashCode, HashCode>)map);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object object, Object object2, Object object3) {
        return this.mergeToMap((HashCode)object, (HashCode)object2, (HashCode)object3);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((HashCode)object, (List<HashCode>)list);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object object, Object object2) {
        return this.mergeToList((HashCode)object, (HashCode)object2);
    }

    @Override
    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    @Override
    public /* synthetic */ DataResult getStringValue(Object object) {
        return this.getStringValue((HashCode)object);
    }

    @Override
    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
    }

    @Override
    public /* synthetic */ DataResult getBooleanValue(Object object) {
        return this.getBooleanValue((HashCode)object);
    }

    @Override
    public /* synthetic */ Object createDouble(double d) {
        return this.createDouble(d);
    }

    @Override
    public /* synthetic */ Object createFloat(float f) {
        return this.createFloat(f);
    }

    @Override
    public /* synthetic */ Object createLong(long l) {
        return this.createLong(l);
    }

    @Override
    public /* synthetic */ Object createInt(int i) {
        return this.createInt(i);
    }

    @Override
    public /* synthetic */ Object createShort(short s) {
        return this.createShort(s);
    }

    @Override
    public /* synthetic */ Object createByte(byte b) {
        return this.createByte(b);
    }

    @Override
    public /* synthetic */ Object createNumeric(Number number) {
        return this.createNumeric(number);
    }

    @Override
    public /* synthetic */ Number getNumberValue(Object object, Number number) {
        return this.getNumberValue((HashCode)object, number);
    }

    @Override
    public /* synthetic */ DataResult getNumberValue(Object object) {
        return this.getNumberValue((HashCode)object);
    }

    @Override
    public /* synthetic */ Object convertTo(DynamicOps dynamicOps, Object object) {
        return this.convertTo(dynamicOps, (HashCode)object);
    }

    @Override
    public /* synthetic */ Object emptyList() {
        return this.emptyList();
    }

    @Override
    public /* synthetic */ Object emptyMap() {
        return this.emptyMap();
    }

    @Override
    public /* synthetic */ Object empty() {
        return this.empty();
    }

    final class Builder
    extends RecordBuilder.AbstractUniversalBuilder<HashCode, List<Pair<HashCode, HashCode>>> {
        public Builder() {
            super(HashCodeOps.this);
        }

        @Override
        protected List<Pair<HashCode, HashCode>> initBuilder() {
            return new ArrayList<Pair<HashCode, HashCode>>();
        }

        @Override
        protected List<Pair<HashCode, HashCode>> append(HashCode hashCode, HashCode hashCode2, List<Pair<HashCode, HashCode>> list) {
            list.add(Pair.of(hashCode, hashCode2));
            return list;
        }

        @Override
        protected DataResult<HashCode> build(List<Pair<HashCode, HashCode>> list, HashCode hashCode) {
            assert (hashCode.equals(HashCodeOps.this.empty()));
            return DataResult.success(HashCodeOps.hash(HashCodeOps.this.function.newHasher(), list.stream()).hash());
        }

        @Override
        protected /* synthetic */ Object append(Object object, Object object2, Object object3) {
            return this.append((HashCode)object, (HashCode)object2, (List)object3);
        }

        @Override
        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((List)object, (HashCode)object2);
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    class ListBuilder
    extends AbstractListBuilder<HashCode, Hasher> {
        public ListBuilder() {
            super(HashCodeOps.this);
        }

        @Override
        protected Hasher initBuilder() {
            return HashCodeOps.this.function.newHasher().putByte((byte)4);
        }

        @Override
        protected Hasher add(Hasher hasher, HashCode hashCode) {
            return hasher.putBytes(hashCode.asBytes());
        }

        @Override
        protected DataResult<HashCode> build(Hasher hasher, HashCode hashCode) {
            assert (hashCode.equals(HashCodeOps.this.empty));
            hasher.putByte((byte)5);
            return DataResult.success(hasher.hash());
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }
}

