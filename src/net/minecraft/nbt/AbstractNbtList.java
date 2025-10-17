/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;

public sealed interface AbstractNbtList
extends Iterable<NbtElement>,
NbtElement
permits NbtList, NbtByteArray, NbtIntArray, NbtLongArray {
    public void clear();

    public boolean setElement(int var1, NbtElement var2);

    public boolean addElement(int var1, NbtElement var2);

    public NbtElement remove(int var1);

    public NbtElement get(int var1);

    public int size();

    default public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    default public Iterator<NbtElement> iterator() {
        return new Iterator<NbtElement>(){
            private int current;

            @Override
            public boolean hasNext() {
                return this.current < AbstractNbtList.this.size();
            }

            @Override
            public NbtElement next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return AbstractNbtList.this.get(this.current++);
            }

            @Override
            public /* synthetic */ Object next() {
                return this.next();
            }
        };
    }

    default public Stream<NbtElement> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}

