/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.PngMetadata;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ServerInfo {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_FAVICON_SIZE = 1024;
    public String name;
    public String address;
    public Text playerCountLabel;
    public Text label;
    @Nullable
    public ServerMetadata.Players players;
    public long ping;
    public int protocolVersion = SharedConstants.getGameVersion().protocolVersion();
    public Text version = Text.literal(SharedConstants.getGameVersion().name());
    public List<Text> playerListSummary = Collections.emptyList();
    private ResourcePackPolicy resourcePackPolicy = ResourcePackPolicy.PROMPT;
    @Nullable
    private byte[] favicon;
    private ServerType serverType;
    private int acceptedCodeOfConduct;
    private Status status = Status.INITIAL;

    public ServerInfo(String name, String address, ServerType serverType) {
        this.name = name;
        this.address = address;
        this.serverType = serverType;
    }

    public NbtCompound toNbt() {
        NbtCompound lv = new NbtCompound();
        lv.putString("name", this.name);
        lv.putString("ip", this.address);
        lv.putNullable("icon", Codecs.BASE_64, this.favicon);
        lv.copyFromCodec(ResourcePackPolicy.CODEC, this.resourcePackPolicy);
        if (this.acceptedCodeOfConduct != 0) {
            lv.putInt("acceptedCodeOfConduct", this.acceptedCodeOfConduct);
        }
        return lv;
    }

    public ResourcePackPolicy getResourcePackPolicy() {
        return this.resourcePackPolicy;
    }

    public void setResourcePackPolicy(ResourcePackPolicy resourcePackPolicy) {
        this.resourcePackPolicy = resourcePackPolicy;
    }

    public static ServerInfo fromNbt(NbtCompound root) {
        ServerInfo lv = new ServerInfo(root.getString("name", ""), root.getString("ip", ""), ServerType.OTHER);
        lv.setFavicon(root.get("icon", Codecs.BASE_64).orElse(null));
        lv.setResourcePackPolicy(root.decode(ResourcePackPolicy.CODEC).orElse(ResourcePackPolicy.PROMPT));
        lv.acceptedCodeOfConduct = root.getInt("acceptedCodeOfConduct", 0);
        return lv;
    }

    @Nullable
    public byte[] getFavicon() {
        return this.favicon;
    }

    public void setFavicon(@Nullable byte[] favicon) {
        this.favicon = favicon;
    }

    public boolean isLocal() {
        return this.serverType == ServerType.LAN;
    }

    public boolean isRealm() {
        return this.serverType == ServerType.REALM;
    }

    public ServerType getServerType() {
        return this.serverType;
    }

    public boolean hasAcceptedCodeOfConduct(String codeOfConductText) {
        return this.acceptedCodeOfConduct == codeOfConductText.hashCode();
    }

    public void setAcceptedCodeOfConduct(String codeOfConductText) {
        this.acceptedCodeOfConduct = codeOfConductText.hashCode();
    }

    public void resetAcceptedCodeOfConduct() {
        this.acceptedCodeOfConduct = 0;
    }

    public void copyFrom(ServerInfo serverInfo) {
        this.address = serverInfo.address;
        this.name = serverInfo.name;
        this.favicon = serverInfo.favicon;
    }

    public void copyWithSettingsFrom(ServerInfo serverInfo) {
        this.copyFrom(serverInfo);
        this.setResourcePackPolicy(serverInfo.getResourcePackPolicy());
        this.serverType = serverInfo.serverType;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Nullable
    public static byte[] validateFavicon(@Nullable byte[] favicon) {
        if (favicon != null) {
            try {
                PngMetadata lv = PngMetadata.fromBytes(favicon);
                if (lv.width() <= 1024 && lv.height() <= 1024) {
                    return favicon;
                }
            } catch (IOException iOException) {
                LOGGER.warn("Failed to decode server icon", iOException);
            }
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ResourcePackPolicy {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        public static final MapCodec<ResourcePackPolicy> CODEC;
        private final Text name;

        private ResourcePackPolicy(String name) {
            this.name = Text.translatable("manageServer.resourcePack." + name);
        }

        public Text getName() {
            return this.name;
        }

        static {
            CODEC = Codec.BOOL.optionalFieldOf("acceptTextures").xmap(value -> value.map(acceptTextures -> acceptTextures != false ? ENABLED : DISABLED).orElse(PROMPT), value -> switch (value.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Optional.of(true);
                case 1 -> Optional.of(false);
                case 2 -> Optional.empty();
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Status {
        INITIAL,
        PINGING,
        UNREACHABLE,
        INCOMPATIBLE,
        SUCCESSFUL;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum ServerType {
        LAN,
        REALM,
        OTHER;

    }
}

