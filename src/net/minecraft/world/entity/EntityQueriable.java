/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.entity;

import java.util.UUID;
import net.minecraft.world.entity.UniquelyIdentifiable;
import org.jetbrains.annotations.Nullable;

public interface EntityQueriable<IdentifiedType extends UniquelyIdentifiable> {
    @Nullable
    public IdentifiedType lookup(UUID var1);
}

