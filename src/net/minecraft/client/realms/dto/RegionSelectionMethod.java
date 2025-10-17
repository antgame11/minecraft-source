/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public enum RegionSelectionMethod {
    AUTOMATIC_PLAYER(0, "realms.configuration.region_preference.automatic_player"),
    AUTOMATIC_OWNER(1, "realms.configuration.region_preference.automatic_owner"),
    MANUAL(2, "");

    public static final RegionSelectionMethod DEFAULT;
    public final int index;
    public final String translationKey;

    private RegionSelectionMethod(int index, String translationKey) {
        this.index = index;
        this.translationKey = translationKey;
    }

    static {
        DEFAULT = AUTOMATIC_PLAYER;
    }

    @Environment(value=EnvType.CLIENT)
    public static class SelectionMethodTypeAdapter
    extends TypeAdapter<RegionSelectionMethod> {
        private static final Logger LOGGER = LogUtils.getLogger();

        @Override
        public void write(JsonWriter jsonWriter, RegionSelectionMethod arg) throws IOException {
            jsonWriter.value(arg.index);
        }

        @Override
        public RegionSelectionMethod read(JsonReader jsonReader) throws IOException {
            int i = jsonReader.nextInt();
            for (RegionSelectionMethod lv : RegionSelectionMethod.values()) {
                if (lv.index != i) continue;
                return lv;
            }
            LOGGER.warn("Unsupported RegionSelectionPreference {}", (Object)i);
            return DEFAULT;
        }

        @Override
        public /* synthetic */ Object read(JsonReader reader) throws IOException {
            return this.read(reader);
        }

        @Override
        public /* synthetic */ void write(JsonWriter writer, Object selectionMethod) throws IOException {
            this.write(writer, (RegionSelectionMethod)((Object)selectionMethod));
        }
    }
}

