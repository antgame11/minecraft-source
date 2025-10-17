/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerConfigEntry;

public class OperatorEntry
extends ServerConfigEntry<PlayerConfigEntry> {
    private final int permissionLevel;
    private final boolean bypassPlayerLimit;

    public OperatorEntry(PlayerConfigEntry player, int permissionLevel, boolean bypassPlayerLimit) {
        super(player);
        this.permissionLevel = permissionLevel;
        this.bypassPlayerLimit = bypassPlayerLimit;
    }

    public OperatorEntry(JsonObject json) {
        super(PlayerConfigEntry.read(json));
        this.permissionLevel = json.has("level") ? json.get("level").getAsInt() : 0;
        this.bypassPlayerLimit = json.has("bypassesPlayerLimit") && json.get("bypassesPlayerLimit").getAsBoolean();
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    public boolean canBypassPlayerLimit() {
        return this.bypassPlayerLimit;
    }

    @Override
    protected void write(JsonObject json) {
        if (this.getKey() == null) {
            return;
        }
        ((PlayerConfigEntry)this.getKey()).write(json);
        json.addProperty("level", this.permissionLevel);
        json.addProperty("bypassesPlayerLimit", this.bypassPlayerLimit);
    }
}

