/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.property.numeric;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.CompassState;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CompassProperty
implements NumericProperty {
    public static final MapCodec<CompassProperty> CODEC = CompassState.CODEC.xmap(CompassProperty::new, property -> property.state);
    private final CompassState state;

    public CompassProperty(boolean wobble, CompassState.Target target) {
        this(new CompassState(wobble, target));
    }

    private CompassProperty(CompassState state) {
        this.state = state;
    }

    @Override
    public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable HeldItemContext context, int seed) {
        return this.state.getValue(stack, world, context, seed);
    }

    public MapCodec<CompassProperty> getCodec() {
        return CODEC;
    }
}

