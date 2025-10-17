/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TridentItem
extends Item
implements ProjectileItem {
    public static final int MIN_DRAW_DURATION = 10;
    public static final float ATTACK_DAMAGE = 8.0f;
    public static final float THROW_SPEED = 2.5f;

    public TridentItem(Item.Settings arg) {
        super(arg);
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder().add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 8.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.9f, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).build();
    }

    public static ToolComponent createToolComponent() {
        return new ToolComponent(List.of(), 1.0f, 2, false);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity lv = (PlayerEntity)user;
        int j = this.getMaxUseTime(stack, user) - remainingUseTicks;
        if (j < 10) {
            return false;
        }
        float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, lv);
        if (f > 0.0f && !lv.isTouchingWaterOrRain()) {
            return false;
        }
        if (stack.willBreakNextUse()) {
            return false;
        }
        RegistryEntry<SoundEvent> lv2 = EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.TRIDENT_SOUND).orElse(SoundEvents.ITEM_TRIDENT_THROW);
        lv.incrementStat(Stats.USED.getOrCreateStat(this));
        if (world instanceof ServerWorld) {
            ServerWorld lv3 = (ServerWorld)world;
            stack.damage(1, lv);
            if (f == 0.0f) {
                ItemStack lv4 = stack.splitUnlessCreative(1, lv);
                TridentEntity lv5 = ProjectileEntity.spawnWithVelocity(TridentEntity::new, lv3, lv4, lv, 0.0f, 2.5f, 1.0f);
                if (lv.isInCreativeMode()) {
                    lv5.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                }
                world.playSoundFromEntity(null, lv5, lv2.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                return true;
            }
        }
        if (f > 0.0f) {
            float g = lv.getYaw();
            float h = lv.getPitch();
            float k = -MathHelper.sin(g * ((float)Math.PI / 180)) * MathHelper.cos(h * ((float)Math.PI / 180));
            float l = -MathHelper.sin(h * ((float)Math.PI / 180));
            float m = MathHelper.cos(g * ((float)Math.PI / 180)) * MathHelper.cos(h * ((float)Math.PI / 180));
            float n = MathHelper.sqrt(k * k + l * l + m * m);
            lv.addVelocity(k *= f / n, l *= f / n, m *= f / n);
            lv.useRiptide(20, 8.0f, stack);
            if (lv.isOnGround()) {
                float o = 1.1999999f;
                lv.move(MovementType.SELF, new Vec3d(0.0, 1.1999999284744263, 0.0));
            }
            world.playSoundFromEntity(null, lv, lv2.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        if (lv.willBreakNextUse()) {
            return ActionResult.FAIL;
        }
        if (EnchantmentHelper.getTridentSpinAttackStrength(lv, user) > 0.0f && !user.isTouchingWaterOrRain()) {
            return ActionResult.FAIL;
        }
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        TridentEntity lv = new TridentEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copyWithCount(1));
        lv.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
        return lv;
    }
}

