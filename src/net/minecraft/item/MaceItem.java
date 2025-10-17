/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class MaceItem
extends Item {
    private static final int ATTACK_DAMAGE_MODIFIER_VALUE = 5;
    private static final float ATTACK_SPEED_MODIFIER_VALUE = -3.4f;
    public static final float MINING_SPEED_MULTIPLIER = 1.5f;
    private static final float HEAVY_SMASH_SOUND_FALL_DISTANCE_THRESHOLD = 5.0f;
    public static final float KNOCKBACK_RANGE = 3.5f;
    private static final float KNOCKBACK_POWER = 0.7f;

    public MaceItem(Item.Settings arg) {
        super(arg);
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder().add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 5.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3.4f, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).build();
    }

    public static ToolComponent createToolComponent() {
        return new ToolComponent(List.of(), 1.0f, 2, false);
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (MaceItem.shouldDealAdditionalDamage(attacker)) {
            ServerPlayerEntity lv2;
            ServerWorld lv = (ServerWorld)attacker.getEntityWorld();
            attacker.setVelocity(attacker.getVelocity().withAxis(Direction.Axis.Y, 0.01f));
            if (attacker instanceof ServerPlayerEntity) {
                lv2 = (ServerPlayerEntity)attacker;
                lv2.currentExplosionImpactPos = this.getCurrentExplosionImpactPos(lv2);
                lv2.setIgnoreFallDamageFromCurrentExplosion(true);
                lv2.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(lv2));
            }
            if (target.isOnGround()) {
                if (attacker instanceof ServerPlayerEntity) {
                    lv2 = (ServerPlayerEntity)attacker;
                    lv2.setSpawnExtraParticlesOnFall(true);
                }
                SoundEvent lv3 = attacker.fallDistance > 5.0 ? SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY : SoundEvents.ITEM_MACE_SMASH_GROUND;
                lv.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), lv3, attacker.getSoundCategory(), 1.0f, 1.0f);
            } else {
                lv.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ITEM_MACE_SMASH_AIR, attacker.getSoundCategory(), 1.0f, 1.0f);
            }
            MaceItem.knockbackNearbyEntities(lv, attacker, target);
        }
    }

    private Vec3d getCurrentExplosionImpactPos(ServerPlayerEntity player) {
        if (player.shouldIgnoreFallDamageFromCurrentExplosion() && player.currentExplosionImpactPos != null && player.currentExplosionImpactPos.y <= player.getEntityPos().y) {
            return player.currentExplosionImpactPos;
        }
        return player.getEntityPos();
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (MaceItem.shouldDealAdditionalDamage(attacker)) {
            attacker.onLanding();
        }
    }

    @Override
    public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource) {
        Entity entity = damageSource.getSource();
        if (!(entity instanceof LivingEntity)) {
            return 0.0f;
        }
        LivingEntity lv = (LivingEntity)entity;
        if (!MaceItem.shouldDealAdditionalDamage(lv)) {
            return 0.0f;
        }
        double d = 3.0;
        double e = 8.0;
        double g = lv.fallDistance;
        double h = g <= 3.0 ? 4.0 * g : (g <= 8.0 ? 12.0 + 2.0 * (g - 3.0) : 22.0 + g - 8.0);
        World world = lv.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            return (float)(h + (double)EnchantmentHelper.getSmashDamagePerFallenBlock(lv2, lv.getWeaponStack(), target, damageSource, 0.0f) * g);
        }
        return (float)h;
    }

    private static void knockbackNearbyEntities(World world, Entity attacker, Entity attacked) {
        world.syncWorldEvent(WorldEvents.SMASH_ATTACK, attacked.getSteppingPos(), 750);
        world.getEntitiesByClass(LivingEntity.class, attacked.getBoundingBox().expand(3.5), MaceItem.getKnockbackPredicate(attacker, attacked)).forEach(entity -> {
            Vec3d lv = entity.getEntityPos().subtract(attacked.getEntityPos());
            double d = MaceItem.getKnockback(attacker, entity, lv);
            Vec3d lv2 = lv.normalize().multiply(d);
            if (d > 0.0) {
                entity.addVelocity(lv2.x, 0.7f, lv2.z);
                if (entity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity lv3 = (ServerPlayerEntity)entity;
                    lv3.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(lv3));
                }
            }
        });
    }

    private static Predicate<LivingEntity> getKnockbackPredicate(Entity attacker, Entity attacked) {
        return arg_0 -> MaceItem.method_58661(attacker, attacked, arg_0);
    }

    private static double getKnockback(Entity attacker, LivingEntity attacked, Vec3d distance) {
        return (3.5 - distance.length()) * (double)0.7f * (double)(attacker.fallDistance > 5.0 ? 2 : 1) * (1.0 - attacked.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE));
    }

    public static boolean shouldDealAdditionalDamage(LivingEntity attacker) {
        return attacker.fallDistance > 1.5 && !attacker.isGliding();
    }

    @Override
    @Nullable
    public DamageSource getDamageSource(LivingEntity user) {
        if (MaceItem.shouldDealAdditionalDamage(user)) {
            return user.getDamageSources().maceSmash(user);
        }
        return super.getDamageSource(user);
    }

    /*
     * Unable to fully structure code
     */
    private static /* synthetic */ boolean method_58661(Entity arg, Entity arg2, LivingEntity entity) {
        bl = entity.isSpectator() == false;
        bl2 = entity != arg && entity != arg2;
        v0 = bl3 = arg.isTeammate(entity) == false;
        if (!(entity instanceof TameableEntity)) ** GOTO lbl-1000
        lv = (TameableEntity)entity;
        if (!(arg2 instanceof LivingEntity)) ** GOTO lbl-1000
        lv2 = (LivingEntity)arg2;
        if (lv.isTamed() && lv.isOwner(lv2)) {
            v1 = true;
        } else lbl-1000:
        // 3 sources

        {
            v1 = false;
        }
        bl4 = v1 == false;
        bl5 = entity instanceof ArmorStandEntity == false || (lv3 = (ArmorStandEntity)entity).isMarker() == false;
        bl6 = arg2.squaredDistanceTo(entity) <= Math.pow(3.5, 2.0);
        return bl != false && bl2 != false && bl3 != false && bl4 != false && bl5 != false && bl6 != false;
    }
}

