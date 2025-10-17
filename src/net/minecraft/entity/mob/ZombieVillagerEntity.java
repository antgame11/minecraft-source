/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerGossips;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class ZombieVillagerEntity
extends ZombieEntity
implements VillagerDataContainer {
    private static final TrackedData<Boolean> CONVERTING = DataTracker.registerData(ZombieVillagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<VillagerData> VILLAGER_DATA = DataTracker.registerData(ZombieVillagerEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
    private static final int BASE_CONVERSION_DELAY = 3600;
    private static final int field_30520 = 6000;
    private static final int field_30521 = 14;
    private static final int field_30522 = 4;
    private static final int DEFAULT_CONVERSION_TIME = -1;
    private static final int DEFAULT_EXPERIENCE = 0;
    private int conversionTimer;
    @Nullable
    private UUID converter;
    @Nullable
    private VillagerGossips gossip;
    @Nullable
    private TradeOfferList offerData;
    private int experience = 0;

    public ZombieVillagerEntity(EntityType<? extends ZombieVillagerEntity> arg, World arg2) {
        super((EntityType<? extends ZombieEntity>)arg, arg2);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CONVERTING, false);
        builder.add(VILLAGER_DATA, this.createVillagerData());
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("VillagerData", VillagerData.CODEC, this.getVillagerData());
        view.putNullable("Offers", TradeOfferList.CODEC, this.offerData);
        view.putNullable("Gossips", VillagerGossips.CODEC, this.gossip);
        view.putInt("ConversionTime", this.isConverting() ? this.conversionTimer : -1);
        view.putNullable("ConversionPlayer", Uuids.INT_STREAM_CODEC, this.converter);
        view.putInt("Xp", this.experience);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.dataTracker.set(VILLAGER_DATA, view.read("VillagerData", VillagerData.CODEC).orElseGet(this::createVillagerData));
        this.offerData = view.read("Offers", TradeOfferList.CODEC).orElse(null);
        this.gossip = view.read("Gossips", VillagerGossips.CODEC).orElse(null);
        int i = view.getInt("ConversionTime", -1);
        if (i != -1) {
            UUID uUID = view.read("ConversionPlayer", Uuids.INT_STREAM_CODEC).orElse(null);
            this.setConverting(uUID, i);
        } else {
            this.getDataTracker().set(CONVERTING, false);
            this.conversionTimer = -1;
        }
        this.experience = view.getInt("Xp", 0);
    }

    private VillagerData createVillagerData() {
        World lv = this.getEntityWorld();
        Optional optional = Registries.VILLAGER_PROFESSION.getRandom(this.random);
        VillagerData lv2 = VillagerEntity.createVillagerData().withType(lv.getRegistryManager(), VillagerType.forBiome(lv.getBiome(this.getBlockPos())));
        if (optional.isPresent()) {
            lv2 = lv2.withProfession(optional.get());
        }
        return lv2;
    }

    @Override
    public void tick() {
        if (!this.getEntityWorld().isClient() && this.isAlive() && this.isConverting()) {
            int i = this.getConversionRate();
            this.conversionTimer -= i;
            if (this.conversionTimer <= 0) {
                this.finishConversion((ServerWorld)this.getEntityWorld());
            }
        }
        super.tick();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (lv.isOf(Items.GOLDEN_APPLE)) {
            if (this.hasStatusEffect(StatusEffects.WEAKNESS)) {
                lv.decrementUnlessCreative(1, player);
                if (!this.getEntityWorld().isClient()) {
                    this.setConverting(player.getUuid(), this.random.nextInt(2401) + 3600);
                }
                return ActionResult.SUCCESS_SERVER;
            }
            return ActionResult.CONSUME;
        }
        return super.interactMob(player, hand);
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.isConverting() && this.experience == 0;
    }

    public boolean isConverting() {
        return this.getDataTracker().get(CONVERTING);
    }

    private void setConverting(@Nullable UUID uuid, int delay) {
        this.converter = uuid;
        this.conversionTimer = delay;
        this.getDataTracker().set(CONVERTING, true);
        this.removeStatusEffect(StatusEffects.WEAKNESS);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, delay, Math.min(this.getEntityWorld().getDifficulty().getId() - 1, 0)));
        this.getEntityWorld().sendEntityStatus(this, EntityStatuses.PLAY_CURE_ZOMBIE_VILLAGER_SOUND);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PLAY_CURE_ZOMBIE_VILLAGER_SOUND) {
            if (!this.isSilent()) {
                this.getEntityWorld().playSoundClient(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, this.getSoundCategory(), 1.0f + this.random.nextFloat(), this.random.nextFloat() * 0.7f + 0.3f, false);
            }
            return;
        }
        super.handleStatus(status);
    }

    private void finishConversion(ServerWorld world) {
        this.convertTo(EntityType.VILLAGER, EntityConversionContext.create(this, false, false), villager -> {
            PlayerEntity lv3;
            for (EquipmentSlot lv : this.dropForeignEquipment(world, stack -> !EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE))) {
                StackReference lv2 = villager.getStackReference(lv.getEntitySlotId() + 300);
                lv2.set(this.getEquippedStack(lv));
            }
            villager.setVillagerData(this.getVillagerData());
            if (this.gossip != null) {
                villager.readGossipData(this.gossip);
            }
            if (this.offerData != null) {
                villager.setOffers(this.offerData.copy());
            }
            villager.setExperience(this.experience);
            villager.initialize(world, world.getLocalDifficulty(villager.getBlockPos()), SpawnReason.CONVERSION, null);
            villager.reinitializeBrain(world);
            if (this.converter != null && (lv3 = world.getPlayerByUuid(this.converter)) instanceof ServerPlayerEntity) {
                Criteria.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayerEntity)lv3, this, (VillagerEntity)villager);
                world.handleInteraction(EntityInteraction.ZOMBIE_VILLAGER_CURED, lv3, (InteractionObserver)((Object)villager));
            }
            villager.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
            if (!this.isSilent()) {
                world.syncWorldEvent(null, WorldEvents.ZOMBIE_VILLAGER_CURED, this.getBlockPos(), 0);
            }
        });
    }

    @VisibleForTesting
    public void setConversionTimer(int conversionTimer) {
        this.conversionTimer = conversionTimer;
    }

    private int getConversionRate() {
        int i = 1;
        if (this.random.nextFloat() < 0.01f) {
            int j = 0;
            BlockPos.Mutable lv = new BlockPos.Mutable();
            for (int k = (int)this.getX() - 4; k < (int)this.getX() + 4 && j < 14; ++k) {
                for (int l = (int)this.getY() - 4; l < (int)this.getY() + 4 && j < 14; ++l) {
                    for (int m = (int)this.getZ() - 4; m < (int)this.getZ() + 4 && j < 14; ++m) {
                        BlockState lv2 = this.getEntityWorld().getBlockState(lv.set(k, l, m));
                        if (!lv2.isOf(Blocks.IRON_BARS) && !(lv2.getBlock() instanceof BedBlock)) continue;
                        if (this.random.nextFloat() < 0.3f) {
                            ++i;
                        }
                        ++j;
                    }
                }
            }
        }
        return i;
    }

    @Override
    public float getSoundPitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 2.0f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    public SoundEvent getStepSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP;
    }

    public void setOfferData(TradeOfferList offerData) {
        this.offerData = offerData;
    }

    public void setGossip(VillagerGossips gossip) {
        this.gossip = gossip;
    }

    @Override
    public void setVillagerData(VillagerData villagerData) {
        VillagerData lv = this.getVillagerData();
        if (!lv.profession().equals(villagerData.profession())) {
            this.offerData = null;
        }
        this.dataTracker.set(VILLAGER_DATA, villagerData);
    }

    @Override
    public VillagerData getVillagerData() {
        return this.dataTracker.get(VILLAGER_DATA);
    }

    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.VILLAGER_VARIANT) {
            return ZombieVillagerEntity.castComponentValue(type, this.getVillagerData().type());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.VILLAGER_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.VILLAGER_VARIANT) {
            RegistryEntry<VillagerType> lv = ZombieVillagerEntity.castComponentValue(DataComponentTypes.VILLAGER_VARIANT, value);
            this.setVillagerData(this.getVillagerData().withType(lv));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }
}

