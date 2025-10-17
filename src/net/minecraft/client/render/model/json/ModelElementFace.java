/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ModelElementFace(@Nullable Direction cullFace, int tintIndex, String textureId, @Nullable UV uvs, AxisRotation rotation) {
    public static final int field_32789 = -1;

    public static float getUValue(UV arg, AxisRotation arg2, int i) {
        return arg.getUVertices(arg2.rotate(i)) / 16.0f;
    }

    public static float getVValue(UV arg, AxisRotation arg2, int i) {
        return arg.getVVertices(arg2.rotate(i)) / 16.0f;
    }

    @Nullable
    public Direction cullFace() {
        return this.cullFace;
    }

    @Nullable
    public UV uvs() {
        return this.uvs;
    }

    @Environment(value=EnvType.CLIENT)
    public record UV(float minU, float minV, float maxU, float maxV) {
        public float getUVertices(int i) {
            return i == 0 || i == 1 ? this.minU : this.maxU;
        }

        public float getVVertices(int i) {
            return i == 0 || i == 3 ? this.minV : this.maxV;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<ModelElementFace> {
        private static final int DEFAULT_TINT_INDEX = -1;
        private static final int field_56927 = 0;

        protected Deserializer() {
        }

        @Override
        public ModelElementFace deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Direction lv = Deserializer.deserializeCullFace(jsonObject);
            int i = Deserializer.deserializeTintIndex(jsonObject);
            String string = Deserializer.deserializeTexture(jsonObject);
            UV lv2 = Deserializer.getUV(jsonObject);
            AxisRotation lv3 = Deserializer.getRotation(jsonObject);
            return new ModelElementFace(lv, i, string, lv2, lv3);
        }

        private static int deserializeTintIndex(JsonObject jsonObject) {
            return JsonHelper.getInt(jsonObject, "tintindex", -1);
        }

        private static String deserializeTexture(JsonObject jsonObject) {
            return JsonHelper.getString(jsonObject, "texture");
        }

        @Nullable
        private static Direction deserializeCullFace(JsonObject jsonObject) {
            String string = JsonHelper.getString(jsonObject, "cullface", "");
            return Direction.byId(string);
        }

        private static AxisRotation getRotation(JsonObject jsonObject) {
            int i = JsonHelper.getInt(jsonObject, "rotation", 0);
            return AxisRotation.fromDegrees(i);
        }

        @Nullable
        private static UV getUV(JsonObject jsonObject) {
            if (!jsonObject.has("uv")) {
                return null;
            }
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "uv");
            if (jsonArray.size() != 4) {
                throw new JsonParseException("Expected 4 uv values, found: " + jsonArray.size());
            }
            float f = JsonHelper.asFloat(jsonArray.get(0), "minU");
            float g = JsonHelper.asFloat(jsonArray.get(1), "minV");
            float h = JsonHelper.asFloat(jsonArray.get(2), "maxU");
            float i = JsonHelper.asFloat(jsonArray.get(3), "maxV");
            return new UV(f, g, h, i);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement functionJson, Type unused, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(functionJson, unused, context);
        }
    }
}

