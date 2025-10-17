/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public class SingularPalette<T>
implements Palette<T> {
    @Nullable
    private T entry;

    public SingularPalette(List<T> idList) {
        if (idList.size() > 0) {
            Validate.isTrue(idList.size() <= 1, "Can't initialize SingleValuePalette with %d values.", idList.size());
            this.entry = idList.get(0);
        }
    }

    public static <A> Palette<A> create(int bitSize, List<A> idList) {
        return new SingularPalette<A>(idList);
    }

    @Override
    public int index(T object, PaletteResizeListener<T> listener) {
        if (this.entry == null || this.entry == object) {
            this.entry = object;
            return 0;
        }
        return listener.onResize(1, object);
    }

    @Override
    public boolean hasAny(Predicate<T> predicate) {
        if (this.entry == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return predicate.test(this.entry);
    }

    @Override
    public T get(int id) {
        if (this.entry == null || id != 0) {
            throw new IllegalStateException("Missing Palette entry for id " + id + ".");
        }
        return this.entry;
    }

    @Override
    public void readPacket(PacketByteBuf buf, IndexedIterable<T> idList) {
        this.entry = idList.getOrThrow(buf.readVarInt());
    }

    @Override
    public void writePacket(PacketByteBuf buf, IndexedIterable<T> idList) {
        if (this.entry == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        buf.writeVarInt(idList.getRawId(this.entry));
    }

    @Override
    public int getPacketSize(IndexedIterable<T> idList) {
        if (this.entry == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return VarInts.getSizeInBytes(idList.getRawId(this.entry));
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public Palette<T> copy() {
        if (this.entry == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return this;
    }
}

