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
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BedBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SimpleSpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class BedModelRenderer
implements SimpleSpecialModelRenderer {
    private final BedBlockEntityRenderer blockEntityRenderer;
    private final SpriteIdentifier textureId;

    public BedModelRenderer(BedBlockEntityRenderer blockEntityRenderer, SpriteIdentifier textureId) {
        this.blockEntityRenderer = blockEntityRenderer;
        this.textureId = textureId;
    }

    @Override
    public void render(ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int k) {
        this.blockEntityRenderer.renderAsItem(matrices, queue, light, overlay, this.textureId, k);
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        this.blockEntityRenderer.collectVertices(vertices);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Identifier texture) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("texture")).forGetter(Unbaked::texture)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        public Unbaked(DyeColor color) {
            this(TexturedRenderLayers.createColorId(color));
        }

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            return new BedModelRenderer(new BedBlockEntityRenderer(context), TexturedRenderLayers.BED_SPRITE_MAPPER.map(this.texture));
        }
    }
}

