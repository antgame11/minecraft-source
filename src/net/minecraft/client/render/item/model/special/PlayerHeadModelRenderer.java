/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model.special;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class PlayerHeadModelRenderer
implements SpecialModelRenderer<PlayerSkinCache.Entry> {
    private final PlayerSkinCache playerSkinCache;
    private final SkullBlockEntityModel model;

    PlayerHeadModelRenderer(PlayerSkinCache playerSkinCache, SkullBlockEntityModel model) {
        this.playerSkinCache = playerSkinCache;
        this.model = model;
    }

    @Override
    public void render(@Nullable PlayerSkinCache.Entry arg, ItemDisplayContext arg2, MatrixStack arg3, OrderedRenderCommandQueue arg4, int i, int j, boolean bl, int k) {
        RenderLayer lv = arg != null ? arg.getRenderLayer() : PlayerSkinCache.DEFAULT_RENDER_LAYER;
        SkullBlockEntityRenderer.render(null, 180.0f, 0.0f, arg3, arg4, i, this.model, lv, k, null);
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack lv = new MatrixStack();
        lv.translate(0.5f, 0.0f, 0.5f);
        lv.scale(-1.0f, -1.0f, 1.0f);
        this.model.getRootPart().collectVertices(lv, vertices);
    }

    @Override
    @Nullable
    public PlayerSkinCache.Entry getData(ItemStack arg) {
        ProfileComponent lv = arg.get(DataComponentTypes.PROFILE);
        if (lv == null) {
            return null;
        }
        return this.playerSkinCache.get(lv);
    }

    @Override
    @Nullable
    public /* synthetic */ Object getData(ItemStack stack) {
        return this.getData(stack);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        @Nullable
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            SkullBlockEntityModel lv = SkullBlockEntityRenderer.getModels(context.entityModelSet(), SkullBlock.Type.PLAYER);
            if (lv == null) {
                return null;
            }
            return new PlayerHeadModelRenderer(context.playerSkinRenderCache(), lv);
        }
    }
}

