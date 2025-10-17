/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.buffers;

import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.annotation.DeobfuscateClass;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.lwjgl.system.MemoryStack;

@Environment(value=EnvType.CLIENT)
@DeobfuscateClass
public class Std140Builder {
    private final ByteBuffer buffer;
    private final int start;

    private Std140Builder(ByteBuffer buffer) {
        this.buffer = buffer;
        this.start = buffer.position();
    }

    public static Std140Builder intoBuffer(ByteBuffer buffer) {
        return new Std140Builder(buffer);
    }

    public static Std140Builder onStack(MemoryStack stack, int size) {
        return new Std140Builder(stack.malloc(size));
    }

    public ByteBuffer get() {
        return this.buffer.flip();
    }

    public Std140Builder align(int alignedSize) {
        int j = this.buffer.position();
        this.buffer.position(this.start + MathHelper.roundUpToMultiple(j - this.start, alignedSize));
        return this;
    }

    public Std140Builder putFloat(float value) {
        this.align(4);
        this.buffer.putFloat(value);
        return this;
    }

    public Std140Builder putInt(int value) {
        this.align(4);
        this.buffer.putInt(value);
        return this;
    }

    public Std140Builder putVec2(float x, float y) {
        this.align(8);
        this.buffer.putFloat(x);
        this.buffer.putFloat(y);
        return this;
    }

    public Std140Builder putVec2(Vector2fc vec) {
        this.align(8);
        vec.get(this.buffer);
        this.buffer.position(this.buffer.position() + 8);
        return this;
    }

    public Std140Builder putIVec2(int x, int y) {
        this.align(8);
        this.buffer.putInt(x);
        this.buffer.putInt(y);
        return this;
    }

    public Std140Builder putIVec2(Vector2ic vec) {
        this.align(8);
        vec.get(this.buffer);
        this.buffer.position(this.buffer.position() + 8);
        return this;
    }

    public Std140Builder putVec3(float x, float y, float z) {
        this.align(16);
        this.buffer.putFloat(x);
        this.buffer.putFloat(y);
        this.buffer.putFloat(z);
        this.buffer.position(this.buffer.position() + 4);
        return this;
    }

    public Std140Builder putVec3(Vector3fc vec) {
        this.align(16);
        vec.get(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return this;
    }

    public Std140Builder putIVec3(int x, int y, int z) {
        this.align(16);
        this.buffer.putInt(x);
        this.buffer.putInt(y);
        this.buffer.putInt(z);
        this.buffer.position(this.buffer.position() + 4);
        return this;
    }

    public Std140Builder putIVec3(Vector3ic vec) {
        this.align(16);
        vec.get(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return this;
    }

    public Std140Builder putVec4(float x, float y, float z, float w) {
        this.align(16);
        this.buffer.putFloat(x);
        this.buffer.putFloat(y);
        this.buffer.putFloat(z);
        this.buffer.putFloat(w);
        return this;
    }

    public Std140Builder putVec4(Vector4fc vec) {
        this.align(16);
        vec.get(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return this;
    }

    public Std140Builder putIVec4(int x, int y, int z, int w) {
        this.align(16);
        this.buffer.putInt(x);
        this.buffer.putInt(y);
        this.buffer.putInt(z);
        this.buffer.putInt(w);
        return this;
    }

    public Std140Builder putIVec4(Vector4ic vec) {
        this.align(16);
        vec.get(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return this;
    }

    public Std140Builder putMat4f(Matrix4fc matrix) {
        this.align(16);
        matrix.get(this.buffer);
        this.buffer.position(this.buffer.position() + 64);
        return this;
    }
}

