/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.resource.metadata.AnimationFrameResourceMetadata;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Animator;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SpriteContents
implements TextureStitcher.Stitchable,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    final Identifier id;
    final int width;
    final int height;
    private final NativeImage image;
    NativeImage[] mipmapLevelsImages;
    @Nullable
    private final Animation animation;
    private final List<ResourceMetadataSerializer.Value<?>> additionalMetadata;

    public SpriteContents(Identifier id, SpriteDimensions dimensions, NativeImage image) {
        this(id, dimensions, image, Optional.empty(), List.of());
    }

    public SpriteContents(Identifier id, SpriteDimensions dimensions, NativeImage image, Optional<AnimationResourceMetadata> animationResourceMetadata, List<ResourceMetadataSerializer.Value<?>> additionalMetadata) {
        this.id = id;
        this.width = dimensions.width();
        this.height = dimensions.height();
        this.additionalMetadata = additionalMetadata;
        this.animation = animationResourceMetadata.map(animationMetadata -> this.createAnimation(dimensions, image.getWidth(), image.getHeight(), (AnimationResourceMetadata)animationMetadata)).orElse(null);
        this.image = image;
        this.mipmapLevelsImages = new NativeImage[]{this.image};
    }

    public void generateMipmaps(int mipmapLevels) {
        try {
            this.mipmapLevelsImages = MipmapHelper.getMipmapLevelsImages(this.mipmapLevelsImages, mipmapLevels);
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Generating mipmaps for frame");
            CrashReportSection lv2 = lv.addElement("Frame being iterated");
            lv2.add("Sprite name", this.id);
            lv2.add("Sprite size", () -> this.width + " x " + this.height);
            lv2.add("Sprite frames", () -> this.getFrameCount() + " frames");
            lv2.add("Mipmap levels", mipmapLevels);
            lv2.add("Original image size", () -> this.image.getWidth() + "x" + this.image.getHeight());
            throw new CrashException(lv);
        }
    }

    private int getFrameCount() {
        return this.animation != null ? this.animation.frames.size() : 1;
    }

    public boolean isAnimated() {
        return this.getFrameCount() > 1;
    }

    @Nullable
    private Animation createAnimation(SpriteDimensions dimensions, int imageWidth, int imageHeight, AnimationResourceMetadata metadata) {
        ArrayList<AnimationFrame> list;
        int k = imageWidth / dimensions.width();
        int l = imageHeight / dimensions.height();
        int m = k * l;
        int n = metadata.defaultFrameTime();
        if (metadata.frames().isEmpty()) {
            list = new ArrayList<AnimationFrame>(m);
            for (int o = 0; o < m; ++o) {
                list.add(new AnimationFrame(o, n));
            }
        } else {
            List<AnimationFrameResourceMetadata> list2 = metadata.frames().get();
            list = new ArrayList(list2.size());
            for (AnimationFrameResourceMetadata lv : list2) {
                list.add(new AnimationFrame(lv.index(), lv.getTime(n)));
            }
            int p = 0;
            IntOpenHashSet intSet = new IntOpenHashSet();
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                AnimationFrame lv2 = (AnimationFrame)iterator.next();
                boolean bl = true;
                if (lv2.time <= 0) {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", this.id, p, lv2.time);
                    bl = false;
                }
                if (lv2.index < 0 || lv2.index >= m) {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", this.id, p, lv2.index);
                    bl = false;
                }
                if (bl) {
                    intSet.add(lv2.index);
                } else {
                    iterator.remove();
                }
                ++p;
            }
            int[] is = IntStream.range(0, m).filter(i -> !intSet.contains(i)).toArray();
            if (is.length > 0) {
                LOGGER.warn("Unused frames in sprite {}: {}", (Object)this.id, (Object)Arrays.toString(is));
            }
        }
        if (list.size() <= 1) {
            return null;
        }
        return new Animation(List.copyOf(list), k, metadata.interpolate());
    }

    void upload(int x, int y, int unpackSkipPixels, int unpackSkipRows, NativeImage[] images, GpuTexture texture) {
        for (int m = 0; m < this.mipmapLevelsImages.length; ++m) {
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, images[m], m, 0, x >> m, y >> m, this.width >> m, this.height >> m, unpackSkipPixels >> m, unpackSkipRows >> m);
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    public IntStream getDistinctFrameCount() {
        return this.animation != null ? this.animation.getDistinctFrameCount() : IntStream.of(1);
    }

    @Nullable
    public Animator createAnimator() {
        return this.animation != null ? this.animation.createAnimator() : null;
    }

    public <T> Optional<T> getAdditionalMetadataValue(ResourceMetadataSerializer<T> serializer) {
        for (ResourceMetadataSerializer.Value<?> lv : this.additionalMetadata) {
            Optional<T> optional = lv.getValueIfMatching(serializer);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public void close() {
        for (NativeImage lv : this.mipmapLevelsImages) {
            lv.close();
        }
    }

    public String toString() {
        return "SpriteContents{name=" + String.valueOf(this.id) + ", frameCount=" + this.getFrameCount() + ", height=" + this.height + ", width=" + this.width + "}";
    }

    public boolean isPixelTransparent(int frame, int x, int y) {
        int l = x;
        int m = y;
        if (this.animation != null) {
            l += this.animation.getFrameX(frame) * this.width;
            m += this.animation.getFrameY(frame) * this.height;
        }
        return ColorHelper.getAlpha(this.image.getColorArgb(l, m)) == 0;
    }

    public void upload(int x, int y, GpuTexture texture) {
        if (this.animation != null) {
            this.animation.upload(x, y, texture);
        } else {
            this.upload(x, y, 0, 0, this.mipmapLevelsImages, texture);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class Animation {
        final List<AnimationFrame> frames;
        private final int frameCount;
        private final boolean interpolation;

        Animation(List<AnimationFrame> frames, int frameCount, boolean interpolation) {
            this.frames = frames;
            this.frameCount = frameCount;
            this.interpolation = interpolation;
        }

        int getFrameX(int frame) {
            return frame % this.frameCount;
        }

        int getFrameY(int frame) {
            return frame / this.frameCount;
        }

        void upload(int x, int y, int frame, GpuTexture texture) {
            int l = this.getFrameX(frame) * SpriteContents.this.width;
            int m = this.getFrameY(frame) * SpriteContents.this.height;
            SpriteContents.this.upload(x, y, l, m, SpriteContents.this.mipmapLevelsImages, texture);
        }

        public Animator createAnimator() {
            return new AnimatorImpl(SpriteContents.this, this, this.interpolation ? new Interpolation() : null);
        }

        public void upload(int x, int y, GpuTexture texture) {
            this.upload(x, y, this.frames.get((int)0).index, texture);
        }

        public IntStream getDistinctFrameCount() {
            return this.frames.stream().mapToInt(frame -> frame.index).distinct();
        }
    }

    @Environment(value=EnvType.CLIENT)
    record AnimationFrame(int index, int time) {
    }

    @Environment(value=EnvType.CLIENT)
    class AnimatorImpl
    implements Animator {
        int frame;
        int currentTime;
        final Animation animation;
        @Nullable
        private final Interpolation interpolation;

        AnimatorImpl(SpriteContents arg, @Nullable Animation animation, Interpolation interpolation) {
            this.animation = animation;
            this.interpolation = interpolation;
        }

        @Override
        public void tick(int x, int y, GpuTexture texture) {
            ++this.currentTime;
            AnimationFrame lv = this.animation.frames.get(this.frame);
            if (this.currentTime >= lv.time) {
                int k = lv.index;
                this.frame = (this.frame + 1) % this.animation.frames.size();
                this.currentTime = 0;
                int l = this.animation.frames.get((int)this.frame).index;
                if (k != l) {
                    this.animation.upload(x, y, l, texture);
                }
            } else if (this.interpolation != null) {
                this.interpolation.method_24128(x, y, this, texture);
            }
        }

        @Override
        public void close() {
            if (this.interpolation != null) {
                this.interpolation.close();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    final class Interpolation
    implements AutoCloseable {
        private final NativeImage[] images;

        Interpolation() {
            this.images = new NativeImage[SpriteContents.this.mipmapLevelsImages.length];
            for (int i = 0; i < this.images.length; ++i) {
                int j = SpriteContents.this.width >> i;
                int k = SpriteContents.this.height >> i;
                this.images[i] = new NativeImage(j, k, false);
            }
        }

        void method_24128(int i, int j, AnimatorImpl arg, GpuTexture gpuTexture) {
            Animation lv = arg.animation;
            List<AnimationFrame> list = lv.frames;
            AnimationFrame lv2 = list.get(arg.frame);
            float f = (float)arg.currentTime / (float)lv2.time;
            int k = lv2.index;
            int l = list.get((int)((arg.frame + 1) % list.size())).index;
            if (k != l) {
                int o;
                for (int m = 0; m < this.images.length; ++m) {
                    int n = SpriteContents.this.width >> m;
                    o = SpriteContents.this.height >> m;
                    for (int p = 0; p < o; ++p) {
                        for (int q = 0; q < n; ++q) {
                            int r = this.getPixelColor(lv, k, m, q, p);
                            int s = this.getPixelColor(lv, l, m, q, p);
                            this.images[m].setColorArgb(q, p, ColorHelper.lerp(f, r, s));
                        }
                    }
                }
                SpriteContents.this.upload(i, j, 0, 0, this.images, gpuTexture);
                if (SharedConstants.DUMP_INTERPOLATED_TEXTURE_FRAMES) {
                    try {
                        Path path = TextureUtil.getDebugTexturePath();
                        Path path2 = path.resolve(SpriteContents.this.id.toUnderscoreSeparatedString());
                        Files.createDirectories(path2, new FileAttribute[0]);
                        for (o = 0; o < this.images.length; ++o) {
                            this.images[o].writeTo(path2.resolve(SpriteContents.this.id.toUnderscoreSeparatedString() + "_" + o + "_" + k + "_" + l + ".png"));
                        }
                    } catch (IOException iOException) {
                        // empty catch block
                    }
                }
            }
        }

        private int getPixelColor(Animation animation, int frameIndex, int layer, int x, int y) {
            return SpriteContents.this.mipmapLevelsImages[layer].getColorArgb(x + (animation.getFrameX(frameIndex) * SpriteContents.this.width >> layer), y + (animation.getFrameY(frameIndex) * SpriteContents.this.height >> layer));
        }

        @Override
        public void close() {
            for (NativeImage lv : this.images) {
                lv.close();
            }
        }
    }
}

