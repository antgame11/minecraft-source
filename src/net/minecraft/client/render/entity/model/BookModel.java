/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

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
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BookModel
extends Model<BookModelState> {
    private static final String LEFT_PAGES = "left_pages";
    private static final String RIGHT_PAGES = "right_pages";
    private static final String FLIP_PAGE1 = "flip_page1";
    private static final String FLIP_PAGE2 = "flip_page2";
    private final ModelPart leftCover;
    private final ModelPart rightCover;
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart leftFlippingPage;
    private final ModelPart rightFlippingPage;

    public BookModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);
        this.leftCover = root.getChild(EntityModelPartNames.LEFT_LID);
        this.rightCover = root.getChild(EntityModelPartNames.RIGHT_LID);
        this.leftPages = root.getChild(LEFT_PAGES);
        this.rightPages = root.getChild(RIGHT_PAGES);
        this.leftFlippingPage = root.getChild(FLIP_PAGE1);
        this.rightFlippingPage = root.getChild(FLIP_PAGE2);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.LEFT_LID, ModelPartBuilder.create().uv(0, 0).cuboid(-6.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), ModelTransform.origin(0.0f, 0.0f, -1.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LID, ModelPartBuilder.create().uv(16, 0).cuboid(0.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), ModelTransform.origin(0.0f, 0.0f, 1.0f));
        lv2.addChild("seam", ModelPartBuilder.create().uv(12, 0).cuboid(-1.0f, -5.0f, 0.0f, 2.0f, 10.0f, 0.005f), ModelTransform.rotation(0.0f, 1.5707964f, 0.0f));
        lv2.addChild(LEFT_PAGES, ModelPartBuilder.create().uv(0, 10).cuboid(0.0f, -4.0f, -0.99f, 5.0f, 8.0f, 1.0f), ModelTransform.NONE);
        lv2.addChild(RIGHT_PAGES, ModelPartBuilder.create().uv(12, 10).cuboid(0.0f, -4.0f, -0.01f, 5.0f, 8.0f, 1.0f), ModelTransform.NONE);
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(24, 10).cuboid(0.0f, -4.0f, 0.0f, 5.0f, 8.0f, 0.005f);
        lv2.addChild(FLIP_PAGE1, lv3, ModelTransform.NONE);
        lv2.addChild(FLIP_PAGE2, lv3, ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void setAngles(BookModelState arg) {
        super.setAngles(arg);
        float f = (MathHelper.sin(arg.pageTurnAmount * 0.02f) * 0.1f + 1.25f) * arg.pageTurnSpeed;
        this.leftCover.yaw = (float)Math.PI + f;
        this.rightCover.yaw = -f;
        this.leftPages.yaw = f;
        this.rightPages.yaw = -f;
        this.leftFlippingPage.yaw = f - f * 2.0f * arg.leftFlipAmount;
        this.rightFlippingPage.yaw = f - f * 2.0f * arg.rightFlipAmount;
        this.leftPages.originX = MathHelper.sin(f);
        this.rightPages.originX = MathHelper.sin(f);
        this.leftFlippingPage.originX = MathHelper.sin(f);
        this.rightFlippingPage.originX = MathHelper.sin(f);
    }

    @Environment(value=EnvType.CLIENT)
    public record BookModelState(float pageTurnAmount, float leftFlipAmount, float rightFlipAmount, float pageTurnSpeed) {
    }
}

