/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.nbt.visitor.StringNbtWriter;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class NbtCompound
implements NbtElement {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<NbtCompound> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        NbtElement lv = dynamic.convert(NbtOps.INSTANCE).getValue();
        if (lv instanceof NbtCompound) {
            NbtCompound lv2 = (NbtCompound)lv;
            return DataResult.success(lv2 == dynamic.getValue() ? lv2.copy() : lv2);
        }
        return DataResult.error(() -> "Not a compound tag: " + String.valueOf(lv));
    }, nbt -> new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbt.copy()));
    private static final int SIZE = 48;
    private static final int field_41719 = 32;
    public static final NbtType<NbtCompound> TYPE = new NbtType.OfVariableSize<NbtCompound>(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public NbtCompound read(DataInput dataInput, NbtSizeTracker arg) throws IOException {
            arg.pushStack();
            try {
                NbtCompound nbtCompound = 1.readCompound(dataInput, arg);
                return nbtCompound;
            } finally {
                arg.popStack();
            }
        }

        private static NbtCompound readCompound(DataInput input, NbtSizeTracker tracker) throws IOException {
            byte b;
            tracker.add(48L);
            HashMap<String, NbtElement> map = Maps.newHashMap();
            while ((b = input.readByte()) != 0) {
                NbtElement lv;
                String string = 1.readString(input, tracker);
                if (map.put(string, lv = NbtCompound.read(NbtTypes.byId(b), string, input, tracker)) != null) continue;
                tracker.add(36L);
            }
            return new NbtCompound(map);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            tracker.pushStack();
            try {
                NbtScanner.Result result = 1.scanCompound(input, visitor, tracker);
                return result;
            } finally {
                tracker.popStack();
            }
        }

        private static NbtScanner.Result scanCompound(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            byte b;
            tracker.add(48L);
            block13: while ((b = input.readByte()) != 0) {
                NbtType<?> lv = NbtTypes.byId(b);
                switch (visitor.visitSubNbtType(lv)) {
                    case HALT: {
                        return NbtScanner.Result.HALT;
                    }
                    case BREAK: {
                        NbtString.skip(input);
                        lv.skip(input, tracker);
                        break block13;
                    }
                    case SKIP: {
                        NbtString.skip(input);
                        lv.skip(input, tracker);
                        continue block13;
                    }
                    default: {
                        String string = 1.readString(input, tracker);
                        switch (visitor.startSubNbt(lv, string)) {
                            case HALT: {
                                return NbtScanner.Result.HALT;
                            }
                            case BREAK: {
                                lv.skip(input, tracker);
                                break block13;
                            }
                            case SKIP: {
                                lv.skip(input, tracker);
                                continue block13;
                            }
                        }
                        tracker.add(36L);
                        switch (lv.doAccept(input, visitor, tracker)) {
                            case HALT: {
                                return NbtScanner.Result.HALT;
                            }
                        }
                        continue block13;
                    }
                }
            }
            if (b != 0) {
                while ((b = input.readByte()) != 0) {
                    NbtString.skip(input);
                    NbtTypes.byId(b).skip(input, tracker);
                }
            }
            return visitor.endNested();
        }

        private static String readString(DataInput input, NbtSizeTracker tracker) throws IOException {
            String string = input.readUTF();
            tracker.add(28L);
            tracker.add(2L, string.length());
            return string;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.pushStack();
            try {
                byte b;
                while ((b = input.readByte()) != 0) {
                    NbtString.skip(input);
                    NbtTypes.byId(b).skip(input, tracker);
                }
            } finally {
                tracker.popStack();
            }
        }

        @Override
        public String getCrashReportName() {
            return "COMPOUND";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Compound";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
            return this.read(input, tracker);
        }
    };
    private final Map<String, NbtElement> entries;

    NbtCompound(Map<String, NbtElement> entries) {
        this.entries = entries;
    }

    public NbtCompound() {
        this(new HashMap<String, NbtElement>());
    }

    @Override
    public void write(DataOutput output) throws IOException {
        for (String string : this.entries.keySet()) {
            NbtElement lv = this.entries.get(string);
            NbtCompound.write(string, lv, output);
        }
        output.writeByte(0);
    }

    @Override
    public int getSizeInBytes() {
        int i = 48;
        for (Map.Entry<String, NbtElement> entry : this.entries.entrySet()) {
            i += 28 + 2 * entry.getKey().length();
            i += 36;
            i += entry.getValue().getSizeInBytes();
        }
        return i;
    }

    public Set<String> getKeys() {
        return this.entries.keySet();
    }

    public Set<Map.Entry<String, NbtElement>> entrySet() {
        return this.entries.entrySet();
    }

    public Collection<NbtElement> values() {
        return this.entries.values();
    }

    public void forEach(BiConsumer<String, NbtElement> entryConsumer) {
        this.entries.forEach(entryConsumer);
    }

    @Override
    public byte getType() {
        return NbtElement.COMPOUND_TYPE;
    }

    public NbtType<NbtCompound> getNbtType() {
        return TYPE;
    }

    public int getSize() {
        return this.entries.size();
    }

    @Nullable
    public NbtElement put(String key, NbtElement element) {
        return this.entries.put(key, element);
    }

    public void putByte(String key, byte value) {
        this.entries.put(key, NbtByte.of(value));
    }

    public void putShort(String key, short value) {
        this.entries.put(key, NbtShort.of(value));
    }

    public void putInt(String key, int value) {
        this.entries.put(key, NbtInt.of(value));
    }

    public void putLong(String key, long value) {
        this.entries.put(key, NbtLong.of(value));
    }

    public void putFloat(String key, float value) {
        this.entries.put(key, NbtFloat.of(value));
    }

    public void putDouble(String key, double value) {
        this.entries.put(key, NbtDouble.of(value));
    }

    public void putString(String key, String value) {
        this.entries.put(key, NbtString.of(value));
    }

    public void putByteArray(String key, byte[] value) {
        this.entries.put(key, new NbtByteArray(value));
    }

    public void putIntArray(String key, int[] value) {
        this.entries.put(key, new NbtIntArray(value));
    }

    public void putLongArray(String key, long[] value) {
        this.entries.put(key, new NbtLongArray(value));
    }

    public void putBoolean(String key, boolean value) {
        this.entries.put(key, NbtByte.of(value));
    }

    @Nullable
    public NbtElement get(String key) {
        return this.entries.get(key);
    }

    public boolean contains(String key) {
        return this.entries.containsKey(key);
    }

    private Optional<NbtElement> getOptional(String key) {
        return Optional.ofNullable(this.entries.get(key));
    }

    public Optional<Byte> getByte(String key) {
        return this.getOptional(key).flatMap(NbtElement::asByte);
    }

    public byte getByte(String key, byte fallback) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber lv = (AbstractNbtNumber)nbtElement;
            return lv.byteValue();
        }
        return fallback;
    }

    public Optional<Short> getShort(String key) {
        return this.getOptional(key).flatMap(NbtElement::asShort);
    }

    public short getShort(String key, short fallback) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber lv = (AbstractNbtNumber)nbtElement;
            return lv.shortValue();
        }
        return fallback;
    }

    public Optional<Integer> getInt(String key) {
        return this.getOptional(key).flatMap(NbtElement::asInt);
    }

    public int getInt(String key, int fallback) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber lv = (AbstractNbtNumber)nbtElement;
            return lv.intValue();
        }
        return fallback;
    }

    public Optional<Long> getLong(String key) {
        return this.getOptional(key).flatMap(NbtElement::asLong);
    }

    public long getLong(String key, long fallback) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber lv = (AbstractNbtNumber)nbtElement;
            return lv.longValue();
        }
        return fallback;
    }

    public Optional<Float> getFloat(String key) {
        return this.getOptional(key).flatMap(NbtElement::asFloat);
    }

    public float getFloat(String key, float fallback) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber lv = (AbstractNbtNumber)nbtElement;
            return lv.floatValue();
        }
        return fallback;
    }

    public Optional<Double> getDouble(String key) {
        return this.getOptional(key).flatMap(NbtElement::asDouble);
    }

    public double getDouble(String key, double fallback) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof AbstractNbtNumber) {
            AbstractNbtNumber lv = (AbstractNbtNumber)nbtElement;
            return lv.doubleValue();
        }
        return fallback;
    }

    public Optional<String> getString(String key) {
        return this.getOptional(key).flatMap(NbtElement::asString);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getString(String key, String fallback) {
        NbtElement nbtElement = this.entries.get(key);
        if (!(nbtElement instanceof NbtString)) return fallback;
        NbtString nbtString = (NbtString)nbtElement;
        try {
            String string = nbtString.value();
            return string;
        } catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    public Optional<byte[]> getByteArray(String key) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof NbtByteArray) {
            NbtByteArray lv = (NbtByteArray)nbtElement;
            return Optional.of(lv.getByteArray());
        }
        return Optional.empty();
    }

    public Optional<int[]> getIntArray(String key) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof NbtIntArray) {
            NbtIntArray lv = (NbtIntArray)nbtElement;
            return Optional.of(lv.getIntArray());
        }
        return Optional.empty();
    }

    public Optional<long[]> getLongArray(String key) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof NbtLongArray) {
            NbtLongArray lv = (NbtLongArray)nbtElement;
            return Optional.of(lv.getLongArray());
        }
        return Optional.empty();
    }

    public Optional<NbtCompound> getCompound(String key) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)nbtElement;
            return Optional.of(lv);
        }
        return Optional.empty();
    }

    public NbtCompound getCompoundOrEmpty(String key) {
        return this.getCompound(key).orElseGet(NbtCompound::new);
    }

    public Optional<NbtList> getList(String key) {
        NbtElement nbtElement = this.entries.get(key);
        if (nbtElement instanceof NbtList) {
            NbtList lv = (NbtList)nbtElement;
            return Optional.of(lv);
        }
        return Optional.empty();
    }

    public NbtList getListOrEmpty(String key) {
        return this.getList(key).orElseGet(NbtList::new);
    }

    public Optional<Boolean> getBoolean(String key) {
        return this.getOptional(key).flatMap(NbtElement::asBoolean);
    }

    public boolean getBoolean(String key, boolean fallback) {
        return this.getByte(key, fallback ? (byte)1 : 0) != 0;
    }

    public void remove(String key) {
        this.entries.remove(key);
    }

    @Override
    public String toString() {
        StringNbtWriter lv = new StringNbtWriter();
        lv.visitCompound(this);
        return lv.getString();
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    protected NbtCompound shallowCopy() {
        return new NbtCompound(new HashMap<String, NbtElement>(this.entries));
    }

    @Override
    public NbtCompound copy() {
        HashMap<String, NbtElement> hashMap = new HashMap<String, NbtElement>();
        this.entries.forEach((? super K key, ? super V value) -> hashMap.put((String)key, value.copy()));
        return new NbtCompound(hashMap);
    }

    @Override
    public Optional<NbtCompound> asCompound() {
        return Optional.of(this);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtCompound && Objects.equals(this.entries, ((NbtCompound)o).entries);
    }

    public int hashCode() {
        return this.entries.hashCode();
    }

    private static void write(String key, NbtElement element, DataOutput output) throws IOException {
        output.writeByte(element.getType());
        if (element.getType() == 0) {
            return;
        }
        output.writeUTF(key);
        element.write(output);
    }

    static NbtElement read(NbtType<?> reader, String key, DataInput input, NbtSizeTracker tracker) {
        try {
            return reader.read(input, tracker);
        } catch (IOException iOException) {
            CrashReport lv = CrashReport.create(iOException, "Loading NBT data");
            CrashReportSection lv2 = lv.addElement("NBT Tag");
            lv2.add("Tag name", key);
            lv2.add("Tag type", reader.getCrashReportName());
            throw new NbtCrashException(lv);
        }
    }

    public NbtCompound copyFrom(NbtCompound source) {
        for (String string : source.entries.keySet()) {
            NbtElement lv = source.entries.get(string);
            if (lv instanceof NbtCompound) {
                NbtCompound lv2 = (NbtCompound)lv;
                NbtElement nbtElement = this.entries.get(string);
                if (nbtElement instanceof NbtCompound) {
                    NbtCompound lv3 = (NbtCompound)nbtElement;
                    lv3.copyFrom(lv2);
                    continue;
                }
            }
            this.put(string, lv.copy());
        }
        return this;
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitCompound(this);
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        block14: for (Map.Entry<String, NbtElement> entry : this.entries.entrySet()) {
            NbtElement lv = entry.getValue();
            NbtType<?> lv2 = lv.getNbtType();
            NbtScanner.NestedResult lv3 = visitor.visitSubNbtType(lv2);
            switch (lv3) {
                case HALT: {
                    return NbtScanner.Result.HALT;
                }
                case BREAK: {
                    return visitor.endNested();
                }
                case SKIP: {
                    continue block14;
                }
            }
            lv3 = visitor.startSubNbt(lv2, entry.getKey());
            switch (lv3) {
                case HALT: {
                    return NbtScanner.Result.HALT;
                }
                case BREAK: {
                    return visitor.endNested();
                }
                case SKIP: {
                    continue block14;
                }
            }
            NbtScanner.Result lv4 = lv.doAccept(visitor);
            switch (lv4) {
                case HALT: {
                    return NbtScanner.Result.HALT;
                }
                case BREAK: {
                    return visitor.endNested();
                }
            }
        }
        return visitor.endNested();
    }

    public <T> void put(String key, Codec<T> codec, T value) {
        this.put(key, codec, NbtOps.INSTANCE, value);
    }

    public <T> void putNullable(String key, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            this.put(key, codec, value);
        }
    }

    public <T> void put(String key, Codec<T> codec, DynamicOps<NbtElement> ops, T value) {
        this.put(key, codec.encodeStart(ops, (NbtElement)value).getOrThrow());
    }

    public <T> void putNullable(String key, Codec<T> codec, DynamicOps<NbtElement> ops, @Nullable T value) {
        if (value != null) {
            this.put(key, codec, ops, value);
        }
    }

    public <T> void copyFromCodec(MapCodec<T> codec, T value) {
        this.copyFromCodec(codec, NbtOps.INSTANCE, value);
    }

    public <T> void copyFromCodec(MapCodec<T> codec, DynamicOps<NbtElement> ops, T value) {
        this.copyFrom((NbtCompound)codec.encoder().encodeStart(ops, value).getOrThrow());
    }

    public <T> Optional<T> get(String key, Codec<T> codec) {
        return this.get(key, codec, NbtOps.INSTANCE);
    }

    public <T> Optional<T> get(String key, Codec<T> codec, DynamicOps<NbtElement> ops) {
        NbtElement lv = this.get(key);
        if (lv == null) {
            return Optional.empty();
        }
        return codec.parse(ops, lv).resultOrPartial(error -> LOGGER.error("Failed to read field ({}={}): {}", key, lv, error));
    }

    public <T> Optional<T> decode(MapCodec<T> codec) {
        return this.decode(codec, NbtOps.INSTANCE);
    }

    public <T> Optional<T> decode(MapCodec<T> codec, DynamicOps<NbtElement> ops) {
        return codec.decode(ops, ops.getMap(this).getOrThrow()).resultOrPartial(error -> LOGGER.error("Failed to read value ({}): {}", (Object)this, error));
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }
}

