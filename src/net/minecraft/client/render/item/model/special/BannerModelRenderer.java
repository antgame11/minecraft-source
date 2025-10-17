/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model.special;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class BannerModelRenderer
implements SpecialModelRenderer<BannerPatternsComponent> {
    private final BannerBlockEntityRenderer blockEntityRenderer;
    private final DyeColor baseColor;

    public BannerModelRenderer(DyeColor baseColor, BannerBlockEntityRenderer blockEntityRenderer) {
        this.blockEntityRenderer = blockEntityRenderer;
        this.baseColor = baseColor;
    }

    @Override
    @Nullable
    public BannerPatternsComponent getData(ItemStack arg) {
        return arg.get(DataComponentTypes.BANNER_PATTERNS);
    }

    @Override
    public void render(@Nullable BannerPatternsComponent arg, ItemDisplayContext arg2, MatrixStack arg3, OrderedRenderCommandQueue arg4, int i, int j, boolean bl, int k) {
        this.blockEntityRenderer.renderAsItem(arg3, arg4, i, j, this.baseColor, Objects.requireNonNullElse(arg, BannerPatternsComponent.DEFAULT), k);
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        this.blockEntityRenderer.collectVertices(vertices);
    }

    @Override
    @Nullable
    public /* synthetic */ Object getData(ItemStack stack) {
        return this.getData(stack);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(DyeColor baseColor) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DyeColor.CODEC.fieldOf("color")).forGetter(Unbaked::baseColor)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            return new BannerModelRenderer(this.baseColor, new BannerBlockEntityRenderer(context));
        }
    }
}

