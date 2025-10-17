/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model.special;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SimpleSpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class HeadModelRenderer
implements SimpleSpecialModelRenderer {
    private final SkullBlockEntityModel model;
    private final float animation;
    private final RenderLayer renderLayer;

    public HeadModelRenderer(SkullBlockEntityModel model, float animation, RenderLayer renderLayer) {
        this.model = model;
        this.animation = animation;
        this.renderLayer = renderLayer;
    }

    @Override
    public void render(ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int k) {
        SkullBlockEntityRenderer.render(null, 180.0f, this.animation, matrices, queue, light, this.model, this.renderLayer, k, null);
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack lv = new MatrixStack();
        lv.translate(0.5f, 0.0f, 0.5f);
        lv.scale(-1.0f, -1.0f, 1.0f);
        SkullBlockEntityModel.SkullModelState lv2 = new SkullBlockEntityModel.SkullModelState();
        lv2.poweredTicks = this.animation;
        lv2.yaw = 180.0f;
        this.model.setAngles(lv2);
        this.model.getRootPart().collectVertices(lv, vertices);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(SkullBlock.SkullType kind, Optional<Identifier> textureOverride, float animation) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)SkullBlock.SkullType.CODEC.fieldOf("kind")).forGetter(Unbaked::kind), Identifier.CODEC.optionalFieldOf("texture").forGetter(Unbaked::textureOverride), Codec.FLOAT.optionalFieldOf("animation", Float.valueOf(0.0f)).forGetter(Unbaked::animation)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        public Unbaked(SkullBlock.SkullType kind) {
            this(kind, Optional.empty(), 0.0f);
        }

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        @Nullable
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            SkullBlockEntityModel lv = SkullBlockEntityRenderer.getModels(context.entityModelSet(), this.kind);
            Identifier lv2 = this.textureOverride.map(id -> id.withPath(texture -> "textures/entity/" + texture + ".png")).orElse(null);
            if (lv == null) {
                return null;
            }
            RenderLayer lv3 = SkullBlockEntityRenderer.getCutoutRenderLayer(this.kind, lv2);
            return new HeadModelRenderer(lv, this.animation, lv3);
        }
    }
}

