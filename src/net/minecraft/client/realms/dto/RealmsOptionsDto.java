/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsSerializable;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsWorldOptions;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public final class RealmsOptionsDto
implements RealmsSerializable {
    @SerializedName(value="slotId")
    public final int slotId;
    @SerializedName(value="spawnProtection")
    private final int spawnProtection;
    @SerializedName(value="forceGameMode")
    private final boolean forceGameMode;
    @SerializedName(value="difficulty")
    private final int difficulty;
    @SerializedName(value="gameMode")
    private final int gameMode;
    @SerializedName(value="slotName")
    private final String slotName;
    @SerializedName(value="version")
    private final String version;
    @SerializedName(value="compatibility")
    private final RealmsServer.Compatibility compatibility;
    @SerializedName(value="worldTemplateId")
    private final long worldTemplateId;
    @Nullable
    @SerializedName(value="worldTemplateImage")
    private final String worldTemplateImage;
    @SerializedName(value="hardcore")
    private final boolean hardcore;

    public RealmsOptionsDto(int slotId, RealmsWorldOptions options, boolean hardcore) {
        this.slotId = slotId;
        this.spawnProtection = options.spawnProtection;
        this.forceGameMode = options.forceGameMode;
        this.difficulty = options.difficulty;
        this.gameMode = options.gameMode;
        this.slotName = options.getSlotName(slotId);
        this.version = options.version;
        this.compatibility = options.compatibility;
        this.worldTemplateId = options.templateId;
        this.worldTemplateImage = options.templateImage;
        this.hardcore = hardcore;
    }
}

