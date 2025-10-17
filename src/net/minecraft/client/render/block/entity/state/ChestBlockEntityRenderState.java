/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class ChestBlockEntityRenderState
extends BlockEntityRenderState {
    public ChestType chestType = ChestType.SINGLE;
    public float lidAnimationProgress;
    public float yaw;
    public Variant variant = Variant.REGULAR;

    @Environment(value=EnvType.CLIENT)
    public static enum Variant {
        ENDER_CHEST,
        CHRISTMAS,
        TRAPPED,
        COPPER_UNAFFECTED,
        COPPER_EXPOSED,
        COPPER_WEATHERED,
        COPPER_OXIDIZED,
        REGULAR;

    }
}

