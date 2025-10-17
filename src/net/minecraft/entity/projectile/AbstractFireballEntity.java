/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AbstractFireballEntity
extends ExplosiveProjectileEntity
implements FlyingItemEntity {
    private static final float MAX_RENDER_DISTANCE_WHEN_NEWLY_SPAWNED = 12.25f;
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(AbstractFireballEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    public AbstractFireballEntity(EntityType<? extends AbstractFireballEntity> arg, World arg2) {
        super((EntityType<? extends ExplosiveProjectileEntity>)arg, arg2);
    }

    public AbstractFireballEntity(EntityType<? extends AbstractFireballEntity> arg, double d, double e, double f, Vec3d arg2, World arg3) {
        super(arg, d, e, f, arg2, arg3);
    }

    public AbstractFireballEntity(EntityType<? extends AbstractFireballEntity> arg, LivingEntity arg2, Vec3d arg3, World arg4) {
        super(arg, arg2, arg3, arg4);
    }

    public void setItem(ItemStack stack) {
        if (stack.isEmpty()) {
            this.getDataTracker().set(ITEM, this.getItem());
        } else {
            this.getDataTracker().set(ITEM, stack.copyWithCount(1));
        }
    }

    @Override
    protected void playExtinguishSound() {
    }

    @Override
    public ItemStack getStack() {
        return this.getDataTracker().get(ITEM);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ITEM, this.getItem());
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("Item", ItemStack.CODEC, this.getStack());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setItem(view.read("Item", ItemStack.CODEC).orElse(this.getItem()));
    }

    private ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        if (mappedIndex == 0) {
            return StackReference.of(this::getStack, this::setItem);
        }
        return super.getStackReference(mappedIndex);
    }

    @Override
    public boolean shouldRender(double distance) {
        if (this.age < 2 && distance < 12.25) {
            return false;
        }
        return super.shouldRender(distance);
    }
}

