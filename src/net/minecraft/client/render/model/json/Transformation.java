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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public record Transformation(Vector3fc rotation, Vector3fc translation, Vector3fc scale) {
    public static final Transformation IDENTITY = new Transformation(new Vector3f(), new Vector3f(), new Vector3f(1.0f, 1.0f, 1.0f));

    public void apply(boolean leftHanded, MatrixStack.Entry arg) {
        float h;
        float g;
        float f;
        if (this == IDENTITY) {
            arg.translate(-0.5f, -0.5f, -0.5f);
            return;
        }
        if (leftHanded) {
            f = -this.translation.x();
            g = -this.rotation.y();
            h = -this.rotation.z();
        } else {
            f = this.translation.x();
            g = this.rotation.y();
            h = this.rotation.z();
        }
        arg.translate(f, this.translation.y(), this.translation.z());
        arg.rotate(new Quaternionf().rotationXYZ(this.rotation.x() * ((float)Math.PI / 180), g * ((float)Math.PI / 180), h * ((float)Math.PI / 180)));
        arg.scale(this.scale.x(), this.scale.y(), this.scale.z());
        arg.translate(-0.5f, -0.5f, -0.5f);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<Transformation> {
        private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0f, 0.0f, 0.0f);
        private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0f, 0.0f, 0.0f);
        private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0f, 1.0f, 1.0f);
        public static final float MAX_TRANSLATION = 5.0f;
        public static final float MAX_SCALE = 4.0f;

        protected Deserializer() {
        }

        @Override
        public Transformation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vector3f vector3f = this.parseVector3f(jsonObject, "rotation", DEFAULT_ROTATION);
            Vector3f vector3f2 = this.parseVector3f(jsonObject, "translation", DEFAULT_TRANSLATION);
            vector3f2.mul(0.0625f);
            vector3f2.set(MathHelper.clamp(vector3f2.x, -5.0f, 5.0f), MathHelper.clamp(vector3f2.y, -5.0f, 5.0f), MathHelper.clamp(vector3f2.z, -5.0f, 5.0f));
            Vector3f vector3f3 = this.parseVector3f(jsonObject, "scale", DEFAULT_SCALE);
            vector3f3.set(MathHelper.clamp(vector3f3.x, -4.0f, 4.0f), MathHelper.clamp(vector3f3.y, -4.0f, 4.0f), MathHelper.clamp(vector3f3.z, -4.0f, 4.0f));
            return new Transformation(vector3f, vector3f2, vector3f3);
        }

        private Vector3f parseVector3f(JsonObject json, String key, Vector3f fallback) {
            if (!json.has(key)) {
                return fallback;
            }
            JsonArray jsonArray = JsonHelper.getArray(json, key);
            if (jsonArray.size() != 3) {
                throw new JsonParseException("Expected 3 " + key + " values, found: " + jsonArray.size());
            }
            float[] fs = new float[3];
            for (int i = 0; i < fs.length; ++i) {
                fs[i] = JsonHelper.asFloat(jsonArray.get(i), key + "[" + i + "]");
            }
            return new Vector3f(fs[0], fs[1], fs[2]);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement functionJson, Type unused, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(functionJson, unused, context);
        }
    }
}

