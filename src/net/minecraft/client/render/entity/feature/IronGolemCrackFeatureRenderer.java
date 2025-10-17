/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.client.render.entity.state.IronGolemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.Cracks;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class IronGolemCrackFeatureRenderer
extends FeatureRenderer<IronGolemEntityRenderState, IronGolemEntityModel> {
    private static final Map<Cracks.CrackLevel, Identifier> CRACK_TEXTURES = ImmutableMap.of(Cracks.CrackLevel.LOW, Identifier.ofVanilla("textures/entity/iron_golem/iron_golem_crackiness_low.png"), Cracks.CrackLevel.MEDIUM, Identifier.ofVanilla("textures/entity/iron_golem/iron_golem_crackiness_medium.png"), Cracks.CrackLevel.HIGH, Identifier.ofVanilla("textures/entity/iron_golem/iron_golem_crackiness_high.png"));

    public IronGolemCrackFeatureRenderer(FeatureRendererContext<IronGolemEntityRenderState, IronGolemEntityModel> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, IronGolemEntityRenderState arg3, float f, float g) {
        if (arg3.invisible) {
            return;
        }
        Cracks.CrackLevel lv = arg3.crackLevel;
        if (lv == Cracks.CrackLevel.NONE) {
            return;
        }
        Identifier lv2 = CRACK_TEXTURES.get((Object)lv);
        IronGolemCrackFeatureRenderer.renderModel(this.getContextModel(), lv2, arg, arg2, i, arg3, -1, 1);
    }
}

