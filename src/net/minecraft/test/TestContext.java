/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.FillBiomeCommand;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTestException;
import net.minecraft.test.GameTestState;
import net.minecraft.test.PositionedException;
import net.minecraft.test.TimedTaskRunner;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestContext {
    private final GameTestState test;
    private boolean hasFinalClause;

    public TestContext(GameTestState test) {
        this.test = test;
    }

    public GameTestException createError(Text message) {
        return new GameTestException(message, this.test.getTick());
    }

    public GameTestException createError(String translationKey, Object ... args) {
        return this.createError(Text.stringifiedTranslatable(translationKey, args));
    }

    public PositionedException createError(BlockPos pos, Text message) {
        return new PositionedException(message, this.getAbsolutePos(pos), pos, this.test.getTick());
    }

    public PositionedException createError(BlockPos pos, String translationKey, Object ... args) {
        return this.createError(pos, Text.stringifiedTranslatable(translationKey, args));
    }

    public ServerWorld getWorld() {
        return this.test.getWorld();
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.getWorld().getBlockState(this.getAbsolutePos(pos));
    }

    public <T extends BlockEntity> T getBlockEntity(BlockPos pos, Class<T> clazz) {
        BlockEntity lv = this.getWorld().getBlockEntity(this.getAbsolutePos(pos));
        if (lv == null) {
            throw this.createError(pos, "test.error.missing_block_entity", new Object[0]);
        }
        if (clazz.isInstance(lv)) {
            return (T)((BlockEntity)clazz.cast(lv));
        }
        throw this.createError(pos, "test.error.wrong_block_entity", lv.getType().getRegistryEntry().getIdAsString());
    }

    public void killAllEntities() {
        this.killAllEntities(Entity.class);
    }

    public void killAllEntities(Class<? extends Entity> entityClass) {
        Box lv = this.getTestBox();
        List<Entity> list = this.getWorld().getEntitiesByClass(entityClass, lv.expand(1.0), entity -> !(entity instanceof PlayerEntity));
        list.forEach(entity -> entity.kill(this.getWorld()));
    }

    public ItemEntity spawnItem(Item item, Vec3d pos) {
        ServerWorld lv = this.getWorld();
        Vec3d lv2 = this.getAbsolute(pos);
        ItemEntity lv3 = new ItemEntity(lv, lv2.x, lv2.y, lv2.z, new ItemStack(item, 1));
        lv3.setVelocity(0.0, 0.0, 0.0);
        lv.spawnEntity(lv3);
        return lv3;
    }

    public ItemEntity spawnItem(Item item, float x, float y, float z) {
        return this.spawnItem(item, new Vec3d(x, y, z));
    }

    public ItemEntity spawnItem(Item item, BlockPos pos) {
        return this.spawnItem(item, pos.getX(), pos.getY(), pos.getZ());
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, BlockPos pos) {
        return this.spawnEntity(type, Vec3d.ofBottomCenter(pos));
    }

    public <E extends Entity> List<E> spawnEntities(EntityType<E> type, BlockPos pos, int count) {
        return this.spawnEntities(type, Vec3d.ofBottomCenter(pos), count);
    }

    public <E extends Entity> List<E> spawnEntities(EntityType<E> type, Vec3d pos, int count) {
        ArrayList<E> list = new ArrayList<E>();
        for (int j = 0; j < count; ++j) {
            list.add(this.spawnEntity(type, pos));
        }
        return list;
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, Vec3d pos) {
        ServerWorld lv = this.getWorld();
        E lv2 = type.create(lv, SpawnReason.STRUCTURE);
        if (lv2 == null) {
            throw this.createError(BlockPos.ofFloored(pos), "test.error.spawn_failure", type.getRegistryEntry().getIdAsString());
        }
        if (lv2 instanceof MobEntity) {
            MobEntity lv3 = (MobEntity)lv2;
            lv3.setPersistent();
        }
        Vec3d lv4 = this.getAbsolute(pos);
        float f = ((Entity)lv2).applyRotation(this.getRotation());
        ((Entity)lv2).refreshPositionAndAngles(lv4.x, lv4.y, lv4.z, f, ((Entity)lv2).getPitch());
        ((Entity)lv2).setBodyYaw(f);
        ((Entity)lv2).setHeadYaw(f);
        lv.spawnEntity((Entity)lv2);
        return lv2;
    }

    public void damage(Entity entity, DamageSource damageSource, float amount) {
        entity.damage(this.getWorld(), damageSource, amount);
    }

    public void killEntity(Entity entity) {
        entity.kill(this.getWorld());
    }

    public <E extends Entity> E expectEntityInWorld(EntityType<E> type) {
        return this.expectEntity(type, 0, 0, 0, 2.147483647E9);
    }

    public <E extends Entity> E expectEntity(EntityType<E> type, int x, int y, int z, double margin) {
        List<E> list = this.getEntitiesAround(type, x, y, z, margin);
        if (list.isEmpty()) {
            throw this.createError("test.error.expected_entity_around", type.getName(), x, y, z);
        }
        if (list.size() > 1) {
            throw this.createError("test.error.too_many_entities", type.getUntranslatedName(), x, y, z, list.size());
        }
        Vec3d lv = this.getAbsolute(new Vec3d(x, y, z));
        list.sort((a, b) -> {
            double d = a.getEntityPos().distanceTo(lv);
            double e = b.getEntityPos().distanceTo(lv);
            return Double.compare(d, e);
        });
        return (E)((Entity)list.get(0));
    }

    public <E extends Entity> List<E> getEntitiesAround(EntityType<E> type, int x, int y, int z, double margin) {
        return this.getEntitiesAround(type, Vec3d.ofBottomCenter(new BlockPos(x, y, z)), margin);
    }

    public <E extends Entity> List<E> getEntitiesAround(EntityType<E> type, Vec3d pos, double margin) {
        ServerWorld lv = this.getWorld();
        Vec3d lv2 = this.getAbsolute(pos);
        Box lv3 = this.test.getBoundingBox();
        Box lv4 = new Box(lv2.add(-margin, -margin, -margin), lv2.add(margin, margin, margin));
        return lv.getEntitiesByType(type, lv3, entity -> entity.getBoundingBox().intersects(lv4) && entity.isAlive());
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, int x, int y, int z) {
        return this.spawnEntity(type, new BlockPos(x, y, z));
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, float x, float y, float z) {
        return this.spawnEntity(type, new Vec3d(x, y, z));
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, BlockPos pos) {
        MobEntity lv = (MobEntity)this.spawnEntity(type, pos);
        lv.clearGoalsAndTasks();
        return (E)lv;
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, int x, int y, int z) {
        return this.spawnMob(type, new BlockPos(x, y, z));
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, Vec3d pos) {
        MobEntity lv = (MobEntity)this.spawnEntity(type, pos);
        lv.clearGoalsAndTasks();
        return (E)lv;
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, float x, float y, float z) {
        return this.spawnMob(type, new Vec3d(x, y, z));
    }

    public void setEntityPos(MobEntity entity, float x, float y, float z) {
        Vec3d lv = this.getAbsolute(new Vec3d(x, y, z));
        entity.refreshPositionAndAngles(lv.x, lv.y, lv.z, entity.getYaw(), entity.getPitch());
    }

    public TimedTaskRunner startMovingTowards(MobEntity entity, BlockPos pos, float speed) {
        return this.createTimedTaskRunner().expectMinDurationAndRun(2, () -> {
            Path lv = entity.getNavigation().findPathTo(this.getAbsolutePos(pos), 0);
            entity.getNavigation().startMovingAlong(lv, speed);
        });
    }

    public void pushButton(int x, int y, int z) {
        this.pushButton(new BlockPos(x, y, z));
    }

    public void pushButton(BlockPos pos) {
        this.expectBlockIn(BlockTags.BUTTONS, pos);
        BlockPos lv = this.getAbsolutePos(pos);
        BlockState lv2 = this.getWorld().getBlockState(lv);
        ButtonBlock lv3 = (ButtonBlock)lv2.getBlock();
        lv3.powerOn(lv2, this.getWorld(), lv, null);
    }

    public void useBlock(BlockPos pos) {
        this.useBlock(pos, this.createMockPlayer(GameMode.CREATIVE));
    }

    public void useBlock(BlockPos pos, PlayerEntity player) {
        BlockPos lv = this.getAbsolutePos(pos);
        this.useBlock(pos, player, new BlockHitResult(Vec3d.ofCenter(lv), Direction.NORTH, lv, true));
    }

    public void useBlock(BlockPos pos, PlayerEntity player, BlockHitResult result) {
        Hand lv3;
        BlockPos lv = this.getAbsolutePos(pos);
        BlockState lv2 = this.getWorld().getBlockState(lv);
        ActionResult lv4 = lv2.onUseWithItem(player.getStackInHand(lv3 = Hand.MAIN_HAND), this.getWorld(), player, lv3, result);
        if (lv4.isAccepted()) {
            return;
        }
        if (lv4 instanceof ActionResult.PassToDefaultBlockAction && lv2.onUse(this.getWorld(), player, result).isAccepted()) {
            return;
        }
        ItemUsageContext lv5 = new ItemUsageContext(player, lv3, result);
        player.getStackInHand(lv3).useOnBlock(lv5);
    }

    public LivingEntity drown(LivingEntity entity) {
        entity.setAir(0);
        entity.setHealth(0.25f);
        return entity;
    }

    public LivingEntity setHealthLow(LivingEntity entity) {
        entity.setHealth(0.25f);
        return entity;
    }

    public PlayerEntity createMockPlayer(final GameMode gameMode) {
        return new PlayerEntity(this, this.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-player")){

            @Override
            @NotNull
            public GameMode getGameMode() {
                return gameMode;
            }

            @Override
            public boolean isControlledByPlayer() {
                return false;
            }
        };
    }

    @Deprecated(forRemoval=true)
    public ServerPlayerEntity createMockCreativeServerPlayerInWorld() {
        ConnectedClientData lv = ConnectedClientData.createDefault(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayerEntity lv2 = new ServerPlayerEntity(this, this.getWorld().getServer(), this.getWorld(), lv.gameProfile(), lv.syncedOptions()){

            @Override
            public GameMode getGameMode() {
                return GameMode.CREATIVE;
            }
        };
        ClientConnection lv3 = new ClientConnection(NetworkSide.SERVERBOUND);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(lv3);
        this.getWorld().getServer().getPlayerManager().onPlayerConnect(lv3, lv2, lv);
        return lv2;
    }

    public void toggleLever(int x, int y, int z) {
        this.toggleLever(new BlockPos(x, y, z));
    }

    public void toggleLever(BlockPos pos) {
        this.expectBlock(Blocks.LEVER, pos);
        BlockPos lv = this.getAbsolutePos(pos);
        BlockState lv2 = this.getWorld().getBlockState(lv);
        LeverBlock lv3 = (LeverBlock)lv2.getBlock();
        lv3.togglePower(lv2, this.getWorld(), lv, null);
    }

    public void putAndRemoveRedstoneBlock(BlockPos pos, long delay) {
        this.setBlockState(pos, Blocks.REDSTONE_BLOCK);
        this.waitAndRun(delay, () -> this.setBlockState(pos, Blocks.AIR));
    }

    public void removeBlock(BlockPos pos) {
        this.getWorld().breakBlock(this.getAbsolutePos(pos), false, null);
    }

    public void setBlockState(int x, int y, int z, Block block) {
        this.setBlockState(new BlockPos(x, y, z), block);
    }

    public void setBlockState(int x, int y, int z, BlockState state) {
        this.setBlockState(new BlockPos(x, y, z), state);
    }

    public void setBlockState(BlockPos pos, Block block) {
        this.setBlockState(pos, block.getDefaultState());
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        this.getWorld().setBlockState(this.getAbsolutePos(pos), state, Block.NOTIFY_ALL);
    }

    public void setBlockFacing(BlockPos pos, Block block, Direction facing) {
        this.setBlockFacing(pos, block.getDefaultState(), facing);
    }

    public void setBlockFacing(BlockPos pos, BlockState block, Direction facing) {
        BlockState lv = block;
        if (block.contains(HorizontalFacingBlock.FACING)) {
            lv = (BlockState)block.with(HorizontalFacingBlock.FACING, facing);
        }
        if (block.contains(Properties.FACING)) {
            lv = (BlockState)block.with(Properties.FACING, facing);
        }
        this.getWorld().setBlockState(this.getAbsolutePos(pos), lv, Block.NOTIFY_ALL);
    }

    public void useNightTime() {
        this.setTime(13000);
    }

    public void setTime(int timeOfDay) {
        this.getWorld().setTimeOfDay(timeOfDay);
    }

    public void expectBlock(Block block, int x, int y, int z) {
        this.expectBlock(block, new BlockPos(x, y, z));
    }

    public void expectBlock(Block block, BlockPos pos) {
        BlockState lv = this.getBlockState(pos);
        this.checkBlock(pos, block1 -> lv.isOf(block), actualBlock -> Text.translatable("test.error.expected_block", block.getName(), actualBlock.getName()));
    }

    public void dontExpectBlock(Block block, int x, int y, int z) {
        this.dontExpectBlock(block, new BlockPos(x, y, z));
    }

    public void dontExpectBlock(Block block, BlockPos pos) {
        this.checkBlock(pos, block1 -> !this.getBlockState(pos).isOf(block), actualBlock -> Text.translatable("test.error.unexpected_block", block.getName()));
    }

    public void expectBlockIn(TagKey<Block> tag, BlockPos pos) {
        this.checkBlockState(pos, state -> state.isIn(tag), state -> Text.translatable("test.error.expected_block_tag", Text.of(tag.id()), state.getBlock().getName()));
    }

    public void expectBlockAtEnd(Block block, int x, int y, int z) {
        this.expectBlockAtEnd(block, new BlockPos(x, y, z));
    }

    public void expectBlockAtEnd(Block block, BlockPos pos) {
        this.addInstantFinalTask(() -> this.expectBlock(block, pos));
    }

    public void checkBlock(BlockPos pos, Predicate<Block> predicate, Function<Block, Text> messageGetter) {
        this.checkBlockState(pos, state -> predicate.test(state.getBlock()), state -> (Text)messageGetter.apply(state.getBlock()));
    }

    public <T extends Comparable<T>> void expectBlockProperty(BlockPos pos, Property<T> property, T value) {
        BlockState lv = this.getBlockState(pos);
        boolean bl = lv.contains(property);
        if (!bl) {
            throw this.createError(pos, "test.error.block_property_missing", property.getName(), value);
        }
        if (!lv.get(property).equals(value)) {
            throw this.createError(pos, "test.error.block_property_mismatch", property.getName(), value, lv.get(property));
        }
    }

    public <T extends Comparable<T>> void checkBlockProperty(BlockPos pos, Property<T> property, Predicate<T> predicate, Text message) {
        this.checkBlockState(pos, state -> {
            if (!state.contains(property)) {
                return false;
            }
            Object comparable = state.get(property);
            return predicate.test(comparable);
        }, state -> message);
    }

    public void expectBlockState(BlockPos pos, BlockState state) {
        BlockState lv = this.getBlockState(pos);
        if (!lv.equals(state)) {
            throw this.createError(pos, "test.error.state_not_equal", state, lv);
        }
    }

    public void checkBlockState(BlockPos pos, Predicate<BlockState> predicate, Function<BlockState, Text> messageGetter) {
        BlockState lv = this.getBlockState(pos);
        if (!predicate.test(lv)) {
            throw this.createError(pos, messageGetter.apply(lv));
        }
    }

    public <T extends BlockEntity> void checkBlockEntity(BlockPos pos, Class<T> clazz, Predicate<T> predicate, Supplier<Text> messageGetter) {
        T lv = this.getBlockEntity(pos, clazz);
        if (!predicate.test(lv)) {
            throw this.createError(pos, messageGetter.get());
        }
    }

    public void expectRedstonePower(BlockPos pos, Direction direction, IntPredicate powerPredicate, Supplier<Text> messageGetter) {
        BlockPos lv = this.getAbsolutePos(pos);
        ServerWorld lv2 = this.getWorld();
        BlockState lv3 = lv2.getBlockState(lv);
        int i = lv3.getWeakRedstonePower(lv2, lv, direction);
        if (!powerPredicate.test(i)) {
            throw this.createError(pos, messageGetter.get());
        }
    }

    public void expectEntity(EntityType<?> type) {
        if (!this.getWorld().hasEntities(type, this.getTestBox(), Entity::isAlive)) {
            throw this.createError("test.error.expected_entity_in_test", type.getName());
        }
    }

    public void expectEntityAt(EntityType<?> type, int x, int y, int z) {
        this.expectEntityAt(type, new BlockPos(x, y, z));
    }

    public void expectEntityAt(EntityType<?> type, BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        if (!this.getWorld().hasEntities(type, new Box(lv), Entity::isAlive)) {
            throw this.createError(pos, "test.error.expected_entity", type.getName());
        }
    }

    public void expectEntityInside(EntityType<?> type, Box box) {
        Box lv = this.getAbsolute(box);
        if (!this.getWorld().hasEntities(type, lv, Entity::isAlive)) {
            throw this.createError(BlockPos.ofFloored(box.getCenter()), "test.error.expected_entity", type.getName());
        }
    }

    public void expectEntities(EntityType<?> type, int amount) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
        if (list.size() != amount) {
            throw this.createError("test.error.expected_entity_count", amount, type.getName(), list.size());
        }
    }

    public void expectEntitiesAround(EntityType<?> type, BlockPos pos, int amount, double radius) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<?> list = this.getEntitiesAround(type, pos, radius);
        if (list.size() != amount) {
            throw this.createError(pos, "test.error.expected_entity_count", amount, type.getName(), list.size());
        }
    }

    public void expectEntityAround(EntityType<?> type, BlockPos pos, double radius) {
        List<?> list = this.getEntitiesAround(type, pos, radius);
        if (list.isEmpty()) {
            BlockPos lv = this.getAbsolutePos(pos);
            throw this.createError(pos, "test.error.expected_entity", type.getName());
        }
    }

    public <T extends Entity> List<T> getEntitiesAround(EntityType<T> type, BlockPos pos, double radius) {
        BlockPos lv = this.getAbsolutePos(pos);
        return this.getWorld().getEntitiesByType(type, new Box(lv).expand(radius), Entity::isAlive);
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> type) {
        return this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
    }

    public void expectEntityAt(Entity entity, int x, int y, int z) {
        this.expectEntityAt(entity, new BlockPos(x, y, z));
    }

    public void expectEntityAt(Entity entity, BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(entity.getType(), new Box(lv), Entity::isAlive);
        list.stream().filter(e -> e == entity).findFirst().orElseThrow(() -> this.createError(pos, "test.error.expected_entity", entity.getType().getName()));
    }

    public void expectItemsAt(Item item, BlockPos pos, double radius, int amount) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<ItemEntity> list = this.getWorld().getEntitiesByType(EntityType.ITEM, new Box(lv).expand(radius), Entity::isAlive);
        int j = 0;
        for (ItemEntity lv2 : list) {
            ItemStack lv3 = lv2.getStack();
            if (!lv3.isOf(item)) continue;
            j += lv3.getCount();
        }
        if (j != amount) {
            throw this.createError(pos, "test.error.expected_items_count", amount, item.getName(), j);
        }
    }

    public void expectItemAt(Item item, BlockPos pos, double radius) {
        BlockPos lv = this.getAbsolutePos(pos);
        Predicate<ItemEntity> predicate = entity -> entity.isAlive() && entity.getStack().isOf(item);
        if (!this.getWorld().hasEntities(EntityType.ITEM, new Box(lv).expand(radius), predicate)) {
            throw this.createError(pos, "test.error.expected_item", item.getName());
        }
    }

    public void dontExpectItemAt(Item item, BlockPos pos, double radius) {
        BlockPos lv = this.getAbsolutePos(pos);
        Predicate<ItemEntity> predicate = entity -> entity.isAlive() && entity.getStack().isOf(item);
        if (this.getWorld().hasEntities(EntityType.ITEM, new Box(lv).expand(radius), predicate)) {
            throw this.createError(pos, "test.error.unexpected_item", item.getName());
        }
    }

    public void expectItem(Item item) {
        Predicate<ItemEntity> predicate = entity -> entity.isAlive() && entity.getStack().isOf(item);
        if (!this.getWorld().hasEntities(EntityType.ITEM, this.getTestBox(), predicate)) {
            throw this.createError("test.error.expected_item", item.getName());
        }
    }

    public void dontExpectItem(Item item) {
        Predicate<ItemEntity> predicate = entity -> entity.isAlive() && entity.getStack().isOf(item);
        if (this.getWorld().hasEntities(EntityType.ITEM, this.getTestBox(), predicate)) {
            throw this.createError("test.error.unexpected_item", item.getName());
        }
    }

    public void dontExpectEntity(EntityType<?> type) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
        if (!list.isEmpty()) {
            throw this.createError(list.getFirst().getBlockPos(), "test.error.unexpected_entity", type.getName());
        }
    }

    public void dontExpectEntityAt(EntityType<?> type, int x, int y, int z) {
        this.dontExpectEntityAt(type, new BlockPos(x, y, z));
    }

    public void dontExpectEntityAt(EntityType<?> type, BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        if (this.getWorld().hasEntities(type, new Box(lv), Entity::isAlive)) {
            throw this.createError(pos, "test.error.unexpected_entity", type.getName());
        }
    }

    public void dontExpectEntityBetween(EntityType<?> type, Box box) {
        Box lv = this.getAbsolute(box);
        List<Entity> list = this.getWorld().getEntitiesByType(type, lv, Entity::isAlive);
        if (!list.isEmpty()) {
            throw this.createError(list.getFirst().getBlockPos(), "test.error.unexpected_entity", type.getName());
        }
    }

    public void expectEntityToTouch(EntityType<?> type, double x, double y, double z) {
        Vec3d lv = new Vec3d(x, y, z);
        Vec3d lv2 = this.getAbsolute(lv);
        Predicate<Entity> predicate = entity -> entity.getBoundingBox().intersects(lv2, lv2);
        if (!this.getWorld().hasEntities(type, this.getTestBox(), predicate)) {
            throw this.createError("test.error.expected_entity_touching", type.getName(), lv2.getX(), lv2.getY(), lv2.getZ(), x, y, z);
        }
    }

    public void dontExpectEntityToTouch(EntityType<?> type, double x, double y, double z) {
        Vec3d lv = new Vec3d(x, y, z);
        Vec3d lv2 = this.getAbsolute(lv);
        Predicate<Entity> predicate = entity -> !entity.getBoundingBox().intersects(lv2, lv2);
        if (!this.getWorld().hasEntities(type, this.getTestBox(), predicate)) {
            throw this.createError("test.error.expected_entity_not_touching", type.getName(), lv2.getX(), lv2.getY(), lv2.getZ(), x, y, z);
        }
    }

    public <E extends Entity, T> void expectEntity(BlockPos pos, EntityType<E> type, Predicate<E> predicate) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(type, new Box(lv), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.createError(pos, "test.error.expected_entity", type.getName());
        }
        for (Entity lv2 : list) {
            if (predicate.test(lv2)) continue;
            throw this.createError(lv2.getBlockPos(), "test.error.expected_entity_data_predicate", lv2.getName());
        }
    }

    public <E extends Entity, T> void expectEntityWithData(BlockPos pos, EntityType<E> type, Function<? super E, T> entityDataGetter, @Nullable T data) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(type, new Box(lv), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.createError(pos, "test.error.expected_entity", type.getName());
        }
        for (Entity lv2 : list) {
            T object2 = entityDataGetter.apply(lv2);
            if (Objects.equals(object2, data)) continue;
            throw this.createError(pos, "test.error.expected_entity_data", data, object2);
        }
    }

    public <E extends LivingEntity> void expectEntityHoldingItem(BlockPos pos, EntityType<E> entityType, Item item) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<LivingEntity> list = this.getWorld().getEntitiesByType(entityType, new Box(lv), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.createError(pos, "test.error.expected_entity", entityType.getName());
        }
        for (LivingEntity lv2 : list) {
            if (!lv2.isHolding(item)) continue;
            return;
        }
        throw this.createError(pos, "test.error.expected_entity_holding", item.getName());
    }

    public <E extends Entity> void expectEntityWithItem(BlockPos pos, EntityType<E> entityType, Item item) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(entityType, new Box(lv), entity -> ((Entity)entity).isAlive());
        if (list.isEmpty()) {
            throw this.createError(pos, "test.error.expected_entity", entityType.getName());
        }
        for (Entity lv2 : list) {
            if (!((InventoryOwner)((Object)lv2)).getInventory().containsAny(stack -> stack.isOf(item))) continue;
            return;
        }
        throw this.createError(pos, "test.error.expected_entity_having", item.getName());
    }

    public void expectEmptyContainer(BlockPos pos) {
        LockableContainerBlockEntity lv = this.getBlockEntity(pos, LockableContainerBlockEntity.class);
        if (!lv.isEmpty()) {
            throw this.createError(pos, "test.error.expected_empty_container", new Object[0]);
        }
    }

    public void expectContainerWithSingle(BlockPos pos, Item item) {
        LockableContainerBlockEntity lv = this.getBlockEntity(pos, LockableContainerBlockEntity.class);
        if (lv.count(item) != 1) {
            throw this.createError(pos, "test.error.expected_container_contents_single", item.getName());
        }
    }

    public void expectContainerWith(BlockPos pos, Item item) {
        LockableContainerBlockEntity lv = this.getBlockEntity(pos, LockableContainerBlockEntity.class);
        if (lv.count(item) == 0) {
            throw this.createError(pos, "test.error.expected_container_contents", item.getName());
        }
    }

    public void expectSameStates(BlockBox checkedBlockBox, BlockPos correctStatePos) {
        BlockPos.stream(checkedBlockBox).forEach(checkedPos -> {
            BlockPos lv = correctStatePos.add(checkedPos.getX() - checkedBlockBox.getMinX(), checkedPos.getY() - checkedBlockBox.getMinY(), checkedPos.getZ() - checkedBlockBox.getMinZ());
            this.expectSameStates((BlockPos)checkedPos, lv);
        });
    }

    public void expectSameStates(BlockPos checkedPos, BlockPos correctStatePos) {
        BlockState lv2;
        BlockState lv = this.getBlockState(checkedPos);
        if (lv != (lv2 = this.getBlockState(correctStatePos))) {
            throw this.createError(checkedPos, "test.error.state_not_equal", lv2, lv);
        }
    }

    public void expectContainerWith(long delay, BlockPos pos, Item item) {
        this.runAtTick(delay, () -> this.expectContainerWithSingle(pos, item));
    }

    public void expectEmptyContainer(long delay, BlockPos pos) {
        this.runAtTick(delay, () -> this.expectEmptyContainer(pos));
    }

    public <E extends Entity, T> void expectEntityWithDataEnd(BlockPos pos, EntityType<E> type, Function<E, T> entityDataGetter, T data) {
        this.addInstantFinalTask(() -> this.expectEntityWithData(pos, type, entityDataGetter, data));
    }

    public void expectEntityIn(Entity entity, Box box, Text message) {
        if (!box.contains(this.getRelative(entity.getEntityPos()))) {
            throw this.createError(message);
        }
    }

    public <E extends Entity> void testEntity(E entity, Predicate<E> predicate, Text message) {
        if (!predicate.test(entity)) {
            throw this.createError(entity.getBlockPos(), "test.error.entity_property", entity.getName(), message);
        }
    }

    public <E extends Entity, T> void testEntityProperty(E entity, Function<E, T> propertyGetter, T value, Text message) {
        T object2 = propertyGetter.apply(entity);
        if (!object2.equals(value)) {
            throw this.createError(entity.getBlockPos(), "test.error.entity_property_details", entity.getName(), message, object2, value);
        }
    }

    public void expectEntityHasEffect(LivingEntity entity, RegistryEntry<StatusEffect> effect, int amplifier) {
        StatusEffectInstance lv = entity.getStatusEffect(effect);
        if (lv == null || lv.getAmplifier() != amplifier) {
            throw this.createError("test.error.expected_entity_effect", entity.getName(), PotionContentsComponent.getEffectText(effect, amplifier));
        }
    }

    public void expectEntityAtEnd(EntityType<?> type, int x, int y, int z) {
        this.expectEntityAtEnd(type, new BlockPos(x, y, z));
    }

    public void expectEntityAtEnd(EntityType<?> type, BlockPos pos) {
        this.addInstantFinalTask(() -> this.expectEntityAt(type, pos));
    }

    public void dontExpectEntityAtEnd(EntityType<?> type, int x, int y, int z) {
        this.dontExpectEntityAtEnd(type, new BlockPos(x, y, z));
    }

    public void dontExpectEntityAtEnd(EntityType<?> type, BlockPos pos) {
        this.addInstantFinalTask(() -> this.dontExpectEntityAt(type, pos));
    }

    public void complete() {
        this.test.completeIfSuccessful();
    }

    private void markFinalCause() {
        if (this.hasFinalClause) {
            throw new IllegalStateException("This test already has final clause");
        }
        this.hasFinalClause = true;
    }

    public void addFinalTask(Runnable runnable) {
        this.markFinalCause();
        this.test.createTimedTaskRunner().createAndAdd(0L, runnable).completeIfSuccessful();
    }

    public void addInstantFinalTask(Runnable runnable) {
        this.markFinalCause();
        this.test.createTimedTaskRunner().createAndAdd(runnable).completeIfSuccessful();
    }

    public void addFinalTaskWithDuration(int duration, Runnable runnable) {
        this.markFinalCause();
        this.test.createTimedTaskRunner().createAndAdd(duration, runnable).completeIfSuccessful();
    }

    public void runAtTick(long tick, Runnable runnable) {
        this.test.runAtTick(tick, runnable);
    }

    public void waitAndRun(long ticks, Runnable runnable) {
        this.runAtTick((long)this.test.getTick() + ticks, runnable);
    }

    public void forceRandomTick(BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        ServerWorld lv2 = this.getWorld();
        lv2.getBlockState(lv).randomTick(lv2, lv, lv2.random);
    }

    public void forceScheduledTick(BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        ServerWorld lv2 = this.getWorld();
        lv2.getBlockState(lv).scheduledTick(lv2, lv, lv2.random);
    }

    public void forceTickIceAndSnow(BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        ServerWorld lv2 = this.getWorld();
        lv2.tickIceAndSnow(lv);
    }

    public void forceTickIceAndSnow() {
        Box lv = this.getRelativeTestBox();
        int i = (int)Math.floor(lv.maxX);
        int j = (int)Math.floor(lv.maxZ);
        int k = (int)Math.floor(lv.maxY);
        for (int l = (int)Math.floor(lv.minX); l < i; ++l) {
            for (int m = (int)Math.floor(lv.minZ); m < j; ++m) {
                this.forceTickIceAndSnow(new BlockPos(l, k, m));
            }
        }
    }

    public int getRelativeTopY(Heightmap.Type heightmap, int x, int z) {
        BlockPos lv = this.getAbsolutePos(new BlockPos(x, 0, z));
        return this.getRelativePos(this.getWorld().getTopPosition(heightmap, lv)).getY();
    }

    public void throwPositionedException(Text message, BlockPos pos) {
        throw this.createError(pos, message);
    }

    public void throwPositionedException(Text message, Entity entity) {
        throw this.createError(entity.getBlockPos(), message);
    }

    public void throwGameTestException(Text message) {
        throw this.createError(message);
    }

    public void throwGameTestException(String message) {
        throw this.createError(Text.literal(message));
    }

    public void addTask(Runnable task) {
        this.test.createTimedTaskRunner().createAndAdd(task).fail(() -> this.createError("test.error.fail", new Object[0]));
    }

    public void runAtEveryTick(Runnable task) {
        LongStream.range(this.test.getTick(), this.test.getTickLimit()).forEach(tick -> this.test.runAtTick(tick, task::run));
    }

    public TimedTaskRunner createTimedTaskRunner() {
        return this.test.createTimedTaskRunner();
    }

    public BlockPos getAbsolutePos(BlockPos pos) {
        BlockPos lv = this.test.getOrigin();
        BlockPos lv2 = lv.add(pos);
        return StructureTemplate.transformAround(lv2, BlockMirror.NONE, this.test.getRotation(), lv);
    }

    public BlockPos getRelativePos(BlockPos pos) {
        BlockPos lv = this.test.getOrigin();
        BlockRotation lv2 = this.test.getRotation().rotate(BlockRotation.CLOCKWISE_180);
        BlockPos lv3 = StructureTemplate.transformAround(pos, BlockMirror.NONE, lv2, lv);
        return lv3.subtract(lv);
    }

    public Box getAbsolute(Box box) {
        Vec3d lv = this.getAbsolute(box.getMinPos());
        Vec3d lv2 = this.getAbsolute(box.getMaxPos());
        return new Box(lv, lv2);
    }

    public Box getRelative(Box box) {
        Vec3d lv = this.getRelative(box.getMinPos());
        Vec3d lv2 = this.getRelative(box.getMaxPos());
        return new Box(lv, lv2);
    }

    public Vec3d getAbsolute(Vec3d pos) {
        Vec3d lv = Vec3d.of(this.test.getOrigin());
        return StructureTemplate.transformAround(lv.add(pos), BlockMirror.NONE, this.test.getRotation(), this.test.getOrigin());
    }

    public Vec3d getRelative(Vec3d pos) {
        Vec3d lv = Vec3d.of(this.test.getOrigin());
        return StructureTemplate.transformAround(pos.subtract(lv), BlockMirror.NONE, this.test.getRotation(), this.test.getOrigin());
    }

    public BlockRotation getRotation() {
        return this.test.getRotation();
    }

    public Direction getDirection() {
        return this.test.getRotation().rotate(Direction.SOUTH);
    }

    public void assertTrue(boolean condition, Text message) {
        if (!condition) {
            throw this.createError(message);
        }
    }

    public <N> void assertEquals(N expected, N value, Text message) {
        if (!expected.equals(value)) {
            throw this.createError("test.error.value_not_equal", message, expected, value);
        }
    }

    public void assertFalse(boolean condition, Text message) {
        this.assertTrue(!condition, message);
    }

    public long getTick() {
        return this.test.getTick();
    }

    public Box getTestBox() {
        return this.test.getBoundingBox();
    }

    private Box getRelativeTestBox() {
        Box lv = this.test.getBoundingBox();
        BlockRotation lv2 = this.test.getRotation();
        switch (lv2) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                return new Box(0.0, 0.0, 0.0, lv.getLengthZ(), lv.getLengthY(), lv.getLengthX());
            }
        }
        return new Box(0.0, 0.0, 0.0, lv.getLengthX(), lv.getLengthY(), lv.getLengthZ());
    }

    public void forEachRelativePos(Consumer<BlockPos> posConsumer) {
        Box lv = this.getRelativeTestBox().shrink(1.0, 1.0, 1.0);
        BlockPos.Mutable.stream(lv).forEach(posConsumer);
    }

    public void forEachRemainingTick(Runnable runnable) {
        LongStream.range(this.test.getTick(), this.test.getTickLimit()).forEach(tick -> this.test.runAtTick(tick, runnable::run));
    }

    public void useStackOnBlock(PlayerEntity player, ItemStack stack, BlockPos pos, Direction direction) {
        BlockPos lv = this.getAbsolutePos(pos.offset(direction));
        BlockHitResult lv2 = new BlockHitResult(Vec3d.ofCenter(lv), direction, lv, false);
        ItemUsageContext lv3 = new ItemUsageContext(player, Hand.MAIN_HAND, lv2);
        stack.useOnBlock(lv3);
    }

    public void setBiome(RegistryKey<Biome> biome) {
        Box lv = this.getTestBox();
        BlockPos lv2 = BlockPos.ofFloored(lv.minX, lv.minY, lv.minZ);
        BlockPos lv3 = BlockPos.ofFloored(lv.maxX, lv.maxY, lv.maxZ);
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fillBiome(this.getWorld(), lv2, lv3, this.getWorld().getRegistryManager().getOrThrow(RegistryKeys.BIOME).getOrThrow(biome));
        if (either.right().isPresent()) {
            throw this.createError("test.error.set_biome", new Object[0]);
        }
    }
}

