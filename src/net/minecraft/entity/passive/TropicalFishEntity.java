/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class TropicalFishEntity
extends SchoolingFishEntity {
    public static final Variant DEFAULT_VARIANT = new Variant(Pattern.KOB, DyeColor.WHITE, DyeColor.WHITE);
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(TropicalFishEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final List<Variant> COMMON_VARIANTS = List.of(new Variant(Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), new Variant(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), new Variant(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), new Variant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY), new Variant(Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), new Variant(Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE), new Variant(Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), new Variant(Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), new Variant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED), new Variant(Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), new Variant(Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY), new Variant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), new Variant(Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK), new Variant(Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), new Variant(Pattern.BETTY, DyeColor.RED, DyeColor.WHITE), new Variant(Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED), new Variant(Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), new Variant(Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), new Variant(Pattern.KOB, DyeColor.RED, DyeColor.WHITE), new Variant(Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), new Variant(Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW), new Variant(Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW));
    private boolean commonSpawn = true;

    public TropicalFishEntity(EntityType<? extends TropicalFishEntity> arg, World arg2) {
        super((EntityType<? extends SchoolingFishEntity>)arg, arg2);
    }

    public static String getToolTipForVariant(int variant) {
        return "entity.minecraft.tropical_fish.predefined." + variant;
    }

    static int getVariantId(Pattern variety, DyeColor baseColor, DyeColor patternColor) {
        return variety.getIndex() & 0xFFFF | (baseColor.getIndex() & 0xFF) << 16 | (patternColor.getIndex() & 0xFF) << 24;
    }

    public static DyeColor getBaseColor(int variant) {
        return DyeColor.byIndex(variant >> 16 & 0xFF);
    }

    public static DyeColor getPatternColor(int variant) {
        return DyeColor.byIndex(variant >> 24 & 0xFF);
    }

    public static Pattern getVariety(int variant) {
        return Pattern.byIndex(variant & 0xFFFF);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, DEFAULT_VARIANT.getId());
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("Variant", Variant.CODEC, new Variant(this.getTropicalFishVariant()));
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        Variant lv = view.read("Variant", Variant.CODEC).orElse(DEFAULT_VARIANT);
        this.setTropicalFishVariant(lv.getId());
    }

    private void setTropicalFishVariant(int variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    @Override
    public boolean spawnsTooManyForEachTry(int count) {
        return !this.commonSpawn;
    }

    private int getTropicalFishVariant() {
        return this.dataTracker.get(VARIANT);
    }

    public DyeColor getBaseColor() {
        return TropicalFishEntity.getBaseColor(this.getTropicalFishVariant());
    }

    public DyeColor getPatternColor() {
        return TropicalFishEntity.getPatternColor(this.getTropicalFishVariant());
    }

    public Pattern getVariety() {
        return TropicalFishEntity.getVariety(this.getTropicalFishVariant());
    }

    private void setVariety(Pattern variety) {
        int i = this.getTropicalFishVariant();
        DyeColor lv = TropicalFishEntity.getBaseColor(i);
        DyeColor lv2 = TropicalFishEntity.getPatternColor(i);
        this.setTropicalFishVariant(TropicalFishEntity.getVariantId(variety, lv, lv2));
    }

    private void setBaseColor(DyeColor baseColor) {
        int i = this.getTropicalFishVariant();
        Pattern lv = TropicalFishEntity.getVariety(i);
        DyeColor lv2 = TropicalFishEntity.getPatternColor(i);
        this.setTropicalFishVariant(TropicalFishEntity.getVariantId(lv, baseColor, lv2));
    }

    private void setPatternColor(DyeColor patternColor) {
        int i = this.getTropicalFishVariant();
        Pattern lv = TropicalFishEntity.getVariety(i);
        DyeColor lv2 = TropicalFishEntity.getBaseColor(i);
        this.setTropicalFishVariant(TropicalFishEntity.getVariantId(lv, lv2, patternColor));
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.TROPICAL_FISH_PATTERN) {
            return TropicalFishEntity.castComponentValue(type, this.getVariety());
        }
        if (type == DataComponentTypes.TROPICAL_FISH_BASE_COLOR) {
            return TropicalFishEntity.castComponentValue(type, this.getBaseColor());
        }
        if (type == DataComponentTypes.TROPICAL_FISH_PATTERN_COLOR) {
            return TropicalFishEntity.castComponentValue(type, this.getPatternColor());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.TROPICAL_FISH_PATTERN);
        this.copyComponentFrom(from, DataComponentTypes.TROPICAL_FISH_BASE_COLOR);
        this.copyComponentFrom(from, DataComponentTypes.TROPICAL_FISH_PATTERN_COLOR);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.TROPICAL_FISH_PATTERN) {
            this.setVariety(TropicalFishEntity.castComponentValue(DataComponentTypes.TROPICAL_FISH_PATTERN, value));
            return true;
        }
        if (type == DataComponentTypes.TROPICAL_FISH_BASE_COLOR) {
            this.setBaseColor(TropicalFishEntity.castComponentValue(DataComponentTypes.TROPICAL_FISH_BASE_COLOR, value));
            return true;
        }
        if (type == DataComponentTypes.TROPICAL_FISH_PATTERN_COLOR) {
            this.setPatternColor(TropicalFishEntity.castComponentValue(DataComponentTypes.TROPICAL_FISH_PATTERN_COLOR, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    public void copyDataToStack(ItemStack stack) {
        super.copyDataToStack(stack);
        stack.copy(DataComponentTypes.TROPICAL_FISH_PATTERN, this);
        stack.copy(DataComponentTypes.TROPICAL_FISH_BASE_COLOR, this);
        stack.copy(DataComponentTypes.TROPICAL_FISH_PATTERN_COLOR, this);
    }

    @Override
    public ItemStack getBucketItem() {
        return new ItemStack(Items.TROPICAL_FISH_BUCKET);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_TROPICAL_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_FLOP;
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Variant lv3;
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        Random lv = world.getRandom();
        if (entityData instanceof TropicalFishData) {
            TropicalFishData lv2 = (TropicalFishData)entityData;
            lv3 = lv2.variant;
        } else if ((double)lv.nextFloat() < 0.9) {
            lv3 = Util.getRandom(COMMON_VARIANTS, lv);
            entityData = new TropicalFishData(this, lv3);
        } else {
            this.commonSpawn = false;
            Pattern[] lvs = Pattern.values();
            DyeColor[] lvs2 = DyeColor.values();
            Pattern lv4 = Util.getRandom(lvs, lv);
            DyeColor lv5 = Util.getRandom(lvs2, lv);
            DyeColor lv6 = Util.getRandom(lvs2, lv);
            lv3 = new Variant(lv4, lv5, lv6);
        }
        this.setTropicalFishVariant(lv3.getId());
        return entityData;
    }

    public static boolean canTropicalFishSpawn(EntityType<TropicalFishEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return world.getFluidState(pos.down()).isIn(FluidTags.WATER) && world.getBlockState(pos.up()).isOf(Blocks.WATER) && (world.getBiome(pos).isIn(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT) || WaterCreatureEntity.canSpawn(type, world, reason, pos, random));
    }

    public static enum Pattern implements StringIdentifiable,
    TooltipAppender
    {
        KOB("kob", Size.SMALL, 0),
        SUNSTREAK("sunstreak", Size.SMALL, 1),
        SNOOPER("snooper", Size.SMALL, 2),
        DASHER("dasher", Size.SMALL, 3),
        BRINELY("brinely", Size.SMALL, 4),
        SPOTTY("spotty", Size.SMALL, 5),
        FLOPPER("flopper", Size.LARGE, 0),
        STRIPEY("stripey", Size.LARGE, 1),
        GLITTER("glitter", Size.LARGE, 2),
        BLOCKFISH("blockfish", Size.LARGE, 3),
        BETTY("betty", Size.LARGE, 4),
        CLAYFISH("clayfish", Size.LARGE, 5);

        public static final Codec<Pattern> CODEC;
        private static final IntFunction<Pattern> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, Pattern> PACKET_CODEC;
        private final String id;
        private final Text text;
        private final Size size;
        private final int index;

        private Pattern(String id, Size size, int index) {
            this.id = id;
            this.size = size;
            this.index = size.index | index << 8;
            this.text = Text.translatable("entity.minecraft.tropical_fish.type." + this.id);
        }

        public static Pattern byIndex(int index) {
            return INDEX_MAPPER.apply(index);
        }

        public Size getSize() {
            return this.size;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public Text getText() {
            return this.text;
        }

        @Override
        public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
            DyeColor lv = components.getOrDefault(DataComponentTypes.TROPICAL_FISH_BASE_COLOR, DEFAULT_VARIANT.baseColor());
            DyeColor lv2 = components.getOrDefault(DataComponentTypes.TROPICAL_FISH_PATTERN_COLOR, DEFAULT_VARIANT.patternColor());
            Formatting[] lvs = new Formatting[]{Formatting.ITALIC, Formatting.GRAY};
            int i = COMMON_VARIANTS.indexOf(new Variant(this, lv, lv2));
            if (i != -1) {
                textConsumer.accept(Text.translatable(TropicalFishEntity.getToolTipForVariant(i)).formatted(lvs));
                return;
            }
            textConsumer.accept(this.text.copyContentOnly().formatted(lvs));
            MutableText lv3 = Text.translatable("color.minecraft." + lv.getId());
            if (lv != lv2) {
                lv3.append(", ").append(Text.translatable("color.minecraft." + lv2.getId()));
            }
            lv3.formatted(lvs);
            textConsumer.accept(lv3);
        }

        static {
            CODEC = StringIdentifiable.createCodec(Pattern::values);
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(Pattern::getIndex, Pattern.values(), KOB);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, Pattern::getIndex);
        }
    }

    public record Variant(Pattern pattern, DyeColor baseColor, DyeColor patternColor) {
        public static final Codec<Variant> CODEC = Codec.INT.xmap(Variant::new, Variant::getId);

        public Variant(int id) {
            this(TropicalFishEntity.getVariety(id), TropicalFishEntity.getBaseColor(id), TropicalFishEntity.getPatternColor(id));
        }

        public int getId() {
            return TropicalFishEntity.getVariantId(this.pattern, this.baseColor, this.patternColor);
        }
    }

    static class TropicalFishData
    extends SchoolingFishEntity.FishData {
        final Variant variant;

        TropicalFishData(TropicalFishEntity leader, Variant variant) {
            super(leader);
            this.variant = variant;
        }
    }

    public static enum Size {
        SMALL(0),
        LARGE(1);

        final int index;

        private Size(int index) {
            this.index = index;
        }
    }
}

