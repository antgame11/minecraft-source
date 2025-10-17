/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.dedicated.management.listener.ManagementListener;

public class OperatorList
extends ServerConfigList<PlayerConfigEntry, OperatorEntry> {
    public OperatorList(File file, ManagementListener arg) {
        super(file, arg);
    }

    @Override
    protected ServerConfigEntry<PlayerConfigEntry> fromJson(JsonObject json) {
        return new OperatorEntry(json);
    }

    @Override
    public String[] getNames() {
        return (String[])this.values().stream().map(ServerConfigEntry::getKey).filter(Objects::nonNull).map(PlayerConfigEntry::name).toArray(String[]::new);
    }

    @Override
    public boolean add(OperatorEntry arg) {
        if (super.add(arg)) {
            if (arg.getKey() != null) {
                this.field_62420.onOperatorAdded(arg);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(PlayerConfigEntry arg) {
        OperatorEntry lv = (OperatorEntry)this.get(arg);
        if (super.remove(arg)) {
            if (lv != null) {
                this.field_62420.onOperatorRemoved(lv);
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (OperatorEntry lv : this.values()) {
            if (lv.getKey() == null) continue;
            this.field_62420.onOperatorRemoved(lv);
        }
        super.clear();
    }

    public boolean canBypassPlayerLimit(PlayerConfigEntry arg) {
        OperatorEntry lv = (OperatorEntry)this.get(arg);
        if (lv != null) {
            return lv.canBypassPlayerLimit();
        }
        return false;
    }

    @Override
    protected String toString(PlayerConfigEntry arg) {
        return arg.id().toString();
    }

    @Override
    protected /* synthetic */ String toString(Object profile) {
        return this.toString((PlayerConfigEntry)profile);
    }
}

