/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.DynamicTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SpriteAtlasTexture
extends AbstractTexture
implements DynamicTexture,
TextureTickListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Deprecated
    public static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.ofVanilla("textures/atlas/blocks.png");
    @Deprecated
    public static final Identifier PARTICLE_ATLAS_TEXTURE = Identifier.ofVanilla("textures/atlas/particles.png");
    private List<SpriteContents> spritesToLoad = List.of();
    private List<Sprite.TickableAnimation> animatedSprites = List.of();
    private Map<Identifier, Sprite> sprites = Map.of();
    @Nullable
    private Sprite missingSprite;
    private final Identifier id;
    private final int maxTextureSize;
    private int width;
    private int height;
    private int mipLevel;

    public SpriteAtlasTexture(Identifier id) {
        this.id = id;
        this.maxTextureSize = RenderSystem.getDevice().getMaxTextureSize();
    }

    private void createTexture(int width, int height, int mipLevel) {
        LOGGER.info("Created: {}x{}x{} {}-atlas", width, height, mipLevel, this.id);
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.close();
        this.glTexture = gpuDevice.createTexture(this.id::toString, 7, TextureFormat.RGBA8, width, height, 1, mipLevel + 1);
        this.glTextureView = gpuDevice.createTextureView(this.glTexture);
        this.width = width;
        this.height = height;
        this.mipLevel = mipLevel;
    }

    public void upload(SpriteLoader.StitchResult stitchResult) {
        this.createTexture(stitchResult.width(), stitchResult.height(), stitchResult.mipLevel());
        this.clear();
        this.setFilter(false, this.mipLevel > 1);
        this.sprites = Map.copyOf(stitchResult.sprites());
        this.missingSprite = this.sprites.get(MissingSprite.getMissingSpriteId());
        if (this.missingSprite == null) {
            throw new IllegalStateException("Atlas '" + String.valueOf(this.id) + "' (" + this.sprites.size() + " sprites) has no missing texture sprite");
        }
        ArrayList<SpriteContents> list = new ArrayList<SpriteContents>();
        ArrayList<Sprite.TickableAnimation> list2 = new ArrayList<Sprite.TickableAnimation>();
        for (Sprite lv : stitchResult.sprites().values()) {
            list.add(lv.getContents());
            try {
                lv.upload(this.glTexture);
            } catch (Throwable throwable) {
                CrashReport lv2 = CrashReport.create(throwable, "Stitching texture atlas");
                CrashReportSection lv3 = lv2.addElement("Texture being stitched together");
                lv3.add("Atlas path", this.id);
                lv3.add("Sprite", lv);
                throw new CrashException(lv2);
            }
            Sprite.TickableAnimation lv4 = lv.createAnimation();
            if (lv4 == null) continue;
            list2.add(lv4);
        }
        this.spritesToLoad = List.copyOf(list);
        this.animatedSprites = List.copyOf(list2);
        if (SharedConstants.DUMP_TEXTURE_ATLAS) {
            Path path = TextureUtil.getDebugTexturePath();
            try {
                Files.createDirectories(path, new FileAttribute[0]);
                this.save(this.id, path);
            } catch (IOException iOException) {
                LOGGER.warn("Failed to dump atlas contents to {}", (Object)path);
            }
        }
    }

    @Override
    public void save(Identifier id, Path path) throws IOException {
        String string = id.toUnderscoreSeparatedString();
        TextureUtil.writeAsPNG(path, string, this.getGlTexture(), this.mipLevel, color -> color);
        SpriteAtlasTexture.dumpAtlasInfos(path, string, this.sprites);
    }

    private static void dumpAtlasInfos(Path path, String id, Map<Identifier, Sprite> sprites) {
        Path path2 = path.resolve(id + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path2, new OpenOption[0]);){
            for (Map.Entry entry : sprites.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                Sprite lv = (Sprite)entry.getValue();
                writer.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", entry.getKey(), lv.getX(), lv.getY(), lv.getContents().getWidth(), lv.getContents().getHeight()));
            }
        } catch (IOException iOException) {
            LOGGER.warn("Failed to write file {}", (Object)path2, (Object)iOException);
        }
    }

    public void tickAnimatedSprites() {
        if (this.glTexture == null) {
            return;
        }
        for (Sprite.TickableAnimation lv : this.animatedSprites) {
            lv.tick(this.glTexture);
        }
    }

    @Override
    public void tick() {
        this.tickAnimatedSprites();
    }

    public Sprite getSprite(Identifier id) {
        Sprite lv = this.sprites.getOrDefault(id, this.missingSprite);
        if (lv == null) {
            throw new IllegalStateException("Tried to lookup sprite, but atlas is not initialized");
        }
        return lv;
    }

    public Sprite getMissingSprite() {
        return Objects.requireNonNull(this.missingSprite, "Atlas not initialized");
    }

    public void clear() {
        this.spritesToLoad.forEach(SpriteContents::close);
        this.animatedSprites.forEach(Sprite.TickableAnimation::close);
        this.spritesToLoad = List.of();
        this.animatedSprites = List.of();
        this.sprites = Map.of();
        this.missingSprite = null;
    }

    public Identifier getId() {
        return this.id;
    }

    public int getMaxTextureSize() {
        return this.maxTextureSize;
    }

    int getWidth() {
        return this.width;
    }

    int getHeight() {
        return this.height;
    }
}

