/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedGeometry;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record JsonUnbakedModel(@Nullable Geometry geometry, @Nullable UnbakedModel.GuiLight guiLight, @Nullable Boolean ambientOcclusion, @Nullable ModelTransformation transformations, ModelTextures.Textures textures, @Nullable Identifier parent) implements UnbakedModel
{
    @VisibleForTesting
    static final Gson GSON = new GsonBuilder().registerTypeAdapter((Type)((Object)JsonUnbakedModel.class), new Deserializer()).registerTypeAdapter((Type)((Object)ModelElement.class), new ModelElement.Deserializer()).registerTypeAdapter((Type)((Object)ModelElementFace.class), new ModelElementFace.Deserializer()).registerTypeAdapter((Type)((Object)Transformation.class), new Transformation.Deserializer()).registerTypeAdapter((Type)((Object)ModelTransformation.class), new ModelTransformation.Deserializer()).create();

    public static JsonUnbakedModel deserialize(Reader input) {
        return JsonHelper.deserialize(GSON, input, JsonUnbakedModel.class);
    }

    @Override
    @Nullable
    public Geometry geometry() {
        return this.geometry;
    }

    @Override
    @Nullable
    public UnbakedModel.GuiLight guiLight() {
        return this.guiLight;
    }

    @Override
    @Nullable
    public Boolean ambientOcclusion() {
        return this.ambientOcclusion;
    }

    @Override
    @Nullable
    public ModelTransformation transformations() {
        return this.transformations;
    }

    @Override
    @Nullable
    public Identifier parent() {
        return this.parent;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Deserializer
    implements JsonDeserializer<JsonUnbakedModel> {
        @Override
        public JsonUnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Geometry lv = this.elementsFromJson(jsonDeserializationContext, jsonObject);
            String string = this.parentFromJson(jsonObject);
            ModelTextures.Textures lv2 = this.texturesFromJson(jsonObject);
            Boolean boolean_ = this.ambientOcclusionFromJson(jsonObject);
            ModelTransformation lv3 = null;
            if (jsonObject.has("display")) {
                JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "display");
                lv3 = (ModelTransformation)jsonDeserializationContext.deserialize(jsonObject2, (Type)((Object)ModelTransformation.class));
            }
            UnbakedModel.GuiLight lv4 = null;
            if (jsonObject.has("gui_light")) {
                lv4 = UnbakedModel.GuiLight.byName(JsonHelper.getString(jsonObject, "gui_light"));
            }
            Identifier lv5 = string.isEmpty() ? null : Identifier.of(string);
            return new JsonUnbakedModel(lv, lv4, boolean_, lv3, lv2, lv5);
        }

        private ModelTextures.Textures texturesFromJson(JsonObject object) {
            if (object.has("textures")) {
                JsonObject jsonObject2 = JsonHelper.getObject(object, "textures");
                return ModelTextures.fromJson(jsonObject2, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
            }
            return ModelTextures.Textures.EMPTY;
        }

        private String parentFromJson(JsonObject json) {
            return JsonHelper.getString(json, "parent", "");
        }

        @Nullable
        protected Boolean ambientOcclusionFromJson(JsonObject json) {
            if (json.has("ambientocclusion")) {
                return JsonHelper.getBoolean(json, "ambientocclusion");
            }
            return null;
        }

        @Nullable
        protected Geometry elementsFromJson(JsonDeserializationContext context, JsonObject json) {
            if (json.has("elements")) {
                ArrayList<ModelElement> list = new ArrayList<ModelElement>();
                for (JsonElement jsonElement : JsonHelper.getArray(json, "elements")) {
                    list.add((ModelElement)context.deserialize(jsonElement, (Type)((Object)ModelElement.class)));
                }
                return new UnbakedGeometry(list);
            }
            return null;
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement element, Type unused, JsonDeserializationContext ctx) throws JsonParseException {
            return this.deserialize(element, unused, ctx);
        }
    }
}

