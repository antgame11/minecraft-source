/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.item;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.network.ClientDynamicRegistryType;
import net.minecraft.registry.ContextSwappableRegistryLookup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ItemAssetsLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceFinder FINDER = ResourceFinder.json("items");

    public static CompletableFuture<Result> load(ResourceManager resourceManager, Executor executor) {
        DynamicRegistryManager.Immutable lv = ClientDynamicRegistryType.createCombinedDynamicRegistries().getCombinedRegistryManager();
        return CompletableFuture.supplyAsync(() -> FINDER.findResources(resourceManager), executor).thenCompose(itemAssets -> {
            ArrayList list = new ArrayList(itemAssets.size());
            itemAssets.forEach((itemId, itemResource) -> list.add(CompletableFuture.supplyAsync(() -> {
                Definition definition;
                block8: {
                    Identifier lv = FINDER.toResourceId((Identifier)itemId);
                    BufferedReader reader = itemResource.getReader();
                    try {
                        ContextSwappableRegistryLookup lv2 = new ContextSwappableRegistryLookup(lv);
                        RegistryOps<JsonElement> dynamicOps = lv2.createRegistryOps(JsonOps.INSTANCE);
                        ItemAsset lv3 = ItemAsset.CODEC.parse(dynamicOps, StrictJsonParser.parse(reader)).ifError(error -> LOGGER.error("Couldn't parse item model '{}' from pack '{}': {}", lv, itemResource.getPackId(), error.message())).result().map(itemAsset -> {
                            if (lv2.hasEntries()) {
                                return itemAsset.withContextSwapper(lv2.createContextSwapper());
                            }
                            return itemAsset;
                        }).orElse(null);
                        definition = new Definition(lv, lv3);
                        if (reader == null) break block8;
                    } catch (Throwable throwable) {
                        try {
                            if (reader != null) {
                                try {
                                    ((Reader)reader).close();
                                } catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        } catch (Exception exception) {
                            LOGGER.error("Failed to open item model {} from pack '{}'", itemId, itemResource.getPackId(), exception);
                            return new Definition(lv, null);
                        }
                    }
                    ((Reader)reader).close();
                }
                return definition;
            }, executor)));
            return Util.combineSafe(list).thenApply(definitions -> {
                HashMap<Identifier, ItemAsset> map = new HashMap<Identifier, ItemAsset>();
                for (Definition lv : definitions) {
                    if (lv.clientItemInfo == null) continue;
                    map.put(lv.id, lv.clientItemInfo);
                }
                return new Result(map);
            });
        });
    }

    @Environment(value=EnvType.CLIENT)
    record Definition(Identifier id, @Nullable ItemAsset clientItemInfo) {
        @Nullable
        public ItemAsset clientItemInfo() {
            return this.clientItemInfo;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Result(Map<Identifier, ItemAsset> contents) {
    }
}

