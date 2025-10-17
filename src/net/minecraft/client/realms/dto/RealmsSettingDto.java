/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsSerializable;

@Environment(value=EnvType.CLIENT)
public record RealmsSettingDto(@SerializedName(value="name") String name, @SerializedName(value="value") String value) implements RealmsSerializable
{
    public static RealmsSettingDto ofHardcore(boolean hardcore) {
        return new RealmsSettingDto("hardcore", Boolean.toString(hardcore));
    }

    public static boolean isHardcore(List<RealmsSettingDto> settings) {
        for (RealmsSettingDto lv : settings) {
            if (!lv.name().equals("hardcore")) continue;
            return Boolean.parseBoolean(lv.value());
        }
        return false;
    }

    @SerializedName(value="name")
    public String name() {
        return this.name;
    }

    @SerializedName(value="value")
    public String value() {
        return this.value;
    }
}

