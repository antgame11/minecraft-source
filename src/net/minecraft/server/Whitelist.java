/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.dedicated.management.listener.ManagementListener;

public class Whitelist
extends ServerConfigList<PlayerConfigEntry, WhitelistEntry> {
    public Whitelist(File file, ManagementListener arg) {
        super(file, arg);
    }

    @Override
    protected ServerConfigEntry<PlayerConfigEntry> fromJson(JsonObject json) {
        return new WhitelistEntry(json);
    }

    public boolean isAllowed(PlayerConfigEntry arg) {
        return this.contains(arg);
    }

    @Override
    public boolean add(WhitelistEntry arg) {
        if (super.add(arg)) {
            if (arg.getKey() != null) {
                this.field_62420.onAllowlistAdded((PlayerConfigEntry)arg.getKey());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(PlayerConfigEntry arg) {
        if (super.remove(arg)) {
            this.field_62420.onAllowlistRemoved(arg);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (WhitelistEntry lv : this.values()) {
            if (lv.getKey() == null) continue;
            this.field_62420.onAllowlistRemoved((PlayerConfigEntry)lv.getKey());
        }
        super.clear();
    }

    @Override
    public String[] getNames() {
        return (String[])this.values().stream().map(ServerConfigEntry::getKey).filter(Objects::nonNull).map(PlayerConfigEntry::name).toArray(String[]::new);
    }

    @Override
    protected String toString(PlayerConfigEntry arg) {
        return arg.id().toString();
    }

    @Override
    protected /* synthetic */ String toString(Object profile) {
        return this.toString((PlayerConfigEntry)profile);
    }

    @Override
    public /* synthetic */ boolean remove(Object key) {
        return this.remove((PlayerConfigEntry)key);
    }
}

