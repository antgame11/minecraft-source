/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BellBlockModel
extends Model<BellModelState> {
    private static final String BELL_BODY = "bell_body";
    private final ModelPart bellBody;

    public BellBlockModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);
        this.bellBody = root.getChild(BELL_BODY);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(BELL_BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -6.0f, -3.0f, 6.0f, 7.0f, 6.0f), ModelTransform.origin(8.0f, 12.0f, 8.0f));
        lv3.addChild("bell_base", ModelPartBuilder.create().uv(0, 13).cuboid(4.0f, 4.0f, 4.0f, 8.0f, 2.0f, 8.0f), ModelTransform.origin(-8.0f, -12.0f, -8.0f));
        return TexturedModelData.of(lv, 32, 32);
    }

    @Override
    public void setAngles(BellModelState arg) {
        super.setAngles(arg);
        float f = 0.0f;
        float g = 0.0f;
        if (arg.shakeDirection != null) {
            float h = MathHelper.sin(arg.ticks / (float)Math.PI) / (4.0f + arg.ticks / 3.0f);
            switch (arg.shakeDirection) {
                case NORTH: {
                    f = -h;
                    break;
                }
                case SOUTH: {
                    f = h;
                    break;
                }
                case EAST: {
                    g = -h;
                    break;
                }
                case WEST: {
                    g = h;
                }
            }
        }
        this.bellBody.pitch = f;
        this.bellBody.roll = g;
    }

    @Environment(value=EnvType.CLIENT)
    public record BellModelState(float ticks, @Nullable Direction shakeDirection) {
        @Nullable
        public Direction shakeDirection() {
            return this.shakeDirection;
        }
    }
}

