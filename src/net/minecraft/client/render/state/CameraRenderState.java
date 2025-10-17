/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class CameraRenderState {
    public BlockPos blockPos = BlockPos.ORIGIN;
    public Vec3d pos = new Vec3d(0.0, 0.0, 0.0);
    public boolean initialized;
    public Vec3d entityPos = new Vec3d(0.0, 0.0, 0.0);
    public Quaternionf orientation = new Quaternionf();
}

