/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerConfigEntry;

public class WhitelistEntry
extends ServerConfigEntry<PlayerConfigEntry> {
    public WhitelistEntry(PlayerConfigEntry player) {
        super(player);
    }

    public WhitelistEntry(JsonObject json) {
        super(PlayerConfigEntry.read(json));
    }

    @Override
    protected void write(JsonObject json) {
        if (this.getKey() == null) {
            return;
        }
        ((PlayerConfigEntry)this.getKey()).write(json);
    }
}

