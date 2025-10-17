/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

public class ItemCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, BlockPos pos, ItemStack stack) {
        ServerWorld lv = player.getEntityWorld();
        BlockState lv2 = lv.getBlockState(pos);
        LootWorldContext lv3 = new LootWorldContext.Builder(lv).add(LootContextParameters.ORIGIN, pos.toCenterPos()).add(LootContextParameters.THIS_ENTITY, player).add(LootContextParameters.BLOCK_STATE, lv2).add(LootContextParameters.TOOL, stack).build(LootContextTypes.ADVANCEMENT_LOCATION);
        LootContext lv4 = new LootContext.Builder(lv3).build(Optional.empty());
        this.trigger(player, conditions -> conditions.test(lv4));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<LootContextPredicate> location) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), LootContextPredicate.CODEC.optionalFieldOf("location").forGetter(Conditions::location)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> createPlacedBlock(Block block) {
            LootContextPredicate lv = LootContextPredicate.create(BlockStatePropertyLootCondition.builder(block).build());
            return Criteria.PLACED_BLOCK.create(new Conditions(Optional.empty(), Optional.of(lv)));
        }

        public static AdvancementCriterion<Conditions> createPlacedBlock(LootCondition.Builder ... locationConditions) {
            LootContextPredicate lv = LootContextPredicate.create((LootCondition[])Arrays.stream(locationConditions).map(LootCondition.Builder::build).toArray(LootCondition[]::new));
            return Criteria.PLACED_BLOCK.create(new Conditions(Optional.empty(), Optional.of(lv)));
        }

        public static <T extends Comparable<T>> AdvancementCriterion<Conditions> createPlacedWithState(Block block, Property<T> property, String value) {
            StatePredicate.Builder lv = StatePredicate.Builder.create().exactMatch(property, value);
            LootContextPredicate lv2 = LootContextPredicate.create(BlockStatePropertyLootCondition.builder(block).properties(lv).build());
            return Criteria.PLACED_BLOCK.create(new Conditions(Optional.empty(), Optional.of(lv2)));
        }

        public static AdvancementCriterion<Conditions> createPlacedWithState(Block block, Property<Boolean> property, boolean value) {
            return Conditions.createPlacedWithState(block, property, String.valueOf(value));
        }

        public static AdvancementCriterion<Conditions> createPlacedWithState(Block block, Property<Integer> property, int value) {
            return Conditions.createPlacedWithState(block, property, String.valueOf(value));
        }

        public static <T extends Comparable<T> & StringIdentifiable> AdvancementCriterion<Conditions> createPlacedWithState(Block block, Property<T> property, T value) {
            return Conditions.createPlacedWithState(block, property, ((StringIdentifiable)value).asString());
        }

        private static Conditions create(LocationPredicate.Builder location, ItemPredicate.Builder item) {
            LootContextPredicate lv = LootContextPredicate.create(LocationCheckLootCondition.builder(location).build(), MatchToolLootCondition.builder(item).build());
            return new Conditions(Optional.empty(), Optional.of(lv));
        }

        public static AdvancementCriterion<Conditions> createItemUsedOnBlock(LocationPredicate.Builder location, ItemPredicate.Builder item) {
            return Criteria.ITEM_USED_ON_BLOCK.create(Conditions.create(location, item));
        }

        public static AdvancementCriterion<Conditions> createAllayDropItemOnBlock(LocationPredicate.Builder location, ItemPredicate.Builder item) {
            return Criteria.ALLAY_DROP_ITEM_ON_BLOCK.create(Conditions.create(location, item));
        }

        public boolean test(LootContext location) {
            return this.location.isEmpty() || this.location.get().test(location);
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            this.location.ifPresent(location -> validator.validate((LootContextPredicate)location, LootContextTypes.ADVANCEMENT_LOCATION, "location"));
        }
    }
}

