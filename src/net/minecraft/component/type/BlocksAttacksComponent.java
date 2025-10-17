/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public record BlocksAttacksComponent(float blockDelaySeconds, float disableCooldownScale, List<DamageReduction> damageReductions, ItemDamage itemDamage, Optional<TagKey<DamageType>> bypassedBy, Optional<RegistryEntry<SoundEvent>> blockSound, Optional<RegistryEntry<SoundEvent>> disableSound) {
    public static final Codec<BlocksAttacksComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("block_delay_seconds", Float.valueOf(0.0f)).forGetter(BlocksAttacksComponent::blockDelaySeconds), Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("disable_cooldown_scale", Float.valueOf(1.0f)).forGetter(BlocksAttacksComponent::disableCooldownScale), DamageReduction.CODEC.listOf().optionalFieldOf("damage_reductions", List.of(new DamageReduction(90.0f, Optional.empty(), 0.0f, 1.0f))).forGetter(BlocksAttacksComponent::damageReductions), ItemDamage.CODEC.optionalFieldOf("item_damage", ItemDamage.DEFAULT).forGetter(BlocksAttacksComponent::itemDamage), TagKey.codec(RegistryKeys.DAMAGE_TYPE).optionalFieldOf("bypassed_by").forGetter(BlocksAttacksComponent::bypassedBy), SoundEvent.ENTRY_CODEC.optionalFieldOf("block_sound").forGetter(BlocksAttacksComponent::blockSound), SoundEvent.ENTRY_CODEC.optionalFieldOf("disabled_sound").forGetter(BlocksAttacksComponent::disableSound)).apply((Applicative<BlocksAttacksComponent, ?>)instance, BlocksAttacksComponent::new));
    public static final PacketCodec<RegistryByteBuf, BlocksAttacksComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, BlocksAttacksComponent::blockDelaySeconds, PacketCodecs.FLOAT, BlocksAttacksComponent::disableCooldownScale, DamageReduction.PACKET_CODEC.collect(PacketCodecs.toList()), BlocksAttacksComponent::damageReductions, ItemDamage.PACKET_CODEC, BlocksAttacksComponent::itemDamage, TagKey.packetCodec(RegistryKeys.DAMAGE_TYPE).collect(PacketCodecs::optional), BlocksAttacksComponent::bypassedBy, SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional), BlocksAttacksComponent::blockSound, SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional), BlocksAttacksComponent::disableSound, BlocksAttacksComponent::new);

    public void playBlockSound(ServerWorld world, LivingEntity from) {
        this.blockSound.ifPresent(sound -> world.playSound(null, from.getX(), from.getY(), from.getZ(), (RegistryEntry<SoundEvent>)sound, from.getSoundCategory(), 1.0f, 0.8f + arg.random.nextFloat() * 0.4f));
    }

    public void applyShieldCooldown(ServerWorld world, LivingEntity affectedEntity, float cooldownSeconds, ItemStack stack) {
        int i = this.convertCooldownToTicks(cooldownSeconds);
        if (i > 0) {
            if (affectedEntity instanceof PlayerEntity) {
                PlayerEntity lv = (PlayerEntity)affectedEntity;
                lv.getItemCooldownManager().set(stack, i);
            }
            affectedEntity.clearActiveItem();
            this.disableSound.ifPresent(sound -> world.playSound(null, affectedEntity.getX(), affectedEntity.getY(), affectedEntity.getZ(), (RegistryEntry<SoundEvent>)sound, affectedEntity.getSoundCategory(), 0.8f, 0.8f + arg.random.nextFloat() * 0.4f));
        }
    }

    public void onShieldHit(World world, ItemStack stack, LivingEntity entity, Hand hand, float itemDamage) {
        int i;
        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity lv = (PlayerEntity)entity;
        if (!world.isClient()) {
            lv.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        }
        if ((i = this.itemDamage.calculate(itemDamage)) > 0) {
            stack.damage(i, entity, hand.getEquipmentSlot());
        }
    }

    private int convertCooldownToTicks(float cooldownSeconds) {
        float g = cooldownSeconds * this.disableCooldownScale;
        if (g > 0.0f) {
            return Math.round(g * 20.0f);
        }
        return 0;
    }

    public int getBlockDelayTicks() {
        return Math.round(this.blockDelaySeconds * 20.0f);
    }

    public float getDamageReductionAmount(DamageSource source, float damage, double angle) {
        float g = 0.0f;
        for (DamageReduction lv : this.damageReductions) {
            g += lv.getReductionAmount(source, damage, angle);
        }
        return MathHelper.clamp(g, 0.0f, damage);
    }

    public record ItemDamage(float threshold, float base, float factor) {
        public static final Codec<ItemDamage> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.NON_NEGATIVE_FLOAT.fieldOf("threshold")).forGetter(ItemDamage::threshold), ((MapCodec)Codec.FLOAT.fieldOf("base")).forGetter(ItemDamage::base), ((MapCodec)Codec.FLOAT.fieldOf("factor")).forGetter(ItemDamage::factor)).apply((Applicative<ItemDamage, ?>)instance, ItemDamage::new));
        public static final PacketCodec<ByteBuf, ItemDamage> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, ItemDamage::threshold, PacketCodecs.FLOAT, ItemDamage::base, PacketCodecs.FLOAT, ItemDamage::factor, ItemDamage::new);
        public static final ItemDamage DEFAULT = new ItemDamage(1.0f, 0.0f, 1.0f);

        public int calculate(float itemDamage) {
            if (itemDamage < this.threshold) {
                return 0;
            }
            return MathHelper.floor(this.base + this.factor * itemDamage);
        }
    }

    public record DamageReduction(float horizontalBlockingAngle, Optional<RegistryEntryList<DamageType>> type, float base, float factor) {
        public static final Codec<DamageReduction> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.POSITIVE_FLOAT.optionalFieldOf("horizontal_blocking_angle", Float.valueOf(90.0f)).forGetter(DamageReduction::horizontalBlockingAngle), RegistryCodecs.entryList(RegistryKeys.DAMAGE_TYPE).optionalFieldOf("type").forGetter(DamageReduction::type), ((MapCodec)Codec.FLOAT.fieldOf("base")).forGetter(DamageReduction::base), ((MapCodec)Codec.FLOAT.fieldOf("factor")).forGetter(DamageReduction::factor)).apply((Applicative<DamageReduction, ?>)instance, DamageReduction::new));
        public static final PacketCodec<RegistryByteBuf, DamageReduction> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, DamageReduction::horizontalBlockingAngle, PacketCodecs.registryEntryList(RegistryKeys.DAMAGE_TYPE).collect(PacketCodecs::optional), DamageReduction::type, PacketCodecs.FLOAT, DamageReduction::base, PacketCodecs.FLOAT, DamageReduction::factor, DamageReduction::new);

        public float getReductionAmount(DamageSource source, float damage, double angle) {
            if (angle > (double)((float)Math.PI / 180 * this.horizontalBlockingAngle)) {
                return 0.0f;
            }
            if (this.type.isPresent() && !this.type.get().contains(source.getTypeRegistryEntry())) {
                return 0.0f;
            }
            return MathHelper.clamp(this.base + this.factor * damage, 0.0f, damage);
        }
    }
}

