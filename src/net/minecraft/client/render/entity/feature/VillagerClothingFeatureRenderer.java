/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.VillagerResourceMetadata;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.VillagerDataRenderState;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

@Environment(value=EnvType.CLIENT)
public class VillagerClothingFeatureRenderer<S extends LivingEntityRenderState, M extends EntityModel<S>>
extends FeatureRenderer<S, M> {
    private static final Int2ObjectMap<Identifier> LEVEL_TO_ID = Util.make(new Int2ObjectOpenHashMap(), levelToId -> {
        levelToId.put(1, Identifier.ofVanilla("stone"));
        levelToId.put(2, Identifier.ofVanilla("iron"));
        levelToId.put(3, Identifier.ofVanilla("gold"));
        levelToId.put(4, Identifier.ofVanilla("emerald"));
        levelToId.put(5, Identifier.ofVanilla("diamond"));
    });
    private final Object2ObjectMap<RegistryKey<VillagerType>, VillagerResourceMetadata.HatType> villagerTypeToHat = new Object2ObjectOpenHashMap<RegistryKey<VillagerType>, VillagerResourceMetadata.HatType>();
    private final Object2ObjectMap<RegistryKey<VillagerProfession>, VillagerResourceMetadata.HatType> professionToHat = new Object2ObjectOpenHashMap<RegistryKey<VillagerProfession>, VillagerResourceMetadata.HatType>();
    private final ResourceManager resourceManager;
    private final String entityType;
    private final M field_61809;
    private final M field_61810;

    public VillagerClothingFeatureRenderer(FeatureRendererContext<S, M> context, ResourceManager resourceManager, String entityType, M arg3, M arg4) {
        super(context);
        this.resourceManager = resourceManager;
        this.entityType = entityType;
        this.field_61809 = arg3;
        this.field_61810 = arg4;
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, S arg3, float f, float g) {
        if (((LivingEntityRenderState)arg3).invisible) {
            return;
        }
        VillagerData lv = ((VillagerDataRenderState)arg3).getVillagerData();
        if (lv == null) {
            return;
        }
        RegistryEntry<VillagerType> lv2 = lv.type();
        RegistryEntry<VillagerProfession> lv3 = lv.profession();
        VillagerResourceMetadata.HatType lv4 = this.getHatType(this.villagerTypeToHat, "type", lv2);
        VillagerResourceMetadata.HatType lv5 = this.getHatType(this.professionToHat, "profession", lv3);
        Object lv6 = this.getContextModel();
        Identifier lv7 = this.getTexture("type", lv2);
        boolean bl = lv5 == VillagerResourceMetadata.HatType.NONE || lv5 == VillagerResourceMetadata.HatType.PARTIAL && lv4 != VillagerResourceMetadata.HatType.FULL;
        M lv8 = ((LivingEntityRenderState)arg3).baby ? this.field_61810 : this.field_61809;
        VillagerClothingFeatureRenderer.renderModel(bl ? lv6 : lv8, lv7, arg, arg2, i, arg3, -1, 1);
        if (!lv3.matchesKey(VillagerProfession.NONE) && !((LivingEntityRenderState)arg3).baby) {
            Identifier lv9 = this.getTexture("profession", lv3);
            VillagerClothingFeatureRenderer.renderModel(lv6, lv9, arg, arg2, i, arg3, -1, 2);
            if (!lv3.matchesKey(VillagerProfession.NITWIT)) {
                Identifier lv10 = this.getTexture("profession_level", (Identifier)LEVEL_TO_ID.get(MathHelper.clamp(lv.level(), 1, LEVEL_TO_ID.size())));
                VillagerClothingFeatureRenderer.renderModel(lv6, lv10, arg, arg2, i, arg3, -1, 3);
            }
        }
    }

    private Identifier getTexture(String keyType, Identifier keyId) {
        return keyId.withPath(path -> "textures/entity/" + this.entityType + "/" + keyType + "/" + path + ".png");
    }

    private Identifier getTexture(String keyType, RegistryEntry<?> entry) {
        return entry.getKey().map(key -> this.getTexture(keyType, key.getValue())).orElse(MissingSprite.getMissingSpriteId());
    }

    public <K> VillagerResourceMetadata.HatType getHatType(Object2ObjectMap<RegistryKey<K>, VillagerResourceMetadata.HatType> metadataMap, String keyType, RegistryEntry<K> entry) {
        RegistryKey lv = entry.getKey().orElse(null);
        if (lv == null) {
            return VillagerResourceMetadata.HatType.NONE;
        }
        return metadataMap.computeIfAbsent(lv, object -> this.resourceManager.getResource(this.getTexture(keyType, lv.getValue())).flatMap(resource -> {
            try {
                return resource.getMetadata().decode(VillagerResourceMetadata.SERIALIZER).map(VillagerResourceMetadata::hatType);
            } catch (IOException iOException) {
                return Optional.empty();
            }
        }).orElse(VillagerResourceMetadata.HatType.NONE));
    }
}

