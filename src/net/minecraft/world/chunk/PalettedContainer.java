/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.EmptyPaletteStorage;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.thread.LockHelper;
import net.minecraft.world.chunk.BiMapPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteProvider;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.PaletteType;
import net.minecraft.world.chunk.ReadableContainer;
import org.jetbrains.annotations.Nullable;

public class PalettedContainer<T>
implements PaletteResizeListener<T>,
ReadableContainer<T> {
    private static final int field_34557 = 0;
    private volatile Data<T> data;
    private final PaletteProvider<T> paletteProvider;
    private final LockHelper lockHelper = new LockHelper("PalettedContainer");

    public void lock() {
        this.lockHelper.lock();
    }

    public void unlock() {
        this.lockHelper.unlock();
    }

    public static <T> Codec<PalettedContainer<T>> createPalettedContainerCodec(Codec<T> entryCodec, PaletteProvider<T> provider, T defaultValue) {
        ReadableContainer.Reader lv = PalettedContainer::read;
        return PalettedContainer.createCodec(entryCodec, provider, defaultValue, lv);
    }

    public static <T> Codec<ReadableContainer<T>> createReadableContainerCodec(Codec<T> entryCodec, PaletteProvider<T> provider, T defaultValue) {
        ReadableContainer.Reader lv = (paletteProvider, serialized) -> PalettedContainer.read(paletteProvider, serialized).map(result -> result);
        return PalettedContainer.createCodec(entryCodec, provider, defaultValue, lv);
    }

    private static <T, C extends ReadableContainer<T>> Codec<C> createCodec(Codec<T> entryCodec, PaletteProvider<T> provider, T defaultValue, ReadableContainer.Reader<T, C> reader) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)entryCodec.mapResult(Codecs.orElsePartial(defaultValue)).listOf().fieldOf("palette")).forGetter(ReadableContainer.Serialized::paletteEntries), Codec.LONG_STREAM.lenientOptionalFieldOf("data").forGetter(ReadableContainer.Serialized::storage)).apply((Applicative<ReadableContainer.Serialized, ?>)instance, ReadableContainer.Serialized::new)).comapFlatMap(serialized -> reader.read(provider, (ReadableContainer.Serialized)serialized), container -> container.serialize(provider));
    }

    private PalettedContainer(PaletteProvider<T> paletteProvider, PaletteType type, PaletteStorage storage, Palette<T> palette) {
        this.paletteProvider = paletteProvider;
        this.data = new Data<T>(type, storage, palette);
    }

    private PalettedContainer(PalettedContainer<T> container) {
        this.paletteProvider = container.paletteProvider;
        this.data = container.data.copy();
    }

    public PalettedContainer(T defaultValue, PaletteProvider<T> paletteProvider) {
        this.paletteProvider = paletteProvider;
        this.data = this.getCompatibleData(null, 0);
        this.data.palette.index(defaultValue, this);
    }

    private Data<T> getCompatibleData(@Nullable Data<T> previousData, int bits) {
        PaletteType lv = this.paletteProvider.createType(bits);
        if (previousData != null && lv.equals(previousData.configuration())) {
            return previousData;
        }
        PaletteStorage lv2 = lv.bitsInMemory() == 0 ? new EmptyPaletteStorage(this.paletteProvider.getSize()) : new PackedIntegerArray(lv.bitsInMemory(), this.paletteProvider.getSize());
        Palette<T> lv3 = lv.createPalette(this.paletteProvider, List.of());
        return new Data<T>(lv, lv2, lv3);
    }

    @Override
    public int onResize(int i, T object) {
        Data<T> lv = this.data;
        Data lv2 = this.getCompatibleData(lv, i);
        lv2.importFrom(lv.palette, lv.storage);
        this.data = lv2;
        return lv2.palette.index(object, PaletteResizeListener.throwing());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public T swap(int x, int y, int z, T value) {
        this.lock();
        try {
            T t = this.swap(this.paletteProvider.computeIndex(x, y, z), value);
            return t;
        } finally {
            this.unlock();
        }
    }

    public T swapUnsafe(int x, int y, int z, T value) {
        return this.swap(this.paletteProvider.computeIndex(x, y, z), value);
    }

    private T swap(int index, T value) {
        int j = this.data.palette.index(value, this);
        int k = this.data.storage.swap(index, j);
        return this.data.palette.get(k);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void set(int x, int y, int z, T value) {
        this.lock();
        try {
            this.set(this.paletteProvider.computeIndex(x, y, z), value);
        } finally {
            this.unlock();
        }
    }

    private void set(int index, T value) {
        int j = this.data.palette.index(value, this);
        this.data.storage.set(index, j);
    }

    @Override
    public T get(int x, int y, int z) {
        return this.get(this.paletteProvider.computeIndex(x, y, z));
    }

    protected T get(int index) {
        Data<T> lv = this.data;
        return lv.palette.get(lv.storage.get(index));
    }

    @Override
    public void forEachValue(Consumer<T> action) {
        Palette lv = this.data.palette();
        IntArraySet intSet = new IntArraySet();
        this.data.storage.forEach(intSet::add);
        intSet.forEach(id -> action.accept(lv.get(id)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void readPacket(PacketByteBuf buf) {
        this.lock();
        try {
            byte i = buf.readByte();
            Data<T> lv = this.getCompatibleData(this.data, i);
            lv.palette.readPacket(buf, this.paletteProvider.getIdList());
            buf.readFixedLengthLongArray(lv.storage.getData());
            this.data = lv;
        } finally {
            this.unlock();
        }
    }

    @Override
    public void writePacket(PacketByteBuf buf) {
        this.lock();
        try {
            this.data.writePacket(buf, this.paletteProvider.getIdList());
        } finally {
            this.unlock();
        }
    }

    @VisibleForTesting
    public static <T> DataResult<PalettedContainer<T>> read(PaletteProvider<T> provider, ReadableContainer.Serialized<T> serialized) {
        PaletteStorage lv3;
        Palette<T> lv2;
        List<T> list = serialized.paletteEntries();
        int i = provider.getSize();
        PaletteType lv = provider.createTypeFromSize(list.size());
        int j = lv.bitsInStorage();
        if (serialized.bitsPerEntry() != -1 && j != serialized.bitsPerEntry()) {
            return DataResult.error(() -> "Invalid bit count, calculated " + j + ", but container declared " + serialized.bitsPerEntry());
        }
        if (lv.bitsInMemory() == 0) {
            lv2 = lv.createPalette(provider, list);
            lv3 = new EmptyPaletteStorage(i);
        } else {
            Optional<LongStream> optional = serialized.storage();
            if (optional.isEmpty()) {
                return DataResult.error(() -> "Missing values for non-zero storage");
            }
            long[] ls = optional.get().toArray();
            try {
                if (lv.shouldRepack() || lv.bitsInMemory() != j) {
                    BiMapPalette<T> lv4 = new BiMapPalette<T>(j, list);
                    PackedIntegerArray lv5 = new PackedIntegerArray(j, i, ls);
                    Palette<T> lv6 = lv.createPalette(provider, list);
                    int[] is = PalettedContainer.repack(lv5, lv4, lv6);
                    lv2 = lv6;
                    lv3 = new PackedIntegerArray(lv.bitsInMemory(), i, is);
                } else {
                    lv2 = lv.createPalette(provider, list);
                    lv3 = new PackedIntegerArray(lv.bitsInMemory(), i, ls);
                }
            } catch (PackedIntegerArray.InvalidLengthException lv7) {
                return DataResult.error(() -> "Failed to read PalettedContainer: " + lv7.getMessage());
            }
        }
        return DataResult.success(new PalettedContainer<T>(provider, lv, lv3, lv2));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ReadableContainer.Serialized<T> serialize(PaletteProvider<T> provider) {
        this.lock();
        try {
            Optional<LongStream> optional;
            PaletteStorage lv = this.data.storage;
            Palette lv2 = this.data.palette;
            BiMapPalette lv3 = new BiMapPalette(lv.getElementBits());
            int i = provider.getSize();
            int[] is = PalettedContainer.repack(lv, lv2, lv3);
            PaletteType lv4 = provider.createTypeFromSize(lv3.getSize());
            int j = lv4.bitsInStorage();
            if (j != 0) {
                PackedIntegerArray lv5 = new PackedIntegerArray(j, i, is);
                optional = Optional.of(Arrays.stream(lv5.getData()));
            } else {
                optional = Optional.empty();
            }
            ReadableContainer.Serialized serialized = new ReadableContainer.Serialized(lv3.getElements(), optional, j);
            return serialized;
        } finally {
            this.unlock();
        }
    }

    private static <T> int[] repack(PaletteStorage storage, Palette<T> oldPalette, Palette<T> newPalette) {
        int[] is = new int[storage.getSize()];
        storage.writePaletteIndices(is);
        PaletteResizeListener lv = PaletteResizeListener.throwing();
        int i = -1;
        int j = -1;
        for (int k = 0; k < is.length; ++k) {
            int l = is[k];
            if (l != i) {
                i = l;
                j = newPalette.index(oldPalette.get(l), lv);
            }
            is[k] = j;
        }
        return is;
    }

    @Override
    public int getPacketSize() {
        return this.data.getPacketSize(this.paletteProvider.getIdList());
    }

    @Override
    public int getElementBits() {
        return this.data.storage().getElementBits();
    }

    @Override
    public boolean hasAny(Predicate<T> predicate) {
        return this.data.palette.hasAny(predicate);
    }

    @Override
    public PalettedContainer<T> copy() {
        return new PalettedContainer<T>(this);
    }

    @Override
    public PalettedContainer<T> slice() {
        return new PalettedContainer(this.data.palette.get(0), this.paletteProvider);
    }

    @Override
    public void count(Counter<T> counter) {
        if (this.data.palette.getSize() == 1) {
            counter.accept(this.data.palette.get(0), this.data.storage.getSize());
            return;
        }
        Int2IntOpenHashMap int2IntOpenHashMap = new Int2IntOpenHashMap();
        this.data.storage.forEach(key -> int2IntOpenHashMap.addTo(key, 1));
        int2IntOpenHashMap.int2IntEntrySet().forEach(entry -> counter.accept(this.data.palette.get(entry.getIntKey()), entry.getIntValue()));
    }

    record Data<T>(PaletteType configuration, PaletteStorage storage, Palette<T> palette) {
        public void importFrom(Palette<T> palette, PaletteStorage storage) {
            PaletteResizeListener lv = PaletteResizeListener.throwing();
            for (int i = 0; i < storage.getSize(); ++i) {
                T object = palette.get(storage.get(i));
                this.storage.set(i, this.palette.index(object, lv));
            }
        }

        public int getPacketSize(IndexedIterable<T> idList) {
            return 1 + this.palette.getPacketSize(idList) + this.storage.getData().length * 8;
        }

        public void writePacket(PacketByteBuf buf, IndexedIterable<T> idList) {
            buf.writeByte(this.storage.getElementBits());
            this.palette.writePacket(buf, idList);
            buf.writeFixedLengthLongArray(this.storage.getData());
        }

        public Data<T> copy() {
            return new Data<T>(this.configuration, this.storage.copy(), this.palette.copy());
        }
    }

    @FunctionalInterface
    public static interface Counter<T> {
        public void accept(T var1, int var2);
    }
}

