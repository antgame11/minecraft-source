/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.Falling;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FallingBlockEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BlockState DEFAULT_BLOCK_STATE = Blocks.SAND.getDefaultState();
    private static final int DEFAULT_TIME = 0;
    private static final float DEFAULT_FALL_HURT_AMOUNT = 0.0f;
    private static final int DEFAULT_FALL_HURT_MAX = 40;
    private static final boolean DEFAULT_DROP_ITEM = true;
    private static final boolean DEFAULT_DESTROYED_ON_LANDING = false;
    private BlockState blockState = DEFAULT_BLOCK_STATE;
    public int timeFalling = 0;
    public boolean dropItem = true;
    private boolean destroyedOnLanding = false;
    private boolean hurtEntities;
    private int fallHurtMax = 40;
    private float fallHurtAmount = 0.0f;
    @Nullable
    public NbtCompound blockEntityData;
    public boolean shouldDupe;
    protected static final TrackedData<BlockPos> BLOCK_POS = DataTracker.registerData(FallingBlockEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> arg, World arg2) {
        super(arg, arg2);
    }

    private FallingBlockEntity(World world, double x, double y, double z, BlockState blockState) {
        this((EntityType<? extends FallingBlockEntity>)EntityType.FALLING_BLOCK, world);
        this.blockState = blockState;
        this.intersectionChecked = true;
        this.setPosition(x, y, z);
        this.setVelocity(Vec3d.ZERO);
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.setFallingBlockPos(this.getBlockPos());
    }

    public static FallingBlockEntity spawnFromBlock(World world, BlockPos pos, BlockState state) {
        FallingBlockEntity lv = new FallingBlockEntity(world, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, state.contains(Properties.WATERLOGGED) ? (BlockState)state.with(Properties.WATERLOGGED, false) : state);
        world.setBlockState(pos, state.getFluidState().getBlockState(), Block.NOTIFY_ALL);
        world.spawnEntity(lv);
        return lv;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public final boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (!this.isAlwaysInvulnerableTo(source)) {
            this.scheduleVelocityUpdate();
        }
        return false;
    }

    public void setFallingBlockPos(BlockPos pos) {
        this.dataTracker.set(BLOCK_POS, pos);
    }

    public BlockPos getFallingBlockPos() {
        return this.dataTracker.get(BLOCK_POS);
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(BLOCK_POS, BlockPos.ORIGIN);
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    protected double getGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
            return;
        }
        Block lv = this.blockState.getBlock();
        ++this.timeFalling;
        this.applyGravity();
        this.move(MovementType.SELF, this.getVelocity());
        this.tickBlockCollision();
        this.tickPortalTeleportation();
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            if (this.isAlive() || this.shouldDupe) {
                BlockHitResult lv4;
                BlockPos lv3 = this.getBlockPos();
                boolean bl = this.blockState.getBlock() instanceof ConcretePowderBlock;
                boolean bl2 = bl && this.getEntityWorld().getFluidState(lv3).isIn(FluidTags.WATER);
                double d = this.getVelocity().lengthSquared();
                if (bl && d > 1.0 && (lv4 = this.getEntityWorld().raycast(new RaycastContext(new Vec3d(this.lastX, this.lastY, this.lastZ), this.getEntityPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY, this))).getType() != HitResult.Type.MISS && this.getEntityWorld().getFluidState(lv4.getBlockPos()).isIn(FluidTags.WATER)) {
                    lv3 = lv4.getBlockPos();
                    bl2 = true;
                }
                if (this.isOnGround() || bl2) {
                    BlockState lv5 = this.getEntityWorld().getBlockState(lv3);
                    this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
                    if (!lv5.isOf(Blocks.MOVING_PISTON)) {
                        if (!this.destroyedOnLanding) {
                            boolean bl5;
                            boolean bl3 = lv5.canReplace(new AutomaticItemPlacementContext(this.getEntityWorld(), lv3, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean bl4 = FallingBlock.canFallThrough(this.getEntityWorld().getBlockState(lv3.down())) && (!bl || !bl2);
                            boolean bl6 = bl5 = this.blockState.canPlaceAt(this.getEntityWorld(), lv3) && !bl4;
                            if (bl3 && bl5) {
                                if (this.blockState.contains(Properties.WATERLOGGED) && this.getEntityWorld().getFluidState(lv3).getFluid() == Fluids.WATER) {
                                    this.blockState = (BlockState)this.blockState.with(Properties.WATERLOGGED, true);
                                }
                                if (this.getEntityWorld().setBlockState(lv3, this.blockState, Block.NOTIFY_ALL)) {
                                    BlockEntity lv7;
                                    lv2.getChunkManager().chunkLoadingManager.sendToOtherNearbyPlayers(this, new BlockUpdateS2CPacket(lv3, this.getEntityWorld().getBlockState(lv3)));
                                    this.discard();
                                    if (lv instanceof Falling) {
                                        Falling lv6 = (Falling)((Object)lv);
                                        lv6.onLanding(this.getEntityWorld(), lv3, this.blockState, lv5, this);
                                    }
                                    if (this.blockEntityData != null && this.blockState.hasBlockEntity() && (lv7 = this.getEntityWorld().getBlockEntity(lv3)) != null) {
                                        try (ErrorReporter.Logging lv8 = new ErrorReporter.Logging(lv7.getReporterContext(), LOGGER);){
                                            DynamicRegistryManager lv9 = this.getEntityWorld().getRegistryManager();
                                            NbtWriteView lv10 = NbtWriteView.create(lv8, lv9);
                                            lv7.writeDataWithoutId(lv10);
                                            NbtCompound lv11 = lv10.getNbt();
                                            this.blockEntityData.forEach((string, arg2) -> lv11.put((String)string, arg2.copy()));
                                            lv7.read(NbtReadView.create(lv8, lv9, lv11));
                                        } catch (Exception exception) {
                                            LOGGER.error("Failed to load block entity from falling block", exception);
                                        }
                                        lv7.markDirty();
                                    }
                                } else if (this.dropItem && lv2.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                                    this.discard();
                                    this.onDestroyedOnLanding(lv, lv3);
                                    this.dropItem(lv2, lv);
                                }
                            } else {
                                this.discard();
                                if (this.dropItem && lv2.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                                    this.onDestroyedOnLanding(lv, lv3);
                                    this.dropItem(lv2, lv);
                                }
                            }
                        } else {
                            this.discard();
                            this.onDestroyedOnLanding(lv, lv3);
                        }
                    }
                } else if (this.timeFalling > 100 && (lv3.getY() <= this.getEntityWorld().getBottomY() || lv3.getY() > this.getEntityWorld().getTopYInclusive()) || this.timeFalling > 600) {
                    if (this.dropItem && lv2.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                        this.dropItem(lv2, lv);
                    }
                    this.discard();
                }
            }
        }
        this.setVelocity(this.getVelocity().multiply(0.98));
    }

    public void onDestroyedOnLanding(Block block, BlockPos pos) {
        if (block instanceof Falling) {
            ((Falling)((Object)block)).onDestroyedOnLanding(this.getEntityWorld(), pos, this);
        }
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        DamageSource damageSource2;
        if (!this.hurtEntities) {
            return false;
        }
        int i = MathHelper.ceil(fallDistance - 1.0);
        if (i < 0) {
            return false;
        }
        Predicate<Entity> predicate = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(EntityPredicates.VALID_LIVING_ENTITY);
        Block block = this.blockState.getBlock();
        if (block instanceof Falling) {
            Falling lv = (Falling)((Object)block);
            damageSource2 = lv.getDamageSource(this);
        } else {
            damageSource2 = this.getDamageSources().fallingBlock(this);
        }
        DamageSource lv2 = damageSource2;
        float g = Math.min(MathHelper.floor((float)i * this.fallHurtAmount), this.fallHurtMax);
        this.getEntityWorld().getOtherEntities(this, this.getBoundingBox(), predicate).forEach(entity -> entity.serverDamage(lv2, g));
        boolean bl = this.blockState.isIn(BlockTags.ANVIL);
        if (bl && g > 0.0f && this.random.nextFloat() < 0.05f + (float)i * 0.05f) {
            BlockState lv3 = AnvilBlock.getLandingState(this.blockState);
            if (lv3 == null) {
                this.destroyedOnLanding = true;
            } else {
                this.blockState = lv3;
            }
        }
        return false;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("BlockState", BlockState.CODEC, this.blockState);
        view.putInt("Time", this.timeFalling);
        view.putBoolean("DropItem", this.dropItem);
        view.putBoolean("HurtEntities", this.hurtEntities);
        view.putFloat("FallHurtAmount", this.fallHurtAmount);
        view.putInt("FallHurtMax", this.fallHurtMax);
        if (this.blockEntityData != null) {
            view.put("TileEntityData", NbtCompound.CODEC, this.blockEntityData);
        }
        view.putBoolean("CancelDrop", this.destroyedOnLanding);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.blockState = view.read("BlockState", BlockState.CODEC).orElse(DEFAULT_BLOCK_STATE);
        this.timeFalling = view.getInt("Time", 0);
        boolean bl = this.blockState.isIn(BlockTags.ANVIL);
        this.hurtEntities = view.getBoolean("HurtEntities", bl);
        this.fallHurtAmount = view.getFloat("FallHurtAmount", 0.0f);
        this.fallHurtMax = view.getInt("FallHurtMax", 40);
        this.dropItem = view.getBoolean("DropItem", true);
        this.blockEntityData = view.read("TileEntityData", NbtCompound.CODEC).orElse(null);
        this.destroyedOnLanding = view.getBoolean("CancelDrop", false);
    }

    public void setHurtEntities(float fallHurtAmount, int fallHurtMax) {
        this.hurtEntities = true;
        this.fallHurtAmount = fallHurtAmount;
        this.fallHurtMax = fallHurtMax;
    }

    public void setDestroyedOnLanding() {
        this.destroyedOnLanding = true;
    }

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    @Override
    public void populateCrashReport(CrashReportSection section) {
        super.populateCrashReport(section);
        section.add("Immitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable("entity.minecraft.falling_block_type", this.blockState.getBlock().getName());
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket((Entity)this, entityTrackerEntry, Block.getRawIdFromState(this.getBlockState()));
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.blockState = Block.getStateFromRawId(packet.getEntityData());
        this.intersectionChecked = true;
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        this.setPosition(d, e, f);
        this.setFallingBlockPos(this.getBlockPos());
    }

    @Override
    @Nullable
    public Entity teleportTo(TeleportTarget teleportTarget) {
        RegistryKey<World> lv = teleportTarget.world().getRegistryKey();
        RegistryKey<World> lv2 = this.getEntityWorld().getRegistryKey();
        boolean bl = (lv2 == World.END || lv == World.END) && lv2 != lv;
        Entity lv3 = super.teleportTo(teleportTarget);
        this.shouldDupe = lv3 != null && bl;
        return lv3;
    }
}

