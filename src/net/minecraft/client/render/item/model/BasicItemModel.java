/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.item.tint.TintSourceTypes;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.ModelSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class BasicItemModel
implements ItemModel {
    private final List<TintSource> tints;
    private final List<BakedQuad> quads;
    private final Supplier<Vector3f[]> vector;
    private final ModelSettings settings;
    private final boolean animated;

    public BasicItemModel(List<TintSource> tints, List<BakedQuad> quads, ModelSettings settings) {
        this.tints = tints;
        this.quads = quads;
        this.settings = settings;
        this.vector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(this.quads));
        boolean bl = false;
        for (BakedQuad lv : quads) {
            if (!lv.sprite().getContents().isAnimated()) continue;
            bl = true;
            break;
        }
        this.animated = bl;
    }

    public static Vector3f[] bakeQuads(List<BakedQuad> quads) {
        HashSet set = new HashSet();
        for (BakedQuad lv : quads) {
            BakedQuadFactory.calculatePosition(lv.vertexData(), set::add);
        }
        return (Vector3f[])set.toArray(Vector3f[]::new);
    }

    @Override
    public void update(ItemRenderState state, ItemStack stack, ItemModelManager resolver, ItemDisplayContext displayContext, @Nullable ClientWorld world, @Nullable HeldItemContext heldItemContext, int seed) {
        state.addModelKey(this);
        ItemRenderState.LayerRenderState lv = state.newLayer();
        if (stack.hasGlint()) {
            ItemRenderState.Glint lv2 = BasicItemModel.shouldUseSpecialGlint(stack) ? ItemRenderState.Glint.SPECIAL : ItemRenderState.Glint.STANDARD;
            lv.setGlint(lv2);
            state.markAnimated();
            state.addModelKey((Object)lv2);
        }
        int j = this.tints.size();
        int[] is = lv.initTints(j);
        for (int k = 0; k < j; ++k) {
            int l;
            is[k] = l = this.tints.get(k).getTint(stack, world, heldItemContext == null ? null : heldItemContext.getEntity());
            state.addModelKey(l);
        }
        lv.setVertices(this.vector);
        lv.setRenderLayer(RenderLayers.getItemLayer(stack));
        this.settings.addSettings(lv, displayContext);
        lv.getQuads().addAll(this.quads);
        if (this.animated) {
            state.markAnimated();
        }
    }

    private static boolean shouldUseSpecialGlint(ItemStack stack) {
        return stack.isIn(ItemTags.COMPASSES) || stack.isOf(Items.CLOCK);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Identifier model, List<TintSource> tints) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("model")).forGetter(Unbaked::model), TintSourceTypes.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            Baker lv = context.blockModelBaker();
            BakedSimpleModel lv2 = lv.getModel(this.model);
            ModelTextures lv3 = lv2.getTextures();
            List<BakedQuad> list = lv2.bakeGeometry(lv3, lv, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings lv4 = ModelSettings.resolveSettings(lv, lv2, lv3);
            return new BasicItemModel(this.tints, list, lv4);
        }

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }
    }
}

