/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface PacketCodecs {
    public static final int field_49674 = 65536;
    public static final PacketCodec<ByteBuf, Boolean> BOOLEAN = new PacketCodec<ByteBuf, Boolean>(){

        @Override
        public Boolean decode(ByteBuf byteBuf) {
            return byteBuf.readBoolean();
        }

        @Override
        public void encode(ByteBuf byteBuf, Boolean boolean_) {
            byteBuf.writeBoolean(boolean_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Boolean)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Byte> BYTE = new PacketCodec<ByteBuf, Byte>(){

        @Override
        public Byte decode(ByteBuf byteBuf) {
            return byteBuf.readByte();
        }

        @Override
        public void encode(ByteBuf byteBuf, Byte byte_) {
            byteBuf.writeByte(byte_.byteValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Byte)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Float> DEGREES = BYTE.xmap(MathHelper::unpackDegrees, MathHelper::packDegrees);
    public static final PacketCodec<ByteBuf, Short> SHORT = new PacketCodec<ByteBuf, Short>(){

        @Override
        public Short decode(ByteBuf byteBuf) {
            return byteBuf.readShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Short short_) {
            byteBuf.writeShort(short_.shortValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Short)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Integer> UNSIGNED_SHORT = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readUnsignedShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeShort(integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Integer> INTEGER = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readInt();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeInt(integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Integer> VAR_INT = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return VarInts.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            VarInts.write(byteBuf, integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, OptionalInt> OPTIONAL_INT = VAR_INT.xmap(value -> value == 0 ? OptionalInt.empty() : OptionalInt.of(value - 1), value -> value.isPresent() ? value.getAsInt() + 1 : 0);
    public static final PacketCodec<ByteBuf, Long> LONG = new PacketCodec<ByteBuf, Long>(){

        @Override
        public Long decode(ByteBuf byteBuf) {
            return byteBuf.readLong();
        }

        @Override
        public void encode(ByteBuf byteBuf, Long long_) {
            byteBuf.writeLong(long_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Long)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Long> VAR_LONG = new PacketCodec<ByteBuf, Long>(){

        @Override
        public Long decode(ByteBuf byteBuf) {
            return VarLongs.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Long long_) {
            VarLongs.write(byteBuf, long_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Long)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Float> FLOAT = new PacketCodec<ByteBuf, Float>(){

        @Override
        public Float decode(ByteBuf byteBuf) {
            return Float.valueOf(byteBuf.readFloat());
        }

        @Override
        public void encode(ByteBuf byteBuf, Float float_) {
            byteBuf.writeFloat(float_.floatValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Float)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Double> DOUBLE = new PacketCodec<ByteBuf, Double>(){

        @Override
        public Double decode(ByteBuf byteBuf) {
            return byteBuf.readDouble();
        }

        @Override
        public void encode(ByteBuf byteBuf, Double double_) {
            byteBuf.writeDouble(double_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Double)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, byte[]> BYTE_ARRAY = new PacketCodec<ByteBuf, byte[]>(){

        @Override
        public byte[] decode(ByteBuf buf) {
            return PacketByteBuf.readByteArray(buf);
        }

        @Override
        public void encode(ByteBuf buf, byte[] value) {
            PacketByteBuf.writeByteArray(buf, value);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (byte[])object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, long[]> LONG_ARRAY = new PacketCodec<ByteBuf, long[]>(){

        @Override
        public long[] decode(ByteBuf buf) {
            return PacketByteBuf.readLongArray(buf);
        }

        @Override
        public void encode(ByteBuf buf, long[] values) {
            PacketByteBuf.writeLongArray(buf, values);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (long[])object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, String> STRING = PacketCodecs.string(Short.MAX_VALUE);
    public static final PacketCodec<ByteBuf, NbtElement> NBT_ELEMENT = PacketCodecs.nbt(() -> NbtSizeTracker.of(PacketByteBuf.MAX_READ_NBT_SIZE));
    public static final PacketCodec<ByteBuf, NbtElement> UNLIMITED_NBT_ELEMENT = PacketCodecs.nbt(NbtSizeTracker::ofUnlimitedBytes);
    public static final PacketCodec<ByteBuf, NbtCompound> NBT_COMPOUND = PacketCodecs.nbtCompound(() -> NbtSizeTracker.of(PacketByteBuf.MAX_READ_NBT_SIZE));
    public static final PacketCodec<ByteBuf, NbtCompound> UNLIMITED_NBT_COMPOUND = PacketCodecs.nbtCompound(NbtSizeTracker::ofUnlimitedBytes);
    public static final PacketCodec<ByteBuf, Optional<NbtCompound>> OPTIONAL_NBT = new PacketCodec<ByteBuf, Optional<NbtCompound>>(){

        @Override
        public Optional<NbtCompound> decode(ByteBuf byteBuf) {
            return Optional.ofNullable(PacketByteBuf.readNbt(byteBuf));
        }

        @Override
        public void encode(ByteBuf byteBuf, Optional<NbtCompound> optional) {
            PacketByteBuf.writeNbt(byteBuf, optional.orElse(null));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Optional)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Vector3f> VECTOR_3F = new PacketCodec<ByteBuf, Vector3f>(){

        @Override
        public Vector3f decode(ByteBuf byteBuf) {
            return PacketByteBuf.readVector3f(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Vector3f vector3f) {
            PacketByteBuf.writeVector3f(byteBuf, vector3f);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Vector3f)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Quaternionf> QUATERNION_F = new PacketCodec<ByteBuf, Quaternionf>(){

        @Override
        public Quaternionf decode(ByteBuf byteBuf) {
            return PacketByteBuf.readQuaternionf(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Quaternionf quaternionf) {
            PacketByteBuf.writeQuaternionf(byteBuf, quaternionf);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Quaternionf)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, Integer> SYNC_ID = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return PacketByteBuf.readSyncId(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            PacketByteBuf.writeSyncId(byteBuf, integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, PropertyMap> PROPERTY_MAP = new PacketCodec<ByteBuf, PropertyMap>(){
        private static final int NAME_MAX_LENGTH = 64;
        private static final int VALUE_MAX_LENGTH = Short.MAX_VALUE;
        private static final int SIGNATURE_MAX_LENGTH = 1024;
        private static final int MAP_MAX_SIZE = 16;

        @Override
        public PropertyMap decode(ByteBuf byteBuf) {
            int i = PacketCodecs.readCollectionSize(byteBuf, 16);
            ImmutableMultimap.Builder<String, Property> builder = ImmutableMultimap.builder();
            for (int j = 0; j < i; ++j) {
                String string = StringEncoding.decode(byteBuf, 64);
                String string2 = StringEncoding.decode(byteBuf, Short.MAX_VALUE);
                String string3 = PacketByteBuf.readNullable(byteBuf, bufx -> StringEncoding.decode(bufx, 1024));
                Property property = new Property(string, string2, string3);
                builder.put(property.name(), property);
            }
            return new PropertyMap(builder.build());
        }

        @Override
        public void encode(ByteBuf byteBuf, PropertyMap propertyMap) {
            PacketCodecs.writeCollectionSize(byteBuf, propertyMap.size(), 16);
            for (Property property : propertyMap.values()) {
                StringEncoding.encode(byteBuf, property.name(), 64);
                StringEncoding.encode(byteBuf, property.value(), Short.MAX_VALUE);
                PacketByteBuf.writeNullable(byteBuf, property.signature(), (bufx, signature) -> StringEncoding.encode(bufx, signature, 1024));
            }
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (PropertyMap)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, String> PLAYER_NAME = PacketCodecs.string(16);
    public static final PacketCodec<ByteBuf, GameProfile> GAME_PROFILE = PacketCodec.tuple(Uuids.PACKET_CODEC, GameProfile::id, PLAYER_NAME, GameProfile::name, PROPERTY_MAP, GameProfile::properties, GameProfile::new);
    public static final PacketCodec<ByteBuf, Integer> RGB = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return ColorHelper.getArgb(byteBuf.readByte() & 0xFF, byteBuf.readByte() & 0xFF, byteBuf.readByte() & 0xFF);
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeByte(ColorHelper.getRed(integer));
            byteBuf.writeByte(ColorHelper.getGreen(integer));
            byteBuf.writeByte(ColorHelper.getBlue(integer));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    public static PacketCodec<ByteBuf, byte[]> byteArray(final int maxLength) {
        return new PacketCodec<ByteBuf, byte[]>(){

            @Override
            public byte[] decode(ByteBuf buf) {
                return PacketByteBuf.readByteArray(buf, maxLength);
            }

            @Override
            public void encode(ByteBuf buf, byte[] value) {
                if (value.length > maxLength) {
                    throw new EncoderException("ByteArray with size " + value.length + " is bigger than allowed " + maxLength);
                }
                PacketByteBuf.writeByteArray(buf, value);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (byte[])object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static PacketCodec<ByteBuf, String> string(final int maxLength) {
        return new PacketCodec<ByteBuf, String>(){

            @Override
            public String decode(ByteBuf byteBuf) {
                return StringEncoding.decode(byteBuf, maxLength);
            }

            @Override
            public void encode(ByteBuf byteBuf, String string) {
                StringEncoding.encode(byteBuf, string, maxLength);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (String)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static PacketCodec<ByteBuf, Optional<NbtElement>> nbtElement(final Supplier<NbtSizeTracker> sizeTrackerSupplier) {
        return new PacketCodec<ByteBuf, Optional<NbtElement>>(){

            @Override
            public Optional<NbtElement> decode(ByteBuf byteBuf) {
                return Optional.ofNullable(PacketByteBuf.readNbt(byteBuf, (NbtSizeTracker)sizeTrackerSupplier.get()));
            }

            @Override
            public void encode(ByteBuf byteBuf, Optional<NbtElement> optional) {
                PacketByteBuf.writeNbt(byteBuf, optional.orElse(null));
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (Optional)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static PacketCodec<ByteBuf, NbtElement> nbt(final Supplier<NbtSizeTracker> sizeTracker) {
        return new PacketCodec<ByteBuf, NbtElement>(){

            @Override
            public NbtElement decode(ByteBuf byteBuf) {
                NbtElement lv = PacketByteBuf.readNbt(byteBuf, (NbtSizeTracker)sizeTracker.get());
                if (lv == null) {
                    throw new DecoderException("Expected non-null compound tag");
                }
                return lv;
            }

            @Override
            public void encode(ByteBuf byteBuf, NbtElement arg) {
                if (arg == NbtEnd.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                }
                PacketByteBuf.writeNbt(byteBuf, arg);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (NbtElement)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static PacketCodec<ByteBuf, NbtCompound> nbtCompound(Supplier<NbtSizeTracker> sizeTracker) {
        return PacketCodecs.nbt(sizeTracker).xmap(nbt -> {
            if (nbt instanceof NbtCompound) {
                NbtCompound lv = (NbtCompound)nbt;
                return lv;
            }
            throw new DecoderException("Not a compound tag: " + String.valueOf(nbt));
        }, nbt -> nbt);
    }

    public static <T> PacketCodec<ByteBuf, T> unlimitedCodec(Codec<T> codec) {
        return PacketCodecs.codec(codec, NbtSizeTracker::ofUnlimitedBytes);
    }

    public static <T> PacketCodec<ByteBuf, T> codec(Codec<T> codec) {
        return PacketCodecs.codec(codec, () -> NbtSizeTracker.of(PacketByteBuf.MAX_READ_NBT_SIZE));
    }

    public static <T, B extends ByteBuf, V> PacketCodec.ResultFunction<B, T, V> fromCodec(final DynamicOps<T> ops, final Codec<V> codec) {
        return codec2 -> new PacketCodec<B, V>(){

            @Override
            public V decode(B byteBuf) {
                Object object = codec2.decode(byteBuf);
                return codec.parse(ops, object).getOrThrow(error -> new DecoderException("Failed to decode: " + error + " " + String.valueOf(object)));
            }

            @Override
            public void encode(B byteBuf, V object) {
                Object object2 = codec.encodeStart(ops, object).getOrThrow(error -> new EncoderException("Failed to encode: " + error + " " + String.valueOf(object)));
                codec2.encode(byteBuf, object2);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <T> PacketCodec<ByteBuf, T> codec(Codec<T> codec, Supplier<NbtSizeTracker> sizeTracker) {
        return PacketCodecs.nbt(sizeTracker).collect(PacketCodecs.fromCodec(NbtOps.INSTANCE, codec));
    }

    public static <T> PacketCodec<RegistryByteBuf, T> unlimitedRegistryCodec(Codec<T> codec) {
        return PacketCodecs.registryCodec(codec, NbtSizeTracker::ofUnlimitedBytes);
    }

    public static <T> PacketCodec<RegistryByteBuf, T> registryCodec(Codec<T> codec) {
        return PacketCodecs.registryCodec(codec, () -> NbtSizeTracker.of(PacketByteBuf.MAX_READ_NBT_SIZE));
    }

    public static <T> PacketCodec<RegistryByteBuf, T> registryCodec(final Codec<T> codec, Supplier<NbtSizeTracker> sizeTracker) {
        final PacketCodec<ByteBuf, NbtElement> lv = PacketCodecs.nbt(sizeTracker);
        return new PacketCodec<RegistryByteBuf, T>(){

            @Override
            public T decode(RegistryByteBuf arg) {
                NbtElement lv3 = (NbtElement)lv.decode(arg);
                RegistryOps<NbtElement> lv2 = arg.getRegistryManager().getOps(NbtOps.INSTANCE);
                return codec.parse(lv2, lv3).getOrThrow(error -> new DecoderException("Failed to decode: " + error + " " + String.valueOf(lv3)));
            }

            @Override
            public void encode(RegistryByteBuf arg, T object) {
                RegistryOps<NbtElement> lv3 = arg.getRegistryManager().getOps(NbtOps.INSTANCE);
                NbtElement lv2 = codec.encodeStart(lv3, object).getOrThrow(error -> new EncoderException("Failed to encode: " + error + " " + String.valueOf(object)));
                lv.encode(arg, lv2);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    public static <B extends ByteBuf, V> PacketCodec<B, Optional<V>> optional(final PacketCodec<? super B, V> codec) {
        return new PacketCodec<B, Optional<V>>(){

            @Override
            public Optional<V> decode(B byteBuf) {
                if (((ByteBuf)byteBuf).readBoolean()) {
                    return Optional.of(codec.decode(byteBuf));
                }
                return Optional.empty();
            }

            @Override
            public void encode(B byteBuf, Optional<V> optional) {
                if (optional.isPresent()) {
                    ((ByteBuf)byteBuf).writeBoolean(true);
                    codec.encode(byteBuf, optional.get());
                } else {
                    ((ByteBuf)byteBuf).writeBoolean(false);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Optional)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static int readCollectionSize(ByteBuf buf, int maxSize) {
        int j = VarInts.read(buf);
        if (j > maxSize) {
            throw new DecoderException(j + " elements exceeded max size of: " + maxSize);
        }
        return j;
    }

    public static void writeCollectionSize(ByteBuf buf, int size, int maxSize) {
        if (size > maxSize) {
            throw new EncoderException(size + " elements exceeded max size of: " + maxSize);
        }
        VarInts.write(buf, size);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> PacketCodec<B, C> collection(IntFunction<C> factory, PacketCodec<? super B, V> elementCodec) {
        return PacketCodecs.collection(factory, elementCodec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> PacketCodec<B, C> collection(final IntFunction<C> factory, final PacketCodec<? super B, V> elementCodec, final int maxSize) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B byteBuf) {
                int i = PacketCodecs.readCollectionSize(byteBuf, maxSize);
                Collection collection = (Collection)factory.apply(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    collection.add(elementCodec.decode(byteBuf));
                }
                return collection;
            }

            @Override
            public void encode(B byteBuf, C collection) {
                PacketCodecs.writeCollectionSize(byteBuf, collection.size(), maxSize);
                for (Object object : collection) {
                    elementCodec.encode(byteBuf, object);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (C)((Collection)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> PacketCodec.ResultFunction<B, V, C> toCollection(IntFunction<C> collectionFactory) {
        return codec -> PacketCodecs.collection(collectionFactory, codec);
    }

    public static <B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, List<V>> toList() {
        return codec -> PacketCodecs.collection(ArrayList::new, codec);
    }

    public static <B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, List<V>> toList(int maxLength) {
        return codec -> PacketCodecs.collection(ArrayList::new, codec, maxLength);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> PacketCodec<B, M> map(IntFunction<? extends M> factory, PacketCodec<? super B, K> keyCodec, PacketCodec<? super B, V> valueCodec) {
        return PacketCodecs.map(factory, keyCodec, valueCodec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> PacketCodec<B, M> map(final IntFunction<? extends M> factory, final PacketCodec<? super B, K> keyCodec, final PacketCodec<? super B, V> valueCodec, final int maxSize) {
        return new PacketCodec<B, M>(){

            @Override
            public void encode(B byteBuf, M map) {
                PacketCodecs.writeCollectionSize(byteBuf, map.size(), maxSize);
                map.forEach((object, object2) -> {
                    keyCodec.encode(byteBuf, object);
                    valueCodec.encode(byteBuf, object2);
                });
            }

            @Override
            public M decode(B byteBuf) {
                int i = PacketCodecs.readCollectionSize(byteBuf, maxSize);
                Map map = (Map)factory.apply(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    Object object = keyCodec.decode(byteBuf);
                    Object object2 = valueCodec.decode(byteBuf);
                    map.put(object, object2);
                }
                return map;
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (M)((Map)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, L, R> PacketCodec<B, Either<L, R>> either(final PacketCodec<? super B, L> left, final PacketCodec<? super B, R> right) {
        return new PacketCodec<B, Either<L, R>>(){

            @Override
            public Either<L, R> decode(B byteBuf) {
                if (((ByteBuf)byteBuf).readBoolean()) {
                    return Either.left(left.decode(byteBuf));
                }
                return Either.right(right.decode(byteBuf));
            }

            @Override
            public void encode(B byteBuf, Either<L, R> either) {
                either.ifLeft(value -> {
                    byteBuf.writeBoolean(true);
                    left.encode(byteBuf, value);
                }).ifRight(value -> {
                    byteBuf.writeBoolean(false);
                    right.encode(byteBuf, value);
                });
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Either)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, V> lengthPrepended(final int maxSize, final BiFunction<B, ByteBuf, B> bufWrapper) {
        return codec -> new PacketCodec<B, V>(){

            @Override
            public V decode(B byteBuf) {
                int i = VarInts.read(byteBuf);
                if (i > maxSize) {
                    throw new DecoderException("Buffer size " + i + " is larger than allowed limit of " + maxSize);
                }
                int j = ((ByteBuf)byteBuf).readerIndex();
                ByteBuf byteBuf2 = (ByteBuf)bufWrapper.apply(byteBuf, ((ByteBuf)byteBuf).slice(j, i));
                ((ByteBuf)byteBuf).readerIndex(j + i);
                return codec.decode(byteBuf2);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void encode(B byteBuf, V object) {
                ByteBuf byteBuf2 = (ByteBuf)bufWrapper.apply(byteBuf, ((ByteBuf)byteBuf).alloc().buffer());
                try {
                    codec.encode(byteBuf2, object);
                    int i = byteBuf2.readableBytes();
                    if (i > maxSize) {
                        throw new EncoderException("Buffer size " + i + " is  larger than allowed limit of " + maxSize);
                    }
                    VarInts.write(byteBuf, i);
                    ((ByteBuf)byteBuf).writeBytes(byteBuf2);
                } finally {
                    byteBuf2.release();
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <V> PacketCodec.ResultFunction<ByteBuf, V, V> lengthPrepended(int maxSize) {
        return PacketCodecs.lengthPrepended(maxSize, (byteBuf, bufToWrap) -> bufToWrap);
    }

    public static <V> PacketCodec.ResultFunction<RegistryByteBuf, V, V> lengthPrependedRegistry(int maxSize) {
        return PacketCodecs.lengthPrepended(maxSize, (arg, byteBuf) -> new RegistryByteBuf((ByteBuf)byteBuf, arg.getRegistryManager()));
    }

    public static <T> PacketCodec<ByteBuf, T> indexed(final IntFunction<T> indexToValue, final ToIntFunction<T> valueToIndex) {
        return new PacketCodec<ByteBuf, T>(){

            @Override
            public T decode(ByteBuf byteBuf) {
                int i = VarInts.read(byteBuf);
                return indexToValue.apply(i);
            }

            @Override
            public void encode(ByteBuf byteBuf, T object) {
                int i = valueToIndex.applyAsInt(object);
                VarInts.write(byteBuf, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static <T> PacketCodec<ByteBuf, T> entryOf(IndexedIterable<T> iterable) {
        return PacketCodecs.indexed(iterable::getOrThrow, iterable::getRawIdOrThrow);
    }

    private static <T, R> PacketCodec<RegistryByteBuf, R> registry(final RegistryKey<? extends Registry<T>> registry, final Function<Registry<T>, IndexedIterable<R>> registryTransformer) {
        return new PacketCodec<RegistryByteBuf, R>(){

            private IndexedIterable<R> getRegistryOrThrow(RegistryByteBuf buf) {
                return (IndexedIterable)registryTransformer.apply(buf.getRegistryManager().getOrThrow(registry));
            }

            @Override
            public R decode(RegistryByteBuf arg) {
                int i = VarInts.read(arg);
                return this.getRegistryOrThrow(arg).getOrThrow(i);
            }

            @Override
            public void encode(RegistryByteBuf arg, R object) {
                int i = this.getRegistryOrThrow(arg).getRawIdOrThrow(object);
                VarInts.write(arg, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    public static <T> PacketCodec<RegistryByteBuf, T> registryValue(RegistryKey<? extends Registry<T>> registry2) {
        return PacketCodecs.registry(registry2, registry -> registry);
    }

    public static <T> PacketCodec<RegistryByteBuf, RegistryEntry<T>> registryEntry(RegistryKey<? extends Registry<T>> registry) {
        return PacketCodecs.registry(registry, Registry::getIndexedEntries);
    }

    public static <T> PacketCodec<RegistryByteBuf, RegistryEntry<T>> registryEntry(final RegistryKey<? extends Registry<T>> registry, final PacketCodec<? super RegistryByteBuf, T> directCodec) {
        return new PacketCodec<RegistryByteBuf, RegistryEntry<T>>(){
            private static final int field_61045 = 0;

            private IndexedIterable<RegistryEntry<T>> getIndexedEntries(RegistryByteBuf buf) {
                return buf.getRegistryManager().getOrThrow(registry).getIndexedEntries();
            }

            @Override
            public RegistryEntry<T> decode(RegistryByteBuf arg) {
                int i = VarInts.read(arg);
                if (i == 0) {
                    return RegistryEntry.of(directCodec.decode(arg));
                }
                return this.getIndexedEntries(arg).getOrThrow(i - 1);
            }

            @Override
            public void encode(RegistryByteBuf arg, RegistryEntry<T> arg2) {
                switch (arg2.getType()) {
                    case REFERENCE: {
                        int i = this.getIndexedEntries(arg).getRawIdOrThrow(arg2);
                        VarInts.write(arg, i + 1);
                        break;
                    }
                    case DIRECT: {
                        VarInts.write(arg, 0);
                        directCodec.encode(arg, arg2.value());
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, (RegistryEntry)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    public static <T> PacketCodec<RegistryByteBuf, RegistryEntryList<T>> registryEntryList(final RegistryKey<? extends Registry<T>> registryRef) {
        return new PacketCodec<RegistryByteBuf, RegistryEntryList<T>>(){
            private static final int field_61046 = -1;
            private final PacketCodec<RegistryByteBuf, RegistryEntry<T>> entryCodec;
            {
                this.entryCodec = PacketCodecs.registryEntry(registryRef);
            }

            @Override
            public RegistryEntryList<T> decode(RegistryByteBuf arg) {
                int i = VarInts.read(arg) - 1;
                if (i == -1) {
                    RegistryWrapper.Impl lv = arg.getRegistryManager().getOrThrow(registryRef);
                    return lv.getOptional(TagKey.of(registryRef, (Identifier)Identifier.PACKET_CODEC.decode(arg))).orElseThrow();
                }
                ArrayList<RegistryEntry> list = new ArrayList<RegistryEntry>(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    list.add((RegistryEntry)this.entryCodec.decode(arg));
                }
                return RegistryEntryList.of(list);
            }

            @Override
            public void encode(RegistryByteBuf arg, RegistryEntryList<T> arg2) {
                Optional optional = arg2.getTagKey();
                if (optional.isPresent()) {
                    VarInts.write(arg, 0);
                    Identifier.PACKET_CODEC.encode(arg, optional.get().id());
                } else {
                    VarInts.write(arg, arg2.size() + 1);
                    for (RegistryEntry registryEntry : arg2) {
                        this.entryCodec.encode(arg, registryEntry);
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, (RegistryEntryList)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    public static PacketCodec<ByteBuf, JsonElement> lenientJson(final int maxLength) {
        return new PacketCodec<ByteBuf, JsonElement>(){
            private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

            @Override
            public JsonElement decode(ByteBuf byteBuf) {
                String string = StringEncoding.decode(byteBuf, maxLength);
                try {
                    return LenientJsonParser.parse(string);
                } catch (JsonSyntaxException jsonSyntaxException) {
                    throw new DecoderException("Failed to parse JSON", jsonSyntaxException);
                }
            }

            @Override
            public void encode(ByteBuf byteBuf, JsonElement jsonElement) {
                String string = GSON.toJson(jsonElement);
                StringEncoding.encode(byteBuf, string, maxLength);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (JsonElement)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }
}

