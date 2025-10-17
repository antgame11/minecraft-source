/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgram;

@Environment(value=EnvType.CLIENT)
public record CompiledShaderPipeline(RenderPipeline info, ShaderProgram program) implements CompiledRenderPipeline
{
    @Override
    public boolean isValid() {
        return this.program != ShaderProgram.INVALID;
    }
}

