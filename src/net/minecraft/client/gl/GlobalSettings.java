/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderTickCounter;
import org.lwjgl.system.MemoryStack;

@Environment(value=EnvType.CLIENT)
public class GlobalSettings
implements AutoCloseable {
    public static final int SIZE = new Std140SizeCalculator().putVec2().putFloat().putFloat().putInt().get();
    private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Global Settings UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, SIZE);

    public void set(int width, int height, double glintStrength, long time, RenderTickCounter tickCounter, int menuBackgroundBlurriness) {
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, SIZE).putVec2(width, height).putFloat((float)glintStrength).putFloat(((float)(time % 24000L) + tickCounter.getTickProgress(false)) / 24000.0f).putInt(menuBackgroundBlurriness).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(), byteBuffer);
        }
        RenderSystem.setGlobalSettingsUniform(this.buffer);
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}

