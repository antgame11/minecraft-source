/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.dynamic;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import java.util.function.UnaryOperator;

abstract class AbstractListBuilder<T, B>
implements ListBuilder<T> {
    private final DynamicOps<T> ops;
    protected DataResult<B> builder = DataResult.success(this.initBuilder(), Lifecycle.stable());

    protected AbstractListBuilder(DynamicOps<T> ops) {
        this.ops = ops;
    }

    @Override
    public DynamicOps<T> ops() {
        return this.ops;
    }

    protected abstract B initBuilder();

    protected abstract B add(B var1, T var2);

    protected abstract DataResult<T> build(B var1, T var2);

    @Override
    public ListBuilder<T> add(T value) {
        this.builder = this.builder.map(object2 -> this.add(object2, value));
        return this;
    }

    @Override
    public ListBuilder<T> add(DataResult<T> value) {
        this.builder = this.builder.apply2stable(this::add, value);
        return this;
    }

    @Override
    public ListBuilder<T> withErrorsFrom(DataResult<?> result) {
        this.builder = this.builder.flatMap(object -> result.map(object2 -> object));
        return this;
    }

    @Override
    public ListBuilder<T> mapError(UnaryOperator<String> onError) {
        this.builder = this.builder.mapError(onError);
        return this;
    }

    @Override
    public DataResult<T> build(T prefix) {
        DataResult dataResult = this.builder.flatMap(object2 -> this.build(object2, prefix));
        this.builder = DataResult.success(this.initBuilder(), Lifecycle.stable());
        return dataResult;
    }
}

