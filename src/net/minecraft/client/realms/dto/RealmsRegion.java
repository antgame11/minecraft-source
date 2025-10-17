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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public enum RealmsRegion {
    AUSTRALIA_EAST("AustraliaEast", "realms.configuration.region.australia_east"),
    AUSTRALIA_SOUTHEAST("AustraliaSoutheast", "realms.configuration.region.australia_southeast"),
    BRAZIL_SOUTH("BrazilSouth", "realms.configuration.region.brazil_south"),
    CENTRAL_INDIA("CentralIndia", "realms.configuration.region.central_india"),
    CENTRAL_US("CentralUs", "realms.configuration.region.central_us"),
    EAST_ASIA("EastAsia", "realms.configuration.region.east_asia"),
    EAST_US("EastUs", "realms.configuration.region.east_us"),
    EAST_US_2("EastUs2", "realms.configuration.region.east_us_2"),
    FRANCE_CENTRAL("FranceCentral", "realms.configuration.region.france_central"),
    JAPAN_EAST("JapanEast", "realms.configuration.region.japan_east"),
    JAPAN_WEST("JapanWest", "realms.configuration.region.japan_west"),
    KOREA_CENTRAL("KoreaCentral", "realms.configuration.region.korea_central"),
    NORTH_CENTRAL_US("NorthCentralUs", "realms.configuration.region.north_central_us"),
    NORTH_EUROPE("NorthEurope", "realms.configuration.region.north_europe"),
    SOUTH_CENTRAL_US("SouthCentralUs", "realms.configuration.region.south_central_us"),
    SOUTHEAST_ASIA("SoutheastAsia", "realms.configuration.region.southeast_asia"),
    SWEDEN_CENTRAL("SwedenCentral", "realms.configuration.region.sweden_central"),
    UAE_NORTH("UAENorth", "realms.configuration.region.uae_north"),
    UK_SOUTH("UKSouth", "realms.configuration.region.uk_south"),
    WEST_CENTRAL_US("WestCentralUs", "realms.configuration.region.west_central_us"),
    WEST_EUROPE("WestEurope", "realms.configuration.region.west_europe"),
    WEST_US("WestUs", "realms.configuration.region.west_us"),
    WEST_US_2("WestUs2", "realms.configuration.region.west_us_2"),
    INVALID_REGION("invalid", "");

    public final String name;
    public final String translationKey;

    private RealmsRegion(String name, String translationKey) {
        this.name = name;
        this.translationKey = translationKey;
    }

    @Nullable
    public static RealmsRegion fromName(String name) {
        for (RealmsRegion lv : RealmsRegion.values()) {
            if (!lv.name.equals(name)) continue;
            return lv;
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public static class RegionTypeAdapter
    extends TypeAdapter<RealmsRegion> {
        private static final Logger LOGGER = LogUtils.getLogger();

        @Override
        public void write(JsonWriter jsonWriter, RealmsRegion arg) throws IOException {
            jsonWriter.value(arg.name);
        }

        @Override
        public RealmsRegion read(JsonReader jsonReader) throws IOException {
            String string = jsonReader.nextString();
            RealmsRegion lv = RealmsRegion.fromName(string);
            if (lv == null) {
                LOGGER.warn("Unsupported RealmsRegion {}", (Object)string);
                return INVALID_REGION;
            }
            return lv;
        }

        @Override
        public /* synthetic */ Object read(JsonReader reader) throws IOException {
            return this.read(reader);
        }

        @Override
        public /* synthetic */ void write(JsonWriter writer, Object region) throws IOException {
            this.write(writer, (RealmsRegion)((Object)region));
        }
    }
}

