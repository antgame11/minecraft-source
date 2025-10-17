/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.render.model.json.ModelVariantOperator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AxisRotation;

@Environment(value=EnvType.CLIENT)
public record ModelVariant(Identifier modelId, ModelState modelState) implements BlockModelPart.Unbaked
{
    public static final MapCodec<ModelVariant> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("model")).forGetter(ModelVariant::modelId), ModelState.CODEC.forGetter(ModelVariant::modelState)).apply((Applicative<ModelVariant, ?>)instance, ModelVariant::new));
    public static final Codec<ModelVariant> CODEC = MAP_CODEC.codec();

    public ModelVariant(Identifier model) {
        this(model, ModelState.DEFAULT);
    }

    public ModelVariant withRotationX(AxisRotation amount) {
        return this.setState(this.modelState.setRotationX(amount));
    }

    public ModelVariant withRotationY(AxisRotation amount) {
        return this.setState(this.modelState.setRotationY(amount));
    }

    public ModelVariant withUVLock(boolean uvLock) {
        return this.setState(this.modelState.setUVLock(uvLock));
    }

    public ModelVariant withModel(Identifier modelId) {
        return new ModelVariant(modelId, this.modelState);
    }

    public ModelVariant setState(ModelState modelState) {
        return new ModelVariant(this.modelId, modelState);
    }

    public ModelVariant with(ModelVariantOperator variantOperator) {
        return (ModelVariant)variantOperator.apply(this);
    }

    @Override
    public BlockModelPart bake(Baker baker) {
        return GeometryBakedModel.create(baker, this.modelId, this.modelState.asModelBakeSettings());
    }

    @Override
    public void resolve(ResolvableModel.Resolver resolver) {
        resolver.markDependency(this.modelId);
    }

    @Environment(value=EnvType.CLIENT)
    public record ModelState(AxisRotation x, AxisRotation y, boolean uvLock) {
        public static final MapCodec<ModelState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(AxisRotation.CODEC.optionalFieldOf("x", AxisRotation.R0).forGetter(ModelState::x), AxisRotation.CODEC.optionalFieldOf("y", AxisRotation.R0).forGetter(ModelState::y), Codec.BOOL.optionalFieldOf("uvlock", false).forGetter(ModelState::uvLock)).apply((Applicative<ModelState, ?>)instance, ModelState::new));
        public static final ModelState DEFAULT = new ModelState(AxisRotation.R0, AxisRotation.R0, false);

        public ModelBakeSettings asModelBakeSettings() {
            ModelRotation lv = ModelRotation.rotate(this.x, this.y);
            return this.uvLock ? lv.getUVModel() : lv;
        }

        public ModelState setRotationX(AxisRotation amount) {
            return new ModelState(amount, this.y, this.uvLock);
        }

        public ModelState setRotationY(AxisRotation amount) {
            return new ModelState(this.x, amount, this.uvLock);
        }

        public ModelState setUVLock(boolean uvLock) {
            return new ModelState(this.x, this.y, uvLock);
        }
    }
}

