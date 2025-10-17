/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public interface UniformValue {
    public static final Codec<UniformValue> CODEC = Type.CODEC.dispatch(UniformValue::getType, val -> val.mapCodec);

    public void write(Std140Builder var1);

    public void addSize(Std140SizeCalculator var1);

    public Type getType();

    @Environment(value=EnvType.CLIENT)
    public static enum Type implements StringIdentifiable
    {
        INT("int", IntValue.CODEC),
        IVEC3("ivec3", Vec3iValue.CODEC),
        FLOAT("float", FloatValue.CODEC),
        VEC2("vec2", Vec2fValue.CODEC),
        VEC3("vec3", Vec3fValue.CODEC),
        VEC4("vec4", Vec4fValue.CODEC),
        MATRIX4X4("matrix4x4", Matrix4fValue.CODEC);

        public static final StringIdentifiable.EnumCodec<Type> CODEC;
        private final String name;
        final MapCodec<? extends UniformValue> mapCodec;

        private Type(String name, Codec<? extends UniformValue> codec) {
            this.name = name;
            this.mapCodec = codec.fieldOf("value");
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Type::values);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Matrix4fValue(Matrix4fc value) implements UniformValue
    {
        public static final Codec<Matrix4fValue> CODEC = Codecs.MATRIX_4F.xmap(Matrix4fValue::new, Matrix4fValue::value);

        @Override
        public void write(Std140Builder builder) {
            builder.putMat4f(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator calculator) {
            calculator.putMat4f();
        }

        @Override
        public Type getType() {
            return Type.MATRIX4X4;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Vec4fValue(Vector4f value) implements UniformValue
    {
        public static final Codec<Vec4fValue> CODEC = Codecs.VECTOR_4F.xmap(Vec4fValue::new, Vec4fValue::value);

        @Override
        public void write(Std140Builder builder) {
            builder.putVec4(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator calculator) {
            calculator.putVec4();
        }

        @Override
        public Type getType() {
            return Type.VEC4;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Vec3fValue(Vector3f value) implements UniformValue
    {
        public static final Codec<Vec3fValue> CODEC = Codecs.VECTOR_3F.xmap(Vec3fValue::new, Vec3fValue::value);

        @Override
        public void write(Std140Builder builder) {
            builder.putVec3(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator calculator) {
            calculator.putVec3();
        }

        @Override
        public Type getType() {
            return Type.VEC3;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Vec2fValue(Vector2f value) implements UniformValue
    {
        public static final Codec<Vec2fValue> CODEC = Codecs.VECTOR_2F.xmap(Vec2fValue::new, Vec2fValue::value);

        @Override
        public void write(Std140Builder builder) {
            builder.putVec2(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator calculator) {
            calculator.putVec2();
        }

        @Override
        public Type getType() {
            return Type.VEC2;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record FloatValue(float value) implements UniformValue
    {
        public static final Codec<FloatValue> CODEC = Codec.FLOAT.xmap(FloatValue::new, FloatValue::value);

        @Override
        public void write(Std140Builder builder) {
            builder.putFloat(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator calculator) {
            calculator.putFloat();
        }

        @Override
        public Type getType() {
            return Type.FLOAT;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Vec3iValue(Vector3i value) implements UniformValue
    {
        public static final Codec<Vec3iValue> CODEC = Codecs.VECTOR_3I.xmap(Vec3iValue::new, Vec3iValue::value);

        @Override
        public void write(Std140Builder builder) {
            builder.putIVec3(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator calculator) {
            calculator.putIVec3();
        }

        @Override
        public Type getType() {
            return Type.IVEC3;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record IntValue(int value) implements UniformValue
    {
        public static final Codec<IntValue> CODEC = Codec.INT.xmap(IntValue::new, IntValue::value);

        @Override
        public void write(Std140Builder builder) {
            builder.putInt(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator calculator) {
            calculator.putInt();
        }

        @Override
        public Type getType() {
            return Type.INT;
        }
    }
}

