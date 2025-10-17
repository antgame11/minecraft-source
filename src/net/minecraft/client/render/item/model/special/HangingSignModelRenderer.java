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
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.WoodType;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.HangingSignBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SimpleSpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class HangingSignModelRenderer
implements SimpleSpecialModelRenderer {
    private final SpriteHolder spriteHolder;
    private final Model.SinglePartModel model;
    private final SpriteIdentifier texture;

    public HangingSignModelRenderer(SpriteHolder spriteHolder, Model.SinglePartModel model, SpriteIdentifier texture) {
        this.spriteHolder = spriteHolder;
        this.model = model;
        this.texture = texture;
    }

    @Override
    public void render(ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int k) {
        HangingSignBlockEntityRenderer.renderAsItem(this.spriteHolder, matrices, queue, light, overlay, this.model, this.texture);
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack lv = new MatrixStack();
        HangingSignBlockEntityRenderer.setAngles(lv, 0.0f);
        lv.scale(1.0f, -1.0f, -1.0f);
        this.model.getRootPart().collectVertices(lv, vertices);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(WoodType woodType, Optional<Identifier> texture) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)WoodType.CODEC.fieldOf("wood_type")).forGetter(Unbaked::woodType), Identifier.CODEC.optionalFieldOf("texture").forGetter(Unbaked::texture)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        public Unbaked(WoodType woodType) {
            this(woodType, Optional.empty());
        }

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            Model.SinglePartModel lv = HangingSignBlockEntityRenderer.createModel(context.entityModelSet(), this.woodType, HangingSignBlockEntityRenderer.AttachmentType.CEILING_MIDDLE);
            SpriteIdentifier lv2 = this.texture.map(TexturedRenderLayers.HANGING_SIGN_SPRITE_MAPPER::map).orElseGet(() -> TexturedRenderLayers.getHangingSignTextureId(this.woodType));
            return new HangingSignModelRenderer(context.spriteHolder(), lv, lv2);
        }
    }
}

