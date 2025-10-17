/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.annotation.DeobfuscateClass;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
@DeobfuscateClass
public class TextureUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MIN_MIPMAP_LEVEL = 0;
    private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;

    public static ByteBuffer readResource(InputStream inputStream) throws IOException {
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        if (readableByteChannel instanceof SeekableByteChannel) {
            SeekableByteChannel seekableByteChannel = (SeekableByteChannel)readableByteChannel;
            return TextureUtil.readResource(readableByteChannel, (int)seekableByteChannel.size() + 1);
        }
        return TextureUtil.readResource(readableByteChannel, 8192);
    }

    private static ByteBuffer readResource(ReadableByteChannel channel, int bufSize) throws IOException {
        ByteBuffer byteBuffer = MemoryUtil.memAlloc(bufSize);
        try {
            while (channel.read(byteBuffer) != -1) {
                if (byteBuffer.hasRemaining()) continue;
                byteBuffer = MemoryUtil.memRealloc(byteBuffer, byteBuffer.capacity() * 2);
            }
            return byteBuffer;
        } catch (IOException iOException) {
            MemoryUtil.memFree(byteBuffer);
            throw iOException;
        }
    }

    public static void writeAsPNG(Path directory, String prefix, GpuTexture texture, int scales, IntUnaryOperator colorFunction) {
        RenderSystem.assertOnRenderThread();
        int j = 0;
        for (int k = 0; k <= scales; ++k) {
            j += texture.getFormat().pixelSize() * texture.getWidth(k) * texture.getHeight(k);
        }
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Texture output buffer", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, j);
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        Runnable runnable = () -> {
            try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(gpuBuffer, true, false);){
                int j = 0;
                for (int k = 0; k <= scales; ++k) {
                    int l = texture.getWidth(k);
                    int m = texture.getHeight(k);
                    try (NativeImage lv = new NativeImage(l, m, false);){
                        for (int n = 0; n < m; ++n) {
                            for (int o = 0; o < l; ++o) {
                                int p = mappedView.data().getInt(j + (o + n * l) * texture.getFormat().pixelSize());
                                lv.setColor(o, n, colorFunction.applyAsInt(p));
                            }
                        }
                        Path path2 = directory.resolve(prefix + "_" + k + ".png");
                        lv.writeTo(path2);
                        LOGGER.debug("Exported png to: {}", (Object)path2.toAbsolutePath());
                    } catch (IOException iOException) {
                        LOGGER.debug("Unable to write: ", iOException);
                    }
                    j += texture.getFormat().pixelSize() * l * m;
                }
            }
            gpuBuffer.close();
        };
        AtomicInteger atomicInteger = new AtomicInteger();
        int l = 0;
        for (int m = 0; m <= scales; ++m) {
            commandEncoder.copyTextureToBuffer(texture, gpuBuffer, l, () -> {
                if (atomicInteger.getAndIncrement() == scales) {
                    runnable.run();
                }
            }, m);
            l += texture.getFormat().pixelSize() * texture.getWidth(m) * texture.getHeight(m);
        }
    }

    public static Path getDebugTexturePath(Path path) {
        return path.resolve("screenshots").resolve("debug");
    }

    public static Path getDebugTexturePath() {
        return TextureUtil.getDebugTexturePath(Path.of(".", new String[0]));
    }
}

