/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.util.LenientJsonParser;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BackupList
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public List<Backup> backups;

    public static BackupList parse(String json) {
        BackupList lv = new BackupList();
        lv.backups = Lists.newArrayList();
        try {
            JsonElement jsonElement = LenientJsonParser.parse(json).getAsJsonObject().get("backups");
            if (jsonElement.isJsonArray()) {
                for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
                    lv.backups.add(Backup.parse(jsonElement2));
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Could not parse BackupList: {}", (Object)exception.getMessage());
        }
        return lv;
    }
}

