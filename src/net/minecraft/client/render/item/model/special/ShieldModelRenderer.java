/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model.special;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ShieldModelRenderer
implements SpecialModelRenderer<ComponentMap> {
    private final SpriteHolder spriteHolder;
    private final ShieldEntityModel model;

    public ShieldModelRenderer(SpriteHolder spriteHolder, ShieldEntityModel model) {
        this.spriteHolder = spriteHolder;
        this.model = model;
    }

    @Override
    @Nullable
    public ComponentMap getData(ItemStack arg) {
        return arg.getImmutableComponents();
    }

    @Override
    public void render(@Nullable ComponentMap arg, ItemDisplayContext arg2, MatrixStack arg3, OrderedRenderCommandQueue arg4, int i, int j, boolean bl, int k) {
        BannerPatternsComponent lv = arg != null ? arg.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT) : BannerPatternsComponent.DEFAULT;
        DyeColor lv2 = arg != null ? arg.get(DataComponentTypes.BASE_COLOR) : null;
        boolean bl2 = !lv.layers().isEmpty() || lv2 != null;
        arg3.push();
        arg3.scale(1.0f, -1.0f, -1.0f);
        SpriteIdentifier lv3 = bl2 ? ModelBaker.SHIELD_BASE : ModelBaker.SHIELD_BASE_NO_PATTERN;
        arg4.submitModelPart(this.model.getHandle(), arg3, this.model.getLayer(lv3.getAtlasId()), i, j, this.spriteHolder.getSprite(lv3), false, false, -1, null, k);
        if (bl2) {
            BannerBlockEntityRenderer.renderCanvas(this.spriteHolder, arg3, arg4, i, j, this.model, Unit.INSTANCE, lv3, false, Objects.requireNonNullElse(lv2, DyeColor.WHITE), lv, bl, null, k);
        } else {
            arg4.submitModelPart(this.model.getPlate(), arg3, this.model.getLayer(lv3.getAtlasId()), i, j, this.spriteHolder.getSprite(lv3), false, bl, -1, null, k);
        }
        arg3.pop();
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack lv = new MatrixStack();
        lv.scale(1.0f, -1.0f, -1.0f);
        this.model.getRootPart().collectVertices(lv, vertices);
    }

    @Override
    @Nullable
    public /* synthetic */ Object getData(ItemStack stack) {
        return this.getData(stack);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(INSTANCE);

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            return new ShieldModelRenderer(context.spriteHolder(), new ShieldEntityModel(context.entityModelSet().getModelPart(EntityModelLayers.SHIELD)));
        }
    }
}

