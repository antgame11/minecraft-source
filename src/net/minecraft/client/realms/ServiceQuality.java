/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public enum ServiceQuality {
    GREAT(1, "icon/ping_5"),
    GOOD(2, "icon/ping_4"),
    OKAY(3, "icon/ping_3"),
    POOR(4, "icon/ping_2"),
    UNKNOWN(5, "icon/ping_unknown");

    final int index;
    private final Identifier icon;

    private ServiceQuality(int index, String icon) {
        this.index = index;
        this.icon = Identifier.ofVanilla(icon);
    }

    @Nullable
    public static ServiceQuality byIndex(int index) {
        for (ServiceQuality lv : ServiceQuality.values()) {
            if (lv.getIndex() != index) continue;
            return lv;
        }
        return null;
    }

    public int getIndex() {
        return this.index;
    }

    public Identifier getIcon() {
        return this.icon;
    }

    @Environment(value=EnvType.CLIENT)
    public static class ServiceQualityTypeAdapter
    extends TypeAdapter<ServiceQuality> {
        private static final Logger LOGGER = LogUtils.getLogger();

        @Override
        public void write(JsonWriter jsonWriter, ServiceQuality arg) throws IOException {
            jsonWriter.value(arg.index);
        }

        @Override
        public ServiceQuality read(JsonReader jsonReader) throws IOException {
            int i = jsonReader.nextInt();
            ServiceQuality lv = ServiceQuality.byIndex(i);
            if (lv == null) {
                LOGGER.warn("Unsupported ServiceQuality {}", (Object)i);
                return UNKNOWN;
            }
            return lv;
        }

        @Override
        public /* synthetic */ Object read(JsonReader reader) throws IOException {
            return this.read(reader);
        }

        @Override
        public /* synthetic */ void write(JsonWriter writer, Object serviceQuality) throws IOException {
            this.write(writer, (ServiceQuality)((Object)serviceQuality));
        }
    }
}

