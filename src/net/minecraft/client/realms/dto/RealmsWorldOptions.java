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
import net.minecraft.client.realms.CheckedGson;
import net.minecraft.client.realms.RealmsSerializable;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.StringHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsWorldOptions
extends ValueObject
implements RealmsSerializable {
    @SerializedName(value="spawnProtection")
    public int spawnProtection = 0;
    @SerializedName(value="forceGameMode")
    public boolean forceGameMode = false;
    @SerializedName(value="difficulty")
    public int difficulty = 2;
    @SerializedName(value="gameMode")
    public int gameMode = 0;
    @SerializedName(value="slotName")
    private String slotName = "";
    @SerializedName(value="version")
    public String version = "";
    @SerializedName(value="compatibility")
    public RealmsServer.Compatibility compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
    @SerializedName(value="worldTemplateId")
    public long templateId = -1L;
    @Nullable
    @SerializedName(value="worldTemplateImage")
    public String templateImage = null;
    public boolean empty;

    private RealmsWorldOptions() {
    }

    public RealmsWorldOptions(int i, int j, int spawnProtection, boolean bl, String string, String string2, RealmsServer.Compatibility arg) {
        this.spawnProtection = i;
        this.difficulty = j;
        this.gameMode = spawnProtection;
        this.forceGameMode = bl;
        this.slotName = string;
        this.version = string2;
        this.compatibility = arg;
    }

    public static RealmsWorldOptions getDefaults() {
        return new RealmsWorldOptions();
    }

    public static RealmsWorldOptions create(GameMode gameMode, Difficulty arg2, boolean bl, String string, String string2) {
        RealmsWorldOptions lv = RealmsWorldOptions.getDefaults();
        lv.difficulty = arg2.getId();
        lv.gameMode = gameMode.getIndex();
        lv.slotName = string2;
        lv.version = string;
        return lv;
    }

    public static RealmsWorldOptions create(LevelInfo levelInfo, String string) {
        return RealmsWorldOptions.create(levelInfo.getGameMode(), levelInfo.getDifficulty(), levelInfo.isHardcore(), string, levelInfo.getLevelName());
    }

    public static RealmsWorldOptions getEmptyDefaults() {
        RealmsWorldOptions lv = RealmsWorldOptions.getDefaults();
        lv.setEmpty(true);
        return lv;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public static RealmsWorldOptions fromJson(CheckedGson gson, String json) {
        RealmsWorldOptions lv = gson.fromJson(json, RealmsWorldOptions.class);
        if (lv == null) {
            return RealmsWorldOptions.getDefaults();
        }
        RealmsWorldOptions.replaceNullsWithDefaults(lv);
        return lv;
    }

    private static void replaceNullsWithDefaults(RealmsWorldOptions options) {
        if (options.slotName == null) {
            options.slotName = "";
        }
        if (options.version == null) {
            options.version = "";
        }
        if (options.compatibility == null) {
            options.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
        }
    }

    public String getSlotName(int index) {
        if (StringHelper.isBlank(this.slotName)) {
            if (this.empty) {
                return I18n.translate("mco.configure.world.slot.empty", new Object[0]);
            }
            return this.getDefaultSlotName(index);
        }
        return this.slotName;
    }

    public String getDefaultSlotName(int index) {
        return I18n.translate("mco.configure.world.slot", index);
    }

    public RealmsWorldOptions clone() {
        return new RealmsWorldOptions(this.spawnProtection, this.difficulty, this.gameMode, this.forceGameMode, this.slotName, this.version, this.compatibility);
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return this.clone();
    }
}

