/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.util.LenientJsonParser;

@Environment(value=EnvType.CLIENT)
public class Ops
extends ValueObject {
    public Set<String> ops = Sets.newHashSet();

    public static Ops parse(String json) {
        Ops lv = new Ops();
        try {
            JsonObject jsonObject = LenientJsonParser.parse(json).getAsJsonObject();
            JsonElement jsonElement = jsonObject.get("ops");
            if (jsonElement.isJsonArray()) {
                for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
                    lv.ops.add(jsonElement2.getAsString());
                }
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return lv;
    }
}

