/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsSerializable;
import net.minecraft.client.realms.util.DontSerialize;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CheckedGson {
    ExclusionStrategy EXCLUSION_STRATEGY = new ExclusionStrategy(this){

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(DontSerialize.class) != null;
        }
    };
    private final Gson GSON = new GsonBuilder().addSerializationExclusionStrategy(this.EXCLUSION_STRATEGY).addDeserializationExclusionStrategy(this.EXCLUSION_STRATEGY).create();

    public String toJson(RealmsSerializable serializable) {
        return this.GSON.toJson(serializable);
    }

    public String toJson(JsonElement json) {
        return this.GSON.toJson(json);
    }

    @Nullable
    public <T extends RealmsSerializable> T fromJson(String json, Class<T> type) {
        return (T)((RealmsSerializable)this.GSON.fromJson(json, type));
    }
}

