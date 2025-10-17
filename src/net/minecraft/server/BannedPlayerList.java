/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.dedicated.management.listener.ManagementListener;

public class BannedPlayerList
extends ServerConfigList<PlayerConfigEntry, BannedPlayerEntry> {
    public BannedPlayerList(File file, ManagementListener arg) {
        super(file, arg);
    }

    @Override
    protected ServerConfigEntry<PlayerConfigEntry> fromJson(JsonObject json) {
        return new BannedPlayerEntry(json);
    }

    @Override
    public boolean contains(PlayerConfigEntry player) {
        return this.contains(player);
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
    public boolean add(BannedPlayerEntry arg) {
        if (super.add(arg)) {
            if (arg.getKey() != null) {
                this.field_62420.onBanAdded(arg);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(PlayerConfigEntry arg) {
        if (super.remove(arg)) {
            this.field_62420.onBanRemoved(arg);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (BannedPlayerEntry lv : this.values()) {
            if (lv.getKey() == null) continue;
            this.field_62420.onBanRemoved((PlayerConfigEntry)lv.getKey());
        }
        super.clear();
    }

    @Override
    public /* synthetic */ boolean remove(Object key) {
        return this.remove((PlayerConfigEntry)key);
    }
}

