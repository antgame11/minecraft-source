/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BeesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.debug.data.BeeHiveDebugData;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BeehiveBlockEntity
extends BlockEntity {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String FLOWER_POS_KEY = "flower_pos";
    private static final String BEES_KEY = "bees";
    static final List<String> IRRELEVANT_BEE_NBT_KEYS = Arrays.asList("Air", "drop_chances", "equipment", "Brain", "CanPickUpLoot", "DeathTime", "fall_distance", "FallFlying", "Fire", "HurtByTimestamp", "HurtTime", "LeftHanded", "Motion", "NoGravity", "OnGround", "PortalCooldown", "Pos", "Rotation", "sleeping_pos", "CannotEnterHiveTicks", "TicksSincePollination", "CropsGrownSincePollination", "hive_pos", "Passengers", "leash", "UUID");
    public static final int MAX_BEE_COUNT = 3;
    private static final int ANGERED_CANNOT_ENTER_HIVE_TICKS = 400;
    private static final int MIN_OCCUPATION_TICKS_WITH_NECTAR = 2400;
    public static final int MIN_OCCUPATION_TICKS_WITHOUT_NECTAR = 600;
    private final List<Bee> bees = Lists.newArrayList();
    @Nullable
    private BlockPos flowerPos;

    public BeehiveBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.BEEHIVE, pos, state);
    }

    @Override
    public void markDirty() {
        if (this.isNearFire()) {
            this.angerBees(null, this.world.getBlockState(this.getPos()), BeeState.EMERGENCY);
        }
        super.markDirty();
    }

    public boolean isNearFire() {
        if (this.world == null) {
            return false;
        }
        for (BlockPos lv : BlockPos.iterate(this.pos.add(-1, -1, -1), this.pos.add(1, 1, 1))) {
            if (!(this.world.getBlockState(lv).getBlock() instanceof FireBlock)) continue;
            return true;
        }
        return false;
    }

    public boolean hasNoBees() {
        return this.bees.isEmpty();
    }

    public boolean isFullOfBees() {
        return this.bees.size() == 3;
    }

    public void angerBees(@Nullable PlayerEntity player, BlockState state, BeeState beeState) {
        List<Entity> list = this.tryReleaseBee(state, beeState);
        if (player != null) {
            for (Entity lv : list) {
                if (!(lv instanceof BeeEntity)) continue;
                BeeEntity lv2 = (BeeEntity)lv;
                if (!(player.getEntityPos().squaredDistanceTo(lv.getEntityPos()) <= 16.0)) continue;
                if (!this.isSmoked()) {
                    lv2.setTarget(player);
                    continue;
                }
                lv2.setCannotEnterHiveTicks(400);
            }
        }
    }

    private List<Entity> tryReleaseBee(BlockState state, BeeState beeState) {
        ArrayList<Entity> list = Lists.newArrayList();
        this.bees.removeIf(bee -> BeehiveBlockEntity.releaseBee(this.world, this.pos, state, bee.createData(), list, beeState, this.flowerPos));
        if (!list.isEmpty()) {
            super.markDirty();
        }
        return list;
    }

    @Debug
    public int getBeeCount() {
        return this.bees.size();
    }

    public static int getHoneyLevel(BlockState state) {
        return state.get(BeehiveBlock.HONEY_LEVEL);
    }

    @Debug
    public boolean isSmoked() {
        return CampfireBlock.isLitCampfireInRange(this.world, this.getPos());
    }

    public void tryEnterHive(BeeEntity entity) {
        if (this.bees.size() >= 3) {
            return;
        }
        entity.stopRiding();
        entity.removeAllPassengers();
        entity.detachLeash();
        this.addBee(BeeData.of(entity));
        if (this.world != null) {
            if (entity.hasFlower() && (!this.hasFlowerPos() || this.world.random.nextBoolean())) {
                this.flowerPos = entity.getFlowerPos();
            }
            BlockPos lv = this.getPos();
            this.world.playSound(null, (double)lv.getX(), (double)lv.getY(), (double)lv.getZ(), SoundEvents.BLOCK_BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0f, 1.0f);
            this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, lv, GameEvent.Emitter.of(entity, this.getCachedState()));
        }
        entity.discard();
        super.markDirty();
    }

    public void addBee(BeeData bee) {
        this.bees.add(new Bee(bee));
    }

    private static boolean releaseBee(World world, BlockPos pos, BlockState state, BeeData bee, @Nullable List<Entity> entities, BeeState beeState, @Nullable BlockPos flowerPos) {
        boolean bl;
        if (BeeEntity.isNightOrRaining(world) && beeState != BeeState.EMERGENCY) {
            return false;
        }
        Direction lv = state.get(BeehiveBlock.FACING);
        BlockPos lv2 = pos.offset(lv);
        boolean bl2 = bl = !world.getBlockState(lv2).getCollisionShape(world, lv2).isEmpty();
        if (bl && beeState != BeeState.EMERGENCY) {
            return false;
        }
        Entity lv3 = bee.loadEntity(world, pos);
        if (lv3 != null) {
            if (lv3 instanceof BeeEntity) {
                BeeEntity lv4 = (BeeEntity)lv3;
                if (flowerPos != null && !lv4.hasFlower() && world.random.nextFloat() < 0.9f) {
                    lv4.setFlowerPos(flowerPos);
                }
                if (beeState == BeeState.HONEY_DELIVERED) {
                    int i;
                    lv4.onHoneyDelivered();
                    if (state.isIn(BlockTags.BEEHIVES, statex -> statex.contains(BeehiveBlock.HONEY_LEVEL)) && (i = BeehiveBlockEntity.getHoneyLevel(state)) < 5) {
                        int j;
                        int n = j = world.random.nextInt(100) == 0 ? 2 : 1;
                        if (i + j > 5) {
                            --j;
                        }
                        world.setBlockState(pos, (BlockState)state.with(BeehiveBlock.HONEY_LEVEL, i + j));
                    }
                }
                if (entities != null) {
                    entities.add(lv4);
                }
                float f = lv3.getWidth();
                double d = bl ? 0.0 : 0.55 + (double)(f / 2.0f);
                double e = (double)pos.getX() + 0.5 + d * (double)lv.getOffsetX();
                double g = (double)pos.getY() + 0.5 - (double)(lv3.getHeight() / 2.0f);
                double h = (double)pos.getZ() + 0.5 + d * (double)lv.getOffsetZ();
                lv3.refreshPositionAndAngles(e, g, h, lv3.getYaw(), lv3.getPitch());
            }
            world.playSound(null, pos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(lv3, world.getBlockState(pos)));
            return world.spawnEntity(lv3);
        }
        return false;
    }

    private boolean hasFlowerPos() {
        return this.flowerPos != null;
    }

    private static void tickBees(World world, BlockPos pos, BlockState state, List<Bee> bees, @Nullable BlockPos flowerPos) {
        boolean bl = false;
        Iterator<Bee> iterator = bees.iterator();
        while (iterator.hasNext()) {
            BeeState lv2;
            Bee lv = iterator.next();
            if (!lv.canExitHive()) continue;
            BeeState beeState = lv2 = lv.hasNectar() ? BeeState.HONEY_DELIVERED : BeeState.BEE_RELEASED;
            if (!BeehiveBlockEntity.releaseBee(world, pos, state, lv.createData(), null, lv2, flowerPos)) continue;
            bl = true;
            iterator.remove();
        }
        if (bl) {
            BeehiveBlockEntity.markDirty(world, pos, state);
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity) {
        BeehiveBlockEntity.tickBees(world, pos, state, blockEntity.bees, blockEntity.flowerPos);
        if (!blockEntity.bees.isEmpty() && world.getRandom().nextDouble() < 0.005) {
            double d = (double)pos.getX() + 0.5;
            double e = pos.getY();
            double f = (double)pos.getZ() + 0.5;
            world.playSound(null, d, e, f, SoundEvents.BLOCK_BEEHIVE_WORK, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.bees.clear();
        view.read(BEES_KEY, BeeData.LIST_CODEC).orElse(List.of()).forEach(this::addBee);
        this.flowerPos = view.read(FLOWER_POS_KEY, BlockPos.CODEC).orElse(null);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put(BEES_KEY, BeeData.LIST_CODEC, this.createBeesData());
        view.putNullable(FLOWER_POS_KEY, BlockPos.CODEC, this.flowerPos);
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.bees.clear();
        List<BeeData> list = components.getOrDefault(DataComponentTypes.BEES, BeesComponent.DEFAULT).bees();
        list.forEach(this::addBee);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DataComponentTypes.BEES, new BeesComponent(this.createBeesData()));
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove(BEES_KEY);
    }

    private List<BeeData> createBeesData() {
        return this.bees.stream().map(Bee::createData).toList();
    }

    @Override
    public void registerTracking(ServerWorld world, DebugTrackable.Tracker tracker) {
        tracker.track(DebugSubscriptionTypes.BEE_HIVES, () -> BeeHiveDebugData.fromBeehive(this));
    }

    public static enum BeeState {
        HONEY_DELIVERED,
        BEE_RELEASED,
        EMERGENCY;

    }

    public record BeeData(TypedEntityData<EntityType<?>> entityData, int ticksInHive, int minTicksInHive) {
        public static final Codec<BeeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)TypedEntityData.createCodec(EntityType.CODEC).fieldOf("entity_data")).forGetter(BeeData::entityData), ((MapCodec)Codec.INT.fieldOf("ticks_in_hive")).forGetter(BeeData::ticksInHive), ((MapCodec)Codec.INT.fieldOf("min_ticks_in_hive")).forGetter(BeeData::minTicksInHive)).apply((Applicative<BeeData, ?>)instance, BeeData::new));
        public static final Codec<List<BeeData>> LIST_CODEC = CODEC.listOf();
        public static final PacketCodec<RegistryByteBuf, BeeData> PACKET_CODEC = PacketCodec.tuple(TypedEntityData.createPacketCodec(EntityType.PACKET_CODEC), BeeData::entityData, PacketCodecs.VAR_INT, BeeData::ticksInHive, PacketCodecs.VAR_INT, BeeData::minTicksInHive, BeeData::new);

        public static BeeData of(Entity entity) {
            try (ErrorReporter.Logging lv = new ErrorReporter.Logging(entity.getErrorReporterContext(), LOGGER);){
                NbtWriteView lv2 = NbtWriteView.create(lv, entity.getRegistryManager());
                entity.saveData(lv2);
                IRRELEVANT_BEE_NBT_KEYS.forEach(lv2::remove);
                NbtCompound lv3 = lv2.getNbt();
                boolean bl = lv3.getBoolean("HasNectar", false);
                BeeData beeData = new BeeData(TypedEntityData.create(entity.getType(), lv3), 0, bl ? 2400 : 600);
                return beeData;
            }
        }

        public static BeeData create(int ticksInHive) {
            return new BeeData(TypedEntityData.create(EntityType.BEE, new NbtCompound()), ticksInHive, 600);
        }

        @Nullable
        public Entity loadEntity(World world, BlockPos pos) {
            NbtCompound lv = this.entityData.copyNbtWithoutId();
            IRRELEVANT_BEE_NBT_KEYS.forEach(lv::remove);
            Entity lv2 = EntityType.loadEntityWithPassengers(this.entityData.getType(), lv, world, SpawnReason.LOAD, entity -> entity);
            if (lv2 == null || !lv2.getType().isIn(EntityTypeTags.BEEHIVE_INHABITORS)) {
                return null;
            }
            lv2.setNoGravity(true);
            if (lv2 instanceof BeeEntity) {
                BeeEntity lv3 = (BeeEntity)lv2;
                lv3.setHivePos(pos);
                BeeData.tickEntity(this.ticksInHive, lv3);
            }
            return lv2;
        }

        private static void tickEntity(int ticksInHive, BeeEntity beeEntity) {
            int j = beeEntity.getBreedingAge();
            if (j < 0) {
                beeEntity.setBreedingAge(Math.min(0, j + ticksInHive));
            } else if (j > 0) {
                beeEntity.setBreedingAge(Math.max(0, j - ticksInHive));
            }
            beeEntity.setLoveTicks(Math.max(0, beeEntity.getLoveTicks() - ticksInHive));
        }
    }

    static class Bee {
        private final BeeData data;
        private int ticksInHive;

        Bee(BeeData data) {
            this.data = data;
            this.ticksInHive = data.ticksInHive();
        }

        public boolean canExitHive() {
            return this.ticksInHive++ > this.data.minTicksInHive;
        }

        public BeeData createData() {
            return new BeeData(this.data.entityData, this.ticksInHive, this.data.minTicksInHive);
        }

        public boolean hasNectar() {
            return this.data.entityData.getNbtWithoutId().getBoolean("HasNectar", false);
        }
    }
}

