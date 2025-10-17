/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelTypes;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class SpecialItemModel<T>
implements ItemModel {
    private final SpecialModelRenderer<T> specialModelType;
    private final ModelSettings settings;

    public SpecialItemModel(SpecialModelRenderer<T> specialModelType, ModelSettings settings) {
        this.specialModelType = specialModelType;
        this.settings = settings;
    }

    @Override
    public void update(ItemRenderState state, ItemStack stack, ItemModelManager resolver, ItemDisplayContext displayContext, @Nullable ClientWorld world, @Nullable HeldItemContext heldItemContext, int seed) {
        state.addModelKey(this);
        ItemRenderState.LayerRenderState lv = state.newLayer();
        if (stack.hasGlint()) {
            ItemRenderState.Glint lv2 = ItemRenderState.Glint.STANDARD;
            lv.setGlint(lv2);
            state.markAnimated();
            state.addModelKey((Object)lv2);
        }
        T object = this.specialModelType.getData(stack);
        lv.setVertices(() -> {
            HashSet<Vector3f> set = new HashSet<Vector3f>();
            this.specialModelType.collectVertices(set);
            return set.toArray(new Vector3f[0]);
        });
        lv.setSpecialModel(this.specialModelType, object);
        if (object != null) {
            state.addModelKey(object);
        }
        this.settings.addSettings(lv, displayContext);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Identifier base, SpecialModelRenderer.Unbaked specialModel) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("base")).forGetter(Unbaked::base), ((MapCodec)SpecialModelTypes.CODEC.fieldOf("model")).forGetter(Unbaked::specialModel)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.base);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            SpecialModelRenderer<?> lv = this.specialModel.bake(context);
            if (lv == null) {
                return context.missingItemModel();
            }
            ModelSettings lv2 = this.getSettings(context);
            return new SpecialItemModel(lv, lv2);
        }

        private ModelSettings getSettings(ItemModel.BakeContext context) {
            Baker lv = context.blockModelBaker();
            BakedSimpleModel lv2 = lv.getModel(this.base);
            ModelTextures lv3 = lv2.getTextures();
            return ModelSettings.resolveSettings(lv, lv2, lv3);
        }

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }
    }
}

