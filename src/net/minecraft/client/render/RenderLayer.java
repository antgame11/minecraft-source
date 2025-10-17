/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.ScissorState;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.block.entity.AbstractEndPortalBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public abstract class RenderLayer
extends RenderPhase {
    private static final int field_32777 = 0x100000;
    public static final int SOLID_BUFFER_SIZE = 0x400000;
    public static final int CUTOUT_BUFFER_SIZE = 786432;
    public static final int DEFAULT_BUFFER_SIZE = 1536;
    private static final RenderLayer SOLID = RenderLayer.of("solid", 1536, true, false, RenderPipelines.SOLID, MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).build(true));
    private static final RenderLayer CUTOUT_MIPPED = RenderLayer.of("cutout_mipped", 1536, true, false, RenderPipelines.CUTOUT_MIPPED, MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).build(true));
    private static final RenderLayer CUTOUT = RenderLayer.of("cutout", 1536, true, false, RenderPipelines.CUTOUT, MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).texture(BLOCK_ATLAS_TEXTURE).build(true));
    private static final RenderLayer TRANSLUCENT_MOVING_BLOCK = RenderLayer.of("translucent_moving_block", 786432, false, true, RenderPipelines.RENDERTYPE_TRANSLUCENT_MOVING_BLOCK, MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).target(ITEM_ENTITY_TARGET).build(true));
    private static final Function<Identifier, RenderLayer> ARMOR_CUTOUT_NO_CULL = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING).build(true);
        return RenderLayer.of("armor_cutout_no_cull", 1536, true, false, RenderPipelines.ARMOR_CUTOUT_NO_CULL, lv);
    });
    private static final Function<Identifier, RenderLayer> ARMOR_TRANSLUCENT = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING).build(true);
        return RenderLayer.of("armor_translucent", 1536, true, true, RenderPipelines.ARMOR_TRANSLUCENT, lv);
    });
    private static final Function<Identifier, RenderLayer> ENTITY_SOLID = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
        return RenderLayer.of("entity_solid", 1536, true, false, RenderPipelines.ENTITY_SOLID, lv);
    });
    private static final Function<Identifier, RenderLayer> ENTITY_SOLID_Z_OFFSET_FORWARD = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING_FORWARD).build(true);
        return RenderLayer.of("entity_solid_z_offset_forward", 1536, true, false, RenderPipelines.ENTITY_SOLID_OFFSET_FORWARD, lv);
    });
    private static final Function<Identifier, RenderLayer> ENTITY_CUTOUT = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
        return RenderLayer.of("entity_cutout", 1536, true, false, RenderPipelines.ENTITY_CUTOUT, lv);
    });
    private static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY_CUTOUT_NO_CULL = Util.memoize((texture, affectsOutline) -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build((boolean)affectsOutline);
        return RenderLayer.of("entity_cutout_no_cull", 1536, true, false, RenderPipelines.ENTITY_CUTOUT_NO_CULL, lv);
    });
    private static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY_CUTOUT_NO_CULL_Z_OFFSET = Util.memoize((texture, affectsOutline) -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING).build((boolean)affectsOutline);
        return RenderLayer.of("entity_cutout_no_cull_z_offset", 1536, true, false, RenderPipelines.ENTITY_CUTOUT_NO_CULL_Z_OFFSET, lv);
    });
    private static final Function<Identifier, RenderLayer> ITEM_ENTITY_TRANSLUCENT_CULL = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).target(ITEM_ENTITY_TARGET).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
        return RenderLayer.of("item_entity_translucent_cull", 1536, true, true, RenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL, lv);
    });
    private static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY_TRANSLUCENT = Util.memoize((texture, affectsOutline) -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build((boolean)affectsOutline);
        return RenderLayer.of("entity_translucent", 1536, true, true, RenderPipelines.ENTITY_TRANSLUCENT, lv);
    });
    private static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY_TRANSLUCENT_EMISSIVE = Util.memoize((texture, affectsOutline) -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).overlay(ENABLE_OVERLAY_COLOR).build((boolean)affectsOutline);
        return RenderLayer.of("entity_translucent_emissive", 1536, true, true, RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, lv);
    });
    private static final Function<Identifier, RenderLayer> ENTITY_SMOOTH_CUTOUT = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
        return RenderLayer.of("entity_smooth_cutout", 1536, RenderPipelines.ENTITY_SMOOTH_CUTOUT, lv);
    });
    private static final BiFunction<Identifier, Boolean, RenderLayer> BEACON_BEAM = Util.memoize((texture, affectsOutline) -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).build(false);
        return RenderLayer.of("beacon_beam", 1536, false, true, affectsOutline != false ? RenderPipelines.BEACON_BEAM_TRANSLUCENT : RenderPipelines.BEACON_BEAM_OPAQUE, lv);
    });
    private static final Function<Identifier, RenderLayer> ENTITY_DECAL = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(false);
        return RenderLayer.of("entity_decal", 1536, RenderPipelines.RENDERTYPE_ENTITY_DECAL, lv);
    });
    private static final Function<Identifier, RenderLayer> ENTITY_NO_OUTLINE = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(false);
        return RenderLayer.of("entity_no_outline", 1536, false, true, RenderPipelines.ENTITY_NO_OUTLINE, lv);
    });
    private static final Function<Identifier, RenderLayer> ENTITY_SHADOW = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING).build(false);
        return RenderLayer.of("entity_shadow", 1536, false, false, RenderPipelines.RENDERTYPE_ENTITY_SHADOW, lv);
    });
    private static final Function<Identifier, RenderLayer> ENTITY_ALPHA = Util.memoize(texture -> {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).build(true);
        return RenderLayer.of("entity_alpha", 1536, RenderPipelines.RENDERTYPE_ENTITY_ALPHA, lv);
    });
    private static final Function<Identifier, RenderLayer> EYES = Util.memoize(texture -> {
        RenderPhase.Texture lv = new RenderPhase.Texture((Identifier)texture, false);
        return RenderLayer.of("eyes", 1536, false, true, RenderPipelines.ENTITY_EYES, MultiPhaseParameters.builder().texture(lv).build(false));
    });
    private static final RenderLayer LEASH = RenderLayer.of("leash", 1536, RenderPipelines.RENDERTYPE_LEASH, MultiPhaseParameters.builder().texture(NO_TEXTURE).lightmap(ENABLE_LIGHTMAP).build(false));
    private static final RenderLayer WATER_MASK = RenderLayer.of("water_mask", 1536, RenderPipelines.RENDERTYPE_WATER_MASK, MultiPhaseParameters.builder().texture(NO_TEXTURE).build(false));
    private static final RenderLayer ARMOR_ENTITY_GLINT = RenderLayer.of("armor_entity_glint", 1536, RenderPipelines.GLINT, MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ENTITY_ENCHANTMENT_GLINT, false)).texturing(ARMOR_ENTITY_GLINT_TEXTURING).layering(VIEW_OFFSET_Z_LAYERING).build(false));
    private static final RenderLayer GLINT_TRANSLUCENT = RenderLayer.of("glint_translucent", 1536, RenderPipelines.GLINT, MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, false)).texturing(GLINT_TEXTURING).target(ITEM_ENTITY_TARGET).build(false));
    private static final RenderLayer GLINT = RenderLayer.of("glint", 1536, RenderPipelines.GLINT, MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, false)).texturing(GLINT_TEXTURING).build(false));
    private static final RenderLayer ENTITY_GLINT = RenderLayer.of("entity_glint", 1536, RenderPipelines.GLINT, MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, false)).texturing(ENTITY_GLINT_TEXTURING).build(false));
    private static final Function<Identifier, RenderLayer> CRUMBLING = Util.memoize(texture -> {
        RenderPhase.Texture lv = new RenderPhase.Texture((Identifier)texture, false);
        return RenderLayer.of("crumbling", 1536, false, true, RenderPipelines.RENDERTYPE_CRUMBLING, MultiPhaseParameters.builder().texture(lv).build(false));
    });
    private static final Function<Identifier, RenderLayer> TEXT = Util.memoize(texture -> RenderLayer.of("text", 786432, false, false, RenderPipelines.RENDERTYPE_TEXT, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).build(false)));
    private static final RenderLayer TEXT_BACKGROUND = RenderLayer.of("text_background", 1536, false, true, RenderPipelines.RENDERTYPE_TEXT_BG, MultiPhaseParameters.builder().texture(NO_TEXTURE).lightmap(ENABLE_LIGHTMAP).build(false));
    private static final Function<Identifier, RenderLayer> TEXT_INTENSITY = Util.memoize(texture -> RenderLayer.of("text_intensity", 786432, false, false, RenderPipelines.RENDERTYPE_TEXT_INTENSITY, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).build(false)));
    private static final Function<Identifier, RenderLayer> TEXT_POLYGON_OFFSET = Util.memoize(texture -> RenderLayer.of("text_polygon_offset", 1536, false, true, RenderPipelines.RENDERTYPE_TEXT_POLYGON_OFFSET, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).build(false)));
    private static final Function<Identifier, RenderLayer> TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize(texture -> RenderLayer.of("text_intensity_polygon_offset", 1536, false, true, RenderPipelines.RENDERTYPE_TEXT_INTENSITY, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).build(false)));
    private static final Function<Identifier, RenderLayer> TEXT_SEE_THROUGH = Util.memoize(texture -> RenderLayer.of("text_see_through", 1536, false, false, RenderPipelines.RENDERTYPE_TEXT_SEETHROUGH, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).build(false)));
    private static final RenderLayer TEXT_BACKGROUND_SEE_THROUGH = RenderLayer.of("text_background_see_through", 1536, false, true, RenderPipelines.RENDERTYPE_TEXT_BG_SEETHROUGH, MultiPhaseParameters.builder().texture(NO_TEXTURE).lightmap(ENABLE_LIGHTMAP).build(false));
    private static final Function<Identifier, RenderLayer> TEXT_INTENSITY_SEE_THROUGH = Util.memoize(texture -> RenderLayer.of("text_intensity_see_through", 1536, false, true, RenderPipelines.RENDERTYPE_TEXT_INTENSITY_SEETHROUGH, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).lightmap(ENABLE_LIGHTMAP).build(false)));
    private static final RenderLayer LIGHTNING = RenderLayer.of("lightning", 1536, false, true, RenderPipelines.RENDERTYPE_LIGHTNING, MultiPhaseParameters.builder().target(WEATHER_TARGET).build(false));
    private static final RenderLayer DRAGON_RAYS = RenderLayer.of("dragon_rays", 1536, false, false, RenderPipelines.RENDERTYPE_LIGHTNING_DRAGON_RAYS, MultiPhaseParameters.builder().build(false));
    private static final RenderLayer DRAGON_RAYS_DEPTH = RenderLayer.of("dragon_rays_depth", 1536, false, false, RenderPipelines.POSITION_DRAGON_RAYS_DEPTH, MultiPhaseParameters.builder().build(false));
    private static final RenderLayer TRIPWIRE = RenderLayer.of("tripwire", 1536, true, true, RenderPipelines.TRIPWIRE, MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).target(WEATHER_TARGET).build(true));
    private static final RenderLayer END_PORTAL = RenderLayer.of("end_portal", 1536, false, false, RenderPipelines.END_PORTAL, MultiPhaseParameters.builder().texture(RenderPhase.Textures.create().add(AbstractEndPortalBlockEntityRenderer.SKY_TEXTURE, false).add(AbstractEndPortalBlockEntityRenderer.PORTAL_TEXTURE, false).build()).build(false));
    private static final RenderLayer END_GATEWAY = RenderLayer.of("end_gateway", 1536, false, false, RenderPipelines.END_GATEWAY, MultiPhaseParameters.builder().texture(RenderPhase.Textures.create().add(AbstractEndPortalBlockEntityRenderer.SKY_TEXTURE, false).add(AbstractEndPortalBlockEntityRenderer.PORTAL_TEXTURE, false).build()).build(false));
    public static final MultiPhase LINES = RenderLayer.of("lines", 1536, RenderPipelines.LINES, MultiPhaseParameters.builder().lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty())).layering(VIEW_OFFSET_Z_LAYERING).target(ITEM_ENTITY_TARGET).build(false));
    public static final MultiPhase SECONDARY_BLOCK_OUTLINE = RenderLayer.of("secondary_block_outline", 1536, RenderPipelines.SECOND_BLOCK_OUTLINE, MultiPhaseParameters.builder().lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(7.0))).layering(VIEW_OFFSET_Z_LAYERING).target(ITEM_ENTITY_TARGET).build(false));
    public static final MultiPhase LINE_STRIP = RenderLayer.of("line_strip", 1536, RenderPipelines.LINE_STRIP, MultiPhaseParameters.builder().lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty())).layering(VIEW_OFFSET_Z_LAYERING).target(ITEM_ENTITY_TARGET).build(false));
    private static final Function<Double, MultiPhase> DEBUG_LINE_STRIP = Util.memoize(lineWidth -> RenderLayer.of("debug_line_strip", 1536, RenderPipelines.DEBUG_LINE_STRIP, MultiPhaseParameters.builder().lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(lineWidth))).build(false)));
    private static final MultiPhase DEBUG_FILLED_BOX = RenderLayer.of("debug_filled_box", 1536, false, true, RenderPipelines.DEBUG_FILLED_BOX, MultiPhaseParameters.builder().layering(VIEW_OFFSET_Z_LAYERING).build(false));
    private static final MultiPhase DEBUG_QUADS = RenderLayer.of("debug_quads", 1536, false, true, RenderPipelines.DEBUG_QUADS, MultiPhaseParameters.builder().build(false));
    private static final MultiPhase DEBUG_TRIANGLE_FAN = RenderLayer.of("debug_triangle_fan", 1536, false, true, RenderPipelines.DEBUG_TRIANGLE_FAN, MultiPhaseParameters.builder().build(false));
    private static final MultiPhase DEBUG_STRUCTURE_QUADS = RenderLayer.of("debug_structure_quads", 1536, false, true, RenderPipelines.DEBUG_STRUCTURE_QUADS, MultiPhaseParameters.builder().build(false));
    private static final MultiPhase DEBUG_SECTION_QUADS = RenderLayer.of("debug_section_quads", 1536, false, true, RenderPipelines.DEBUG_SECTION_QUADS, MultiPhaseParameters.builder().layering(VIEW_OFFSET_Z_LAYERING).build(false));
    private static final Function<Identifier, RenderLayer> WEATHER_ALL_MASK = RenderLayer.createWeather(RenderPipelines.WEATHER_DEPTH);
    private static final Function<Identifier, RenderLayer> WEATHER_COLOR_MASK = RenderLayer.createWeather(RenderPipelines.WEATHER_NO_DEPTH);
    private static final Function<Identifier, RenderLayer> BLOCK_SCREEN_EFFECT = Util.memoize(texture -> RenderLayer.of("block_screen_effect", 1536, false, false, RenderPipelines.BLOCK_SCREEN_EFFECT, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).build(false)));
    private static final Function<Identifier, RenderLayer> FIRE_SCREEN_EFFECT = Util.memoize(texture -> RenderLayer.of("fire_screen_effect", 1536, false, false, RenderPipelines.FIRE_SCREEN_EFFECT, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).build(false)));
    private final int expectedBufferSize;
    private final boolean hasCrumbling;
    private final boolean translucent;

    public static RenderLayer getSolid() {
        return SOLID;
    }

    public static RenderLayer getCutoutMipped() {
        return CUTOUT_MIPPED;
    }

    public static RenderLayer getCutout() {
        return CUTOUT;
    }

    public static RenderLayer getTranslucentMovingBlock() {
        return TRANSLUCENT_MOVING_BLOCK;
    }

    public static RenderLayer getArmorCutoutNoCull(Identifier texture) {
        return ARMOR_CUTOUT_NO_CULL.apply(texture);
    }

    public static RenderLayer createArmorDecalCutoutNoCull(Identifier texture) {
        MultiPhaseParameters lv = MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING).build(true);
        return RenderLayer.of("armor_decal_cutout_no_cull", 1536, true, false, RenderPipelines.ARMOR_DECAL_CUTOUT_NO_CULL, lv);
    }

    public static RenderLayer createArmorTranslucent(Identifier texture) {
        return ARMOR_TRANSLUCENT.apply(texture);
    }

    public static RenderLayer getEntitySolid(Identifier texture) {
        return ENTITY_SOLID.apply(texture);
    }

    public static RenderLayer getEntitySolidZOffsetForward(Identifier texture) {
        return ENTITY_SOLID_Z_OFFSET_FORWARD.apply(texture);
    }

    public static RenderLayer getEntityCutout(Identifier texture) {
        return ENTITY_CUTOUT.apply(texture);
    }

    public static RenderLayer getEntityCutoutNoCull(Identifier texture, boolean affectsOutline) {
        return ENTITY_CUTOUT_NO_CULL.apply(texture, affectsOutline);
    }

    public static RenderLayer getEntityCutoutNoCull(Identifier texture) {
        return RenderLayer.getEntityCutoutNoCull(texture, true);
    }

    public static RenderLayer getEntityCutoutNoCullZOffset(Identifier texture, boolean affectsOutline) {
        return ENTITY_CUTOUT_NO_CULL_Z_OFFSET.apply(texture, affectsOutline);
    }

    public static RenderLayer getEntityCutoutNoCullZOffset(Identifier texture) {
        return RenderLayer.getEntityCutoutNoCullZOffset(texture, true);
    }

    public static RenderLayer getItemEntityTranslucentCull(Identifier texture) {
        return ITEM_ENTITY_TRANSLUCENT_CULL.apply(texture);
    }

    public static RenderLayer getEntityTranslucent(Identifier texture, boolean affectsOutline) {
        return ENTITY_TRANSLUCENT.apply(texture, affectsOutline);
    }

    public static RenderLayer getEntityTranslucent(Identifier texture) {
        return RenderLayer.getEntityTranslucent(texture, true);
    }

    public static RenderLayer getEntityTranslucentEmissive(Identifier texture, boolean affectsOutline) {
        return ENTITY_TRANSLUCENT_EMISSIVE.apply(texture, affectsOutline);
    }

    public static RenderLayer getEntityTranslucentEmissive(Identifier texture) {
        return RenderLayer.getEntityTranslucentEmissive(texture, true);
    }

    public static RenderLayer getEntitySmoothCutout(Identifier texture) {
        return ENTITY_SMOOTH_CUTOUT.apply(texture);
    }

    public static RenderLayer getBeaconBeam(Identifier texture, boolean translucent) {
        return BEACON_BEAM.apply(texture, translucent);
    }

    public static RenderLayer getEntityDecal(Identifier texture) {
        return ENTITY_DECAL.apply(texture);
    }

    public static RenderLayer getEntityNoOutline(Identifier texture) {
        return ENTITY_NO_OUTLINE.apply(texture);
    }

    public static RenderLayer getEntityShadow(Identifier texture) {
        return ENTITY_SHADOW.apply(texture);
    }

    public static RenderLayer getEntityAlpha(Identifier texture) {
        return ENTITY_ALPHA.apply(texture);
    }

    public static RenderLayer getEyes(Identifier texture) {
        return EYES.apply(texture);
    }

    public static RenderLayer getEntityTranslucentEmissiveNoOutline(Identifier texture) {
        return ENTITY_TRANSLUCENT_EMISSIVE.apply(texture, false);
    }

    public static RenderLayer getBreezeWind(Identifier texture, float x, float y) {
        return RenderLayer.of("breeze_wind", 1536, false, true, RenderPipelines.BREEZE_WIND, MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false)).texturing(new RenderPhase.OffsetTexturing(x, y)).lightmap(ENABLE_LIGHTMAP).overlay(DISABLE_OVERLAY_COLOR).build(false));
    }

    public static RenderLayer getEnergySwirl(Identifier texture, float x, float y) {
        return RenderLayer.of("energy_swirl", 1536, false, true, RenderPipelines.ENTITY_ENERGY_SWIRL, MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false)).texturing(new RenderPhase.OffsetTexturing(x, y)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(false));
    }

    public static RenderLayer getLeash() {
        return LEASH;
    }

    public static RenderLayer getWaterMask() {
        return WATER_MASK;
    }

    public static RenderLayer getOutline(Identifier texture) {
        return MultiPhase.CULLING_LAYERS.apply(texture, false);
    }

    public static RenderLayer getArmorEntityGlint() {
        return ARMOR_ENTITY_GLINT;
    }

    public static RenderLayer getGlintTranslucent() {
        return GLINT_TRANSLUCENT;
    }

    public static RenderLayer getGlint() {
        return GLINT;
    }

    public static RenderLayer getEntityGlint() {
        return ENTITY_GLINT;
    }

    public static RenderLayer getBlockBreaking(Identifier texture) {
        return CRUMBLING.apply(texture);
    }

    public static RenderLayer getText(Identifier texture) {
        return TEXT.apply(texture);
    }

    public static RenderLayer getTextBackground() {
        return TEXT_BACKGROUND;
    }

    public static RenderLayer getTextIntensity(Identifier texture) {
        return TEXT_INTENSITY.apply(texture);
    }

    public static RenderLayer getTextPolygonOffset(Identifier texture) {
        return TEXT_POLYGON_OFFSET.apply(texture);
    }

    public static RenderLayer getTextIntensityPolygonOffset(Identifier texture) {
        return TEXT_INTENSITY_POLYGON_OFFSET.apply(texture);
    }

    public static RenderLayer getTextSeeThrough(Identifier texture) {
        return TEXT_SEE_THROUGH.apply(texture);
    }

    public static RenderLayer getTextBackgroundSeeThrough() {
        return TEXT_BACKGROUND_SEE_THROUGH;
    }

    public static RenderLayer getTextIntensitySeeThrough(Identifier texture) {
        return TEXT_INTENSITY_SEE_THROUGH.apply(texture);
    }

    public static RenderLayer getLightning() {
        return LIGHTNING;
    }

    public static RenderLayer getDragonRays() {
        return DRAGON_RAYS;
    }

    public static RenderLayer getDragonRaysDepth() {
        return DRAGON_RAYS_DEPTH;
    }

    public static RenderLayer getTripwire() {
        return TRIPWIRE;
    }

    public static RenderLayer getEndPortal() {
        return END_PORTAL;
    }

    public static RenderLayer getEndGateway() {
        return END_GATEWAY;
    }

    public static RenderLayer getLines() {
        return LINES;
    }

    public static RenderLayer getSecondaryBlockOutline() {
        return SECONDARY_BLOCK_OUTLINE;
    }

    public static RenderLayer getLineStrip() {
        return LINE_STRIP;
    }

    public static RenderLayer getDebugLineStrip(double lineWidth) {
        return DEBUG_LINE_STRIP.apply(lineWidth);
    }

    public static RenderLayer getDebugFilledBox() {
        return DEBUG_FILLED_BOX;
    }

    public static RenderLayer getDebugQuads() {
        return DEBUG_QUADS;
    }

    public static RenderLayer getDebugTriangleFan() {
        return DEBUG_TRIANGLE_FAN;
    }

    public static RenderLayer getDebugStructureQuads() {
        return DEBUG_STRUCTURE_QUADS;
    }

    public static RenderLayer getDebugSectionQuads() {
        return DEBUG_SECTION_QUADS;
    }

    private static Function<Identifier, RenderLayer> createWeather(RenderPipeline pipeline) {
        return Util.memoize(texture -> RenderLayer.of("weather", 1536, false, false, pipeline, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).target(WEATHER_TARGET).lightmap(ENABLE_LIGHTMAP).build(false)));
    }

    public static RenderLayer getWeather(Identifier texture, boolean allMask) {
        return (allMask ? WEATHER_ALL_MASK : WEATHER_COLOR_MASK).apply(texture);
    }

    public static RenderLayer getBlockScreenEffect(Identifier texture) {
        return BLOCK_SCREEN_EFFECT.apply(texture);
    }

    public static RenderLayer getFireScreenEffect(Identifier texture) {
        return FIRE_SCREEN_EFFECT.apply(texture);
    }

    public RenderLayer(String name, int size, boolean hasCrumbling, boolean translucent, Runnable begin, Runnable end) {
        super(name, begin, end);
        this.expectedBufferSize = size;
        this.hasCrumbling = hasCrumbling;
        this.translucent = translucent;
    }

    static MultiPhase of(String name, int size, RenderPipeline pipeline, MultiPhaseParameters params) {
        return RenderLayer.of(name, size, false, false, pipeline, params);
    }

    private static MultiPhase of(String name, int size, boolean hasCrumbling, boolean translucent, RenderPipeline pipeline, MultiPhaseParameters params) {
        return new MultiPhase(name, size, hasCrumbling, translucent, pipeline, params);
    }

    public abstract void draw(BuiltBuffer var1);

    public int getExpectedBufferSize() {
        return this.expectedBufferSize;
    }

    public abstract VertexFormat getVertexFormat();

    public abstract VertexFormat.DrawMode getDrawMode();

    public Optional<RenderLayer> getAffectedOutline() {
        return Optional.empty();
    }

    public boolean isOutline() {
        return false;
    }

    public abstract RenderPipeline getRenderPipeline();

    public boolean hasCrumbling() {
        return this.hasCrumbling;
    }

    public boolean areVerticesNotShared() {
        return !this.getDrawMode().shareVertices;
    }

    public boolean isTranslucent() {
        return this.translucent;
    }

    @Environment(value=EnvType.CLIENT)
    protected static final class MultiPhaseParameters {
        final RenderPhase.TextureBase texture;
        final RenderPhase.Target target;
        final OutlineMode outlineMode;
        final ImmutableList<RenderPhase> phases;

        MultiPhaseParameters(RenderPhase.TextureBase texture, RenderPhase.Lightmap lightMap, RenderPhase.Overlay overlay, RenderPhase.Layering layering, RenderPhase.Target target, RenderPhase.Texturing texturing, RenderPhase.LineWidth lineWidth, OutlineMode outlineMode) {
            this.texture = texture;
            this.target = target;
            this.outlineMode = outlineMode;
            this.phases = ImmutableList.of(texture, lightMap, overlay, layering, target, texturing, lineWidth);
        }

        public String toString() {
            return "CompositeState[" + String.valueOf(this.phases) + ", outlineProperty=" + String.valueOf((Object)this.outlineMode) + "]";
        }

        public static Builder builder() {
            return new Builder();
        }

        @Environment(value=EnvType.CLIENT)
        public static class Builder {
            private RenderPhase.TextureBase texture = RenderPhase.NO_TEXTURE;
            private RenderPhase.Lightmap lightmap = RenderPhase.DISABLE_LIGHTMAP;
            private RenderPhase.Overlay overlay = RenderPhase.DISABLE_OVERLAY_COLOR;
            private RenderPhase.Layering layering = RenderPhase.NO_LAYERING;
            private RenderPhase.Target target = RenderPhase.MAIN_TARGET;
            private RenderPhase.Texturing texturing = RenderPhase.DEFAULT_TEXTURING;
            private RenderPhase.LineWidth lineWidth = RenderPhase.FULL_LINE_WIDTH;

            Builder() {
            }

            protected Builder texture(RenderPhase.TextureBase texture) {
                this.texture = texture;
                return this;
            }

            protected Builder lightmap(RenderPhase.Lightmap lightmap) {
                this.lightmap = lightmap;
                return this;
            }

            protected Builder overlay(RenderPhase.Overlay overlay) {
                this.overlay = overlay;
                return this;
            }

            protected Builder layering(RenderPhase.Layering layering) {
                this.layering = layering;
                return this;
            }

            protected Builder target(RenderPhase.Target target) {
                this.target = target;
                return this;
            }

            protected Builder texturing(RenderPhase.Texturing texturing) {
                this.texturing = texturing;
                return this;
            }

            protected Builder lineWidth(RenderPhase.LineWidth lineWidth) {
                this.lineWidth = lineWidth;
                return this;
            }

            protected MultiPhaseParameters build(boolean affectsOutline) {
                return this.build(affectsOutline ? OutlineMode.AFFECTS_OUTLINE : OutlineMode.NONE);
            }

            protected MultiPhaseParameters build(OutlineMode outlineMode) {
                return new MultiPhaseParameters(this.texture, this.lightmap, this.overlay, this.layering, this.target, this.texturing, this.lineWidth, outlineMode);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class MultiPhase
    extends RenderLayer {
        static final BiFunction<Identifier, Boolean, RenderLayer> CULLING_LAYERS = Util.memoize((texture, hasCulling) -> RenderLayer.of("outline", 1536, hasCulling != false ? RenderPipelines.OUTLINE_CULL : RenderPipelines.OUTLINE_NO_CULL, MultiPhaseParameters.builder().texture(new RenderPhase.Texture((Identifier)texture, false)).target(OUTLINE_TARGET).build(OutlineMode.IS_OUTLINE)));
        private final MultiPhaseParameters phases;
        private final RenderPipeline pipeline;
        private final Optional<RenderLayer> affectedOutline;
        private final boolean outline;

        MultiPhase(String name, int size, boolean hasCrumbling, boolean translucent, RenderPipeline pipeline, MultiPhaseParameters phases) {
            super(name, size, hasCrumbling, translucent, () -> arg.phases.forEach(RenderPhase::startDrawing), () -> arg.phases.forEach(RenderPhase::endDrawing));
            this.phases = phases;
            this.pipeline = pipeline;
            this.affectedOutline = phases.outlineMode == OutlineMode.AFFECTS_OUTLINE ? phases.texture.getId().map(id -> CULLING_LAYERS.apply((Identifier)id, pipeline.isCull())) : Optional.empty();
            this.outline = phases.outlineMode == OutlineMode.IS_OUTLINE;
        }

        @Override
        public Optional<RenderLayer> getAffectedOutline() {
            return this.affectedOutline;
        }

        @Override
        public boolean isOutline() {
            return this.outline;
        }

        @Override
        public VertexFormat getVertexFormat() {
            return this.pipeline.getVertexFormat();
        }

        @Override
        public VertexFormat.DrawMode getDrawMode() {
            return this.pipeline.getVertexFormatMode();
        }

        @Override
        public RenderPipeline getRenderPipeline() {
            return this.pipeline;
        }

        @Override
        public void draw(BuiltBuffer buffer) {
            this.startDrawing();
            GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(), RenderSystem.getTextureMatrix(), RenderSystem.getShaderLineWidth());
            try (BuiltBuffer builtBuffer = buffer;){
                GpuTextureView gpuTextureView;
                VertexFormat.IndexType lv2;
                GpuBuffer gpuBuffer2;
                GpuBuffer gpuBuffer = this.pipeline.getVertexFormat().uploadImmediateVertexBuffer(buffer.getBuffer());
                if (buffer.getSortedBuffer() == null) {
                    RenderSystem.ShapeIndexBuffer lv = RenderSystem.getSequentialBuffer(buffer.getDrawParameters().mode());
                    gpuBuffer2 = lv.getIndexBuffer(buffer.getDrawParameters().indexCount());
                    lv2 = lv.getIndexType();
                } else {
                    gpuBuffer2 = this.pipeline.getVertexFormat().uploadImmediateIndexBuffer(buffer.getSortedBuffer());
                    lv2 = buffer.getDrawParameters().indexType();
                }
                Framebuffer lv3 = this.phases.target.get();
                GpuTextureView gpuTextureView2 = gpuTextureView = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : lv3.getColorAttachmentView();
                GpuTextureView gpuTextureView22 = lv3.useDepthAttachment ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : lv3.getDepthAttachmentView()) : null;
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Immediate draw for " + this.getName(), gpuTextureView, OptionalInt.empty(), gpuTextureView22, OptionalDouble.empty());){
                    renderPass.setPipeline(this.pipeline);
                    ScissorState lv4 = RenderSystem.getScissorStateForRenderTypeDraws();
                    if (lv4.isEnabled()) {
                        renderPass.enableScissor(lv4.getX(), lv4.getY(), lv4.getWidth(), lv4.getHeight());
                    }
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                    renderPass.setVertexBuffer(0, gpuBuffer);
                    for (int i = 0; i < 12; ++i) {
                        GpuTextureView gpuTextureView3 = RenderSystem.getShaderTexture(i);
                        if (gpuTextureView3 == null) continue;
                        renderPass.bindSampler("Sampler" + i, gpuTextureView3);
                    }
                    renderPass.setIndexBuffer(gpuBuffer2, lv2);
                    renderPass.drawIndexed(0, 0, buffer.getDrawParameters().indexCount(), 1);
                }
            }
            this.endDrawing();
        }

        @Override
        public String toString() {
            return "RenderType[" + this.name + ":" + String.valueOf(this.phases) + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum OutlineMode {
        NONE("none"),
        IS_OUTLINE("is_outline"),
        AFFECTS_OUTLINE("affects_outline");

        private final String name;

        private OutlineMode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

