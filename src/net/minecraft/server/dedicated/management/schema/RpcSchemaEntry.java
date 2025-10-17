/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.schema;

import java.net.URI;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public record RpcSchemaEntry(String name, URI reference, RpcSchema schema) {
    public RpcSchema ref() {
        return RpcSchema.ofReference(this.reference);
    }

    public RpcSchema array() {
        return RpcSchema.ofArray(RpcSchema.ofReference(this.reference));
    }
}

