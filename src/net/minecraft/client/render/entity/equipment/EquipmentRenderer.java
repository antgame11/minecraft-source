/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.equipment;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EquipmentRenderer {
    private static final int field_54178 = 0;
    private final EquipmentModelLoader equipmentModelLoader;
    private final Function<LayerTextureKey, Identifier> layerTextures;
    private final Function<TrimSpriteKey, Sprite> trimSprites;

    public EquipmentRenderer(EquipmentModelLoader equipmentModelLoader, SpriteAtlasTexture armorTrimsAtlas) {
        this.equipmentModelLoader = equipmentModelLoader;
        this.layerTextures = Util.memoize(key -> key.layer.getFullTextureId(key.layerType));
        this.trimSprites = Util.memoize(key -> armorTrimsAtlas.getSprite(key.getTexture()));
    }

    public <S> void render(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model<? super S> model, S object, ItemStack arg4, MatrixStack arg5, OrderedRenderCommandQueue arg6, int i, int j) {
        this.render(layerType, assetKey, model, object, arg4, arg5, arg6, i, null, j, 1);
    }

    public <S> void render(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model<? super S> arg3, S object, ItemStack arg4, MatrixStack arg5, OrderedRenderCommandQueue arg6, int i, @Nullable Identifier arg7, int j, int k) {
        List<EquipmentModel.Layer> list = this.equipmentModelLoader.get(assetKey).getLayers(layerType);
        if (list.isEmpty()) {
            return;
        }
        int l = DyedColorComponent.getColor(arg4, 0);
        boolean bl = arg4.hasGlint();
        int m = k;
        for (EquipmentModel.Layer lv : list) {
            int n = EquipmentRenderer.getDyeColor(lv, l);
            if (n == 0) continue;
            Identifier lv2 = lv.usePlayerTexture() && arg7 != null ? arg7 : this.layerTextures.apply(new LayerTextureKey(layerType, lv));
            arg6.getBatchingQueue(m++).submitModel(arg3, object, arg5, RenderLayer.getArmorCutoutNoCull(lv2), i, OverlayTexture.DEFAULT_UV, n, null, j, null);
            if (bl) {
                arg6.getBatchingQueue(m++).submitModel(arg3, object, arg5, RenderLayer.getArmorEntityGlint(), i, OverlayTexture.DEFAULT_UV, n, null, j, null);
            }
            bl = false;
        }
        ArmorTrim lv3 = arg4.get(DataComponentTypes.TRIM);
        if (lv3 != null) {
            Sprite lv4 = this.trimSprites.apply(new TrimSpriteKey(lv3, layerType, assetKey));
            RenderLayer lv5 = TexturedRenderLayers.getArmorTrims(lv3.pattern().value().decal());
            arg6.getBatchingQueue(m++).submitModel(arg3, object, arg5, lv5, i, OverlayTexture.DEFAULT_UV, -1, lv4, j, null);
        }
    }

    private static int getDyeColor(EquipmentModel.Layer layer, int dyeColor) {
        Optional<EquipmentModel.Dyeable> optional = layer.dyeable();
        if (optional.isPresent()) {
            int j = optional.get().colorWhenUndyed().map(ColorHelper::fullAlpha).orElse(0);
            return dyeColor != 0 ? dyeColor : j;
        }
        return -1;
    }

    @Environment(value=EnvType.CLIENT)
    record LayerTextureKey(EquipmentModel.LayerType layerType, EquipmentModel.Layer layer) {
    }

    @Environment(value=EnvType.CLIENT)
    record TrimSpriteKey(ArmorTrim trim, EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> equipmentAssetId) {
        public Identifier getTexture() {
            return this.trim.getTextureId(this.layerType.getTrimsDirectory(), this.equipmentAssetId);
        }
    }
}

