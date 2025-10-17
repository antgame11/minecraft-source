/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.World;
import org.slf4j.Logger;

public abstract class TameableShoulderEntity
extends TameableEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int READY_TO_SIT_COOLDOWN = 100;
    private int ticks;

    protected TameableShoulderEntity(EntityType<? extends TameableShoulderEntity> arg, World arg2) {
        super((EntityType<? extends TameableEntity>)arg, arg2);
    }

    public boolean mountOnto(ServerPlayerEntity player) {
        try (ErrorReporter.Logging lv = new ErrorReporter.Logging(this.getErrorReporterContext(), LOGGER);){
            NbtWriteView lv2 = NbtWriteView.create(lv, this.getRegistryManager());
            this.writeData(lv2);
            lv2.putString("id", this.getSavedEntityId());
            if (player.mountOntoShoulder(lv2.getNbt())) {
                this.discard();
                boolean bl = true;
                return bl;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        ++this.ticks;
        super.tick();
    }

    public boolean isReadyToSitOnPlayer() {
        return this.ticks > 100;
    }
}

