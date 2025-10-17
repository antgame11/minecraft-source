/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.dedicated.management.listener.ManagementListener;
import org.jetbrains.annotations.Nullable;

public class BannedIpList
extends ServerConfigList<String, BannedIpEntry> {
    public BannedIpList(File file, ManagementListener arg) {
        super(file, arg);
    }

    @Override
    protected ServerConfigEntry<String> fromJson(JsonObject json) {
        return new BannedIpEntry(json);
    }

    public boolean isBanned(SocketAddress ip) {
        String string = this.stringifyAddress(ip);
        return this.contains(string);
    }

    public boolean isBanned(String ip) {
        return this.contains(ip);
    }

    @Override
    @Nullable
    public BannedIpEntry get(SocketAddress address) {
        String string = this.stringifyAddress(address);
        return (BannedIpEntry)this.get(string);
    }

    private String stringifyAddress(SocketAddress address) {
        String string = address.toString();
        if (string.contains("/")) {
            string = string.substring(string.indexOf(47) + 1);
        }
        if (string.contains(":")) {
            string = string.substring(0, string.indexOf(58));
        }
        return string;
    }

    @Override
    public boolean add(BannedIpEntry arg) {
        if (super.add(arg)) {
            if (arg.getKey() != null) {
                this.field_62420.onIpBanAdded(arg);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(String string) {
        if (super.remove(string)) {
            this.field_62420.onIpBanRemoved(string);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (BannedIpEntry lv : this.values()) {
            if (lv.getKey() == null) continue;
            this.field_62420.onIpBanRemoved((String)lv.getKey());
        }
        super.clear();
    }

    @Override
    public /* synthetic */ boolean remove(Object key) {
        return this.remove((String)key);
    }
}

