/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public interface SpriteGlyph
extends TextDrawable {
    @Override
    default public void render(Matrix4f matrix4f, VertexConsumer consumer, int light, boolean noDepth) {
        float f = 0.0f;
        if (this.shadowColor() != 0) {
            this.draw(matrix4f, consumer, light, this.x() + this.shadowOffset(), this.y() + this.shadowOffset(), 0.0f, this.shadowColor());
            if (!noDepth) {
                f += 0.03f;
            }
        }
        this.draw(matrix4f, consumer, light, this.x(), this.y(), f, this.color());
    }

    public void draw(Matrix4f var1, VertexConsumer var2, int var3, float var4, float var5, float var6, int var7);

    public float x();

    public float y();

    public int color();

    public int shadowColor();

    public float shadowOffset();
}

