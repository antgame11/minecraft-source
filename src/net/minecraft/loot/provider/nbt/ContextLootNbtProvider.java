/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.nbt;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.nbt.LootNbtProviderTypes;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public class ContextLootNbtProvider
implements LootNbtProvider {
    private static final Codecs.IdMapper<String, Target<?>> TARGETS = new Codecs.IdMapper();
    private static final Codec<Target<?>> TARGET_CODEC;
    public static final MapCodec<ContextLootNbtProvider> CODEC;
    public static final Codec<ContextLootNbtProvider> INLINE_CODEC;
    private final Target<?> target;

    private ContextLootNbtProvider(Target<?> target) {
        this.target = target;
    }

    @Override
    public LootNbtProviderType getType() {
        return LootNbtProviderTypes.CONTEXT;
    }

    @Override
    @Nullable
    public NbtElement getNbt(LootContext context) {
        return this.target.getNbt(context);
    }

    @Override
    public Set<ContextParameter<?>> getRequiredParameters() {
        return Set.of(this.target.contextParam());
    }

    public static LootNbtProvider fromTarget(LootContext.EntityReference target) {
        return new ContextLootNbtProvider(new EntityTarget(target.getParameter()));
    }

    static {
        for (LootContext.EntityReference entityReference : LootContext.EntityReference.values()) {
            TARGETS.put(entityReference.asString(), new EntityTarget(entityReference.getParameter()));
        }
        for (Enum enum_ : LootContext.BlockEntityReference.values()) {
            TARGETS.put(((LootContext.BlockEntityReference)enum_).asString(), new BlockEntityTarget(((LootContext.BlockEntityReference)enum_).getParameter()));
        }
        TARGET_CODEC = TARGETS.getCodec(Codec.STRING);
        CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)TARGET_CODEC.fieldOf("target")).forGetter(provider -> provider.target)).apply((Applicative<ContextLootNbtProvider, ?>)instance, ContextLootNbtProvider::new));
        INLINE_CODEC = TARGET_CODEC.xmap(ContextLootNbtProvider::new, provider -> provider.target);
    }

    static interface Target<T> {
        public ContextParameter<? extends T> contextParam();

        @Nullable
        public NbtElement getNbt(T var1);

        @Nullable
        default public NbtElement getNbt(LootContext context) {
            T object = context.get(this.contextParam());
            return object != null ? this.getNbt(object) : null;
        }
    }

    record EntityTarget(ContextParameter<? extends Entity> contextParam) implements Target<Entity>
    {
        @Override
        public NbtElement getNbt(Entity arg) {
            return NbtPredicate.entityToNbt(arg);
        }
    }

    record BlockEntityTarget(ContextParameter<? extends BlockEntity> contextParam) implements Target<BlockEntity>
    {
        @Override
        public NbtElement getNbt(BlockEntity arg) {
            return arg.createNbtWithIdentifyingData(arg.getWorld().getRegistryManager());
        }
    }
}

