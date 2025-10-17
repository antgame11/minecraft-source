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
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SimpleSpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ShulkerBoxModelRenderer
implements SimpleSpecialModelRenderer {
    private final ShulkerBoxBlockEntityRenderer blockEntityRenderer;
    private final float openness;
    private final Direction facing;
    private final SpriteIdentifier textureId;

    public ShulkerBoxModelRenderer(ShulkerBoxBlockEntityRenderer blockEntityRenderer, float openness, Direction facing, SpriteIdentifier textureId) {
        this.blockEntityRenderer = blockEntityRenderer;
        this.openness = openness;
        this.facing = facing;
        this.textureId = textureId;
    }

    @Override
    public void render(ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int k) {
        this.blockEntityRenderer.render(matrices, queue, light, overlay, this.facing, this.openness, null, this.textureId, k);
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        this.blockEntityRenderer.collectVertices(this.facing, this.openness, vertices);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Identifier texture, float openness, Direction facing) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("texture")).forGetter(Unbaked::texture), Codec.FLOAT.optionalFieldOf("openness", Float.valueOf(0.0f)).forGetter(Unbaked::openness), Direction.CODEC.optionalFieldOf("orientation", Direction.UP).forGetter(Unbaked::facing)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        public Unbaked() {
            this(Identifier.ofVanilla("shulker"), 0.0f, Direction.UP);
        }

        public Unbaked(DyeColor color) {
            this(TexturedRenderLayers.createShulkerId(color), 0.0f, Direction.UP);
        }

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            return new ShulkerBoxModelRenderer(new ShulkerBoxBlockEntityRenderer(context), this.openness, this.facing, TexturedRenderLayers.SHULKER_SPRITE_MAPPER.map(this.texture));
        }
    }
}

