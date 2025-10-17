/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.net.URI;
import java.net.URISyntaxException;

public class UriUtil {
    public static final Codec<URI> URI_CODEC = Codec.STRING.comapFlatMap(uri -> {
        try {
            return DataResult.success(new URI((String)uri));
        } catch (URISyntaxException uRISyntaxException) {
            return DataResult.error(uRISyntaxException::getMessage);
        }
    }, URI::toString);

    public static URI createSchemasUri(String id) {
        return URI.create("#/components/schemas/" + id);
    }
}

