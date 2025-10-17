/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.server.dedicated.management.IncomingRpcMethod;
import net.minecraft.server.dedicated.management.OutgoingRpcMethod;
import net.minecraft.server.dedicated.management.RpcMethodInfo;
import net.minecraft.server.dedicated.management.schema.RpcSchema;
import net.minecraft.server.dedicated.management.schema.RpcSchemaEntry;

public class RpcDiscover {
    public static Document handleRpcDiscover(List<RpcSchemaEntry> entries) {
        ArrayList<RpcMethodInfo.Entry> list2 = new ArrayList<RpcMethodInfo.Entry>(Registries.INCOMING_RPC_METHOD.size() + Registries.OUTGOING_RPC_METHOD.size());
        Registries.INCOMING_RPC_METHOD.streamEntries().forEach(entry -> {
            if (((IncomingRpcMethod)entry.value()).attributes().discoverable()) {
                list2.add(((IncomingRpcMethod)entry.value()).info().toEntry(entry.registryKey().getValue()));
            }
        });
        Registries.OUTGOING_RPC_METHOD.streamEntries().forEach(entry -> {
            if (((OutgoingRpcMethod)entry.value()).attributes().discoverable()) {
                list2.add(((OutgoingRpcMethod)entry.value()).info().toEntry(entry.registryKey().getValue()));
            }
        });
        HashMap<String, RpcSchema> map = new HashMap<String, RpcSchema>();
        for (RpcSchemaEntry lv : entries) {
            map.put(lv.name(), lv.schema());
        }
        Info lv2 = new Info("Minecraft Server JSON-RPC", "1.0.0");
        return new Document("1.3.2", lv2, list2, new Components(map));
    }

    public record Info(String title, String version) {
        public static final MapCodec<Info> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("title")).forGetter(Info::title), ((MapCodec)Codec.STRING.fieldOf("version")).forGetter(Info::version)).apply((Applicative<Info, ?>)instance, Info::new));
    }

    public record Document(String jsonRpcProtocolVersion, Info discoverInfo, List<RpcMethodInfo.Entry> methods, Components components) {
        public static final MapCodec<Document> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("openrpc")).forGetter(Document::jsonRpcProtocolVersion), ((MapCodec)Info.CODEC.codec().fieldOf("info")).forGetter(Document::discoverInfo), ((MapCodec)Codec.list(RpcMethodInfo.Entry.CODEC).fieldOf("methods")).forGetter(Document::methods), ((MapCodec)Components.CODEC.codec().fieldOf("components")).forGetter(Document::components)).apply((Applicative<Document, ?>)instance, Document::new));
    }

    public record Components(Map<String, RpcSchema> schemas) {
        public static final MapCodec<Components> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.unboundedMap(Codec.STRING, RpcSchema.CODEC).fieldOf("schemas")).forGetter(Components::schemas)).apply((Applicative<Components, ?>)instance, Components::new));
    }
}

