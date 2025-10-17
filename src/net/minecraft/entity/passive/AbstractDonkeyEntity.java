/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class AbstractDonkeyEntity
extends AbstractHorseEntity {
    private static final TrackedData<Boolean> CHEST = DataTracker.registerData(AbstractDonkeyEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final boolean DEFAULT_HAS_CHEST = false;
    private final EntityDimensions babyBaseDimensions;

    protected AbstractDonkeyEntity(EntityType<? extends AbstractDonkeyEntity> arg, World arg2) {
        super((EntityType<? extends AbstractHorseEntity>)arg, arg2);
        this.playExtraHorseSounds = false;
        this.babyBaseDimensions = arg.getDimensions().withAttachments(EntityAttachments.builder().add(EntityAttachmentType.PASSENGER, 0.0f, arg.getHeight() - 0.15625f, 0.0f)).scaled(0.5f);
    }

    @Override
    protected void initAttributes(Random random) {
        this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(AbstractDonkeyEntity.getChildHealthBonus(random::nextInt));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CHEST, false);
    }

    public static DefaultAttributeContainer.Builder createAbstractDonkeyAttributes() {
        return AbstractDonkeyEntity.createBaseHorseAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.175f).add(EntityAttributes.JUMP_STRENGTH, 0.5);
    }

    public boolean hasChest() {
        return this.dataTracker.get(CHEST);
    }

    public void setHasChest(boolean hasChest) {
        this.dataTracker.set(CHEST, hasChest);
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? this.babyBaseDimensions : super.getBaseDimensions(pose);
    }

    @Override
    protected void dropInventory(ServerWorld world) {
        super.dropInventory(world);
        if (this.hasChest()) {
            this.dropItem(world, Blocks.CHEST);
            this.setHasChest(false);
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("ChestedHorse", this.hasChest());
        if (this.hasChest()) {
            WriteView.ListAppender<StackWithSlot> lv = view.getListAppender("Items", StackWithSlot.CODEC);
            for (int i = 0; i < this.items.size(); ++i) {
                ItemStack lv2 = this.items.getStack(i);
                if (lv2.isEmpty()) continue;
                lv.add(new StackWithSlot(i, lv2));
            }
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setHasChest(view.getBoolean("ChestedHorse", false));
        this.onChestedStatusChanged();
        if (this.hasChest()) {
            for (StackWithSlot lv : view.getTypedListView("Items", StackWithSlot.CODEC)) {
                if (!lv.isValidSlot(this.items.size())) continue;
                this.items.setStack(lv.slot(), lv.stack());
            }
        }
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        if (mappedIndex == 499) {
            return new StackReference(){

                @Override
                public ItemStack get() {
                    return AbstractDonkeyEntity.this.hasChest() ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
                }

                @Override
                public boolean set(ItemStack stack) {
                    if (stack.isEmpty()) {
                        if (AbstractDonkeyEntity.this.hasChest()) {
                            AbstractDonkeyEntity.this.setHasChest(false);
                            AbstractDonkeyEntity.this.onChestedStatusChanged();
                        }
                        return true;
                    }
                    if (stack.isOf(Items.CHEST)) {
                        if (!AbstractDonkeyEntity.this.hasChest()) {
                            AbstractDonkeyEntity.this.setHasChest(true);
                            AbstractDonkeyEntity.this.onChestedStatusChanged();
                        }
                        return true;
                    }
                    return false;
                }
            };
        }
        return super.getStackReference(mappedIndex);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        boolean bl;
        boolean bl2 = bl = !this.isBaby() && this.isTame() && player.shouldCancelInteraction();
        if (this.hasPassengers() || bl) {
            return super.interactMob(player, hand);
        }
        ItemStack lv = player.getStackInHand(hand);
        if (!lv.isEmpty()) {
            if (this.isBreedingItem(lv)) {
                return this.interactHorse(player, lv);
            }
            if (!this.isTame()) {
                this.playAngrySound();
                return ActionResult.SUCCESS;
            }
            if (!this.hasChest() && lv.isOf(Items.CHEST)) {
                this.addChest(player, lv);
                return ActionResult.SUCCESS;
            }
        }
        return super.interactMob(player, hand);
    }

    private void addChest(PlayerEntity player, ItemStack chest) {
        this.setHasChest(true);
        this.playAddChestSound();
        chest.decrementUnlessCreative(1, player);
        this.onChestedStatusChanged();
    }

    @Override
    public Vec3d[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.04, 0.41, 0.18, 0.73);
    }

    protected void playAddChestSound() {
        this.playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? 5 : 0;
    }
}

