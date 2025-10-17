/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface Tameable {
    @Nullable
    public LazyEntityReference<LivingEntity> getOwnerReference();

    public World getEntityWorld();

    @Nullable
    default public LivingEntity getOwner() {
        return LazyEntityReference.getLivingEntity(this.getOwnerReference(), this.getEntityWorld());
    }

    @Nullable
    default public LivingEntity getTopLevelOwner() {
        ObjectArraySet set = new ObjectArraySet();
        LivingEntity lv = this.getOwner();
        set.add(this);
        while (lv instanceof Tameable) {
            Tameable lv2 = (Tameable)((Object)lv);
            LivingEntity lv3 = lv2.getOwner();
            if (set.contains(lv3)) {
                return null;
            }
            set.add(lv);
            lv = lv2.getOwner();
        }
        return lv;
    }
}

