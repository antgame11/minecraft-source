/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Portal;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.entity.TestBlockEntity;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HangingSignEditScreen;
import net.minecraft.client.gui.screen.ingame.JigsawBlockScreen;
import net.minecraft.client.gui.screen.ingame.MinecartCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.gui.screen.ingame.TestBlockScreen;
import net.minecraft.client.gui.screen.ingame.TestInstanceBlockScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.sound.AmbientSoundLoops;
import net.minecraft.client.sound.AmbientSoundPlayer;
import net.minecraft.client.sound.BiomeEffectSoundPlayer;
import net.minecraft.client.sound.BubbleColumnSoundPlayer;
import net.minecraft.client.sound.ElytraSoundInstance;
import net.minecraft.client.sound.HappyGhastRidingSoundInstance;
import net.minecraft.client.sound.MinecartInsideSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.ClickType;
import net.minecraft.util.Cooldown;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientPlayerEntity
extends AbstractClientPlayerEntity {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_32671 = 20;
    private static final int field_32672 = 600;
    private static final int field_32673 = 100;
    private static final float field_32674 = 0.6f;
    private static final double field_32675 = 0.35;
    private static final double MAX_SOFT_COLLISION_RADIANS = 0.13962633907794952;
    public static final float field_55135 = 0.2f;
    public final ClientPlayNetworkHandler networkHandler;
    private final StatHandler statHandler;
    private final ClientRecipeBook recipeBook;
    private final Cooldown itemDropCooldown = new Cooldown(20, 1280);
    private final List<ClientPlayerTickable> tickables = Lists.newArrayList();
    private int clientPermissionLevel = 0;
    private double lastXClient;
    private double lastYClient;
    private double lastZClient;
    private float lastYawClient;
    private float lastPitchClient;
    private boolean lastOnGround;
    private boolean lastHorizontalCollision;
    private boolean inSneakingPose;
    private boolean lastSprinting;
    private int ticksSinceLastPositionPacketSent;
    private boolean healthInitialized;
    public Input input = new Input();
    private PlayerInput lastPlayerInput;
    protected final MinecraftClient client;
    protected int ticksLeftToDoubleTapSprint;
    public int experienceBarDisplayStartTime;
    public float renderYaw;
    public float renderPitch;
    public float lastRenderYaw;
    public float lastRenderPitch;
    private int mountJumpTicks;
    private float mountJumpStrength;
    public float nauseaIntensity;
    public float lastNauseaIntensity;
    private boolean usingItem;
    @Nullable
    private Hand activeHand;
    private boolean riding;
    private boolean autoJumpEnabled = true;
    private int ticksToNextAutoJump;
    private boolean falling;
    private int underwaterVisibilityTicks;
    private boolean showsDeathScreen = true;
    private boolean limitedCraftingEnabled = false;

    public ClientPlayerEntity(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, PlayerInput lastPlayerInput, boolean lastSprinting) {
        super(world, networkHandler.getProfile());
        this.client = client;
        this.networkHandler = networkHandler;
        this.statHandler = stats;
        this.recipeBook = recipeBook;
        this.lastPlayerInput = lastPlayerInput;
        this.lastSprinting = lastSprinting;
        this.tickables.add(new AmbientSoundPlayer(this, client.getSoundManager()));
        this.tickables.add(new BubbleColumnSoundPlayer(this));
        this.tickables.add(new BiomeEffectSoundPlayer(this, client.getSoundManager(), world.getBiomeAccess()));
    }

    @Override
    public void heal(float amount) {
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        if (!super.startRiding(entity, force, emitEvent)) {
            return false;
        }
        if (entity instanceof AbstractMinecartEntity) {
            this.client.getSoundManager().play(new MinecartInsideSoundInstance(this, (AbstractMinecartEntity)entity, true));
            this.client.getSoundManager().play(new MinecartInsideSoundInstance(this, (AbstractMinecartEntity)entity, false));
        } else if (entity instanceof HappyGhastEntity) {
            this.client.getSoundManager().play(new HappyGhastRidingSoundInstance(this, (HappyGhastEntity)entity));
        }
        return true;
    }

    @Override
    public void dismountVehicle() {
        super.dismountVehicle();
        this.riding = false;
    }

    @Override
    public float getPitch(float tickProgress) {
        return this.getPitch();
    }

    @Override
    public float getYaw(float tickProgress) {
        if (this.hasVehicle()) {
            return super.getYaw(tickProgress);
        }
        return this.getYaw();
    }

    @Override
    public void tick() {
        this.tickLoaded();
        if (!this.isLoaded()) {
            return;
        }
        this.itemDropCooldown.tick();
        super.tick();
        if (!this.lastPlayerInput.equals(this.input.playerInput)) {
            this.networkHandler.sendPacket(new PlayerInputC2SPacket(this.input.playerInput));
            this.lastPlayerInput = this.input.playerInput;
        }
        if (this.hasVehicle()) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.isOnGround(), this.horizontalCollision));
            Entity lv = this.getRootVehicle();
            if (lv != this && lv.isLogicalSideForUpdatingMovement()) {
                this.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(lv));
                this.sendSprintingPacket();
            }
        } else {
            this.sendMovementPackets();
        }
        for (ClientPlayerTickable lv2 : this.tickables) {
            lv2.tick();
        }
    }

    public float getMoodPercentage() {
        for (ClientPlayerTickable lv : this.tickables) {
            if (!(lv instanceof BiomeEffectSoundPlayer)) continue;
            return ((BiomeEffectSoundPlayer)lv).getMoodPercentage();
        }
        return 0.0f;
    }

    private void sendMovementPackets() {
        this.sendSprintingPacket();
        if (this.isCamera()) {
            boolean bl2;
            double d = this.getX() - this.lastXClient;
            double e = this.getY() - this.lastYClient;
            double f = this.getZ() - this.lastZClient;
            double g = this.getYaw() - this.lastYawClient;
            double h = this.getPitch() - this.lastPitchClient;
            ++this.ticksSinceLastPositionPacketSent;
            boolean bl = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl3 = bl2 = g != 0.0 || h != 0.0;
            if (bl && bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(this.getEntityPos(), this.getYaw(), this.getPitch(), this.isOnGround(), this.horizontalCollision));
            } else if (bl) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.getEntityPos(), this.isOnGround(), this.horizontalCollision));
            } else if (bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.isOnGround(), this.horizontalCollision));
            } else if (this.lastOnGround != this.isOnGround() || this.lastHorizontalCollision != this.horizontalCollision) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(this.isOnGround(), this.horizontalCollision));
            }
            if (bl) {
                this.lastXClient = this.getX();
                this.lastYClient = this.getY();
                this.lastZClient = this.getZ();
                this.ticksSinceLastPositionPacketSent = 0;
            }
            if (bl2) {
                this.lastYawClient = this.getYaw();
                this.lastPitchClient = this.getPitch();
            }
            this.lastOnGround = this.isOnGround();
            this.lastHorizontalCollision = this.horizontalCollision;
            this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
        }
    }

    private void sendSprintingPacket() {
        boolean bl = this.isSprinting();
        if (bl != this.lastSprinting) {
            ClientCommandC2SPacket.Mode lv = bl ? ClientCommandC2SPacket.Mode.START_SPRINTING : ClientCommandC2SPacket.Mode.STOP_SPRINTING;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, lv));
            this.lastSprinting = bl;
        }
    }

    public boolean dropSelectedItem(boolean entireStack) {
        PlayerActionC2SPacket.Action lv = entireStack ? PlayerActionC2SPacket.Action.DROP_ALL_ITEMS : PlayerActionC2SPacket.Action.DROP_ITEM;
        ItemStack lv2 = this.getInventory().dropSelectedItem(entireStack);
        this.networkHandler.sendPacket(new PlayerActionC2SPacket(lv, BlockPos.ORIGIN, Direction.DOWN));
        return !lv2.isEmpty();
    }

    @Override
    public void swingHand(Hand hand) {
        super.swingHand(hand);
        this.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
    }

    @Override
    public void requestRespawn() {
        this.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
        KeyBinding.untoggleStickyKeys();
    }

    @Override
    public void closeHandledScreen() {
        this.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(this.currentScreenHandler.syncId));
        this.closeScreen();
    }

    public void closeScreen() {
        super.closeHandledScreen();
        this.client.setScreen(null);
    }

    public void updateHealth(float health) {
        if (this.healthInitialized) {
            float g = this.getHealth() - health;
            if (g <= 0.0f) {
                this.setHealth(health);
                if (g < 0.0f) {
                    this.timeUntilRegen = 10;
                }
            } else {
                this.lastDamageTaken = g;
                this.timeUntilRegen = 20;
                this.setHealth(health);
                this.hurtTime = this.maxHurtTime = 10;
            }
        } else {
            this.setHealth(health);
            this.healthInitialized = true;
        }
    }

    @Override
    public void sendAbilitiesUpdate() {
        this.networkHandler.sendPacket(new UpdatePlayerAbilitiesC2SPacket(this.getAbilities()));
    }

    @Override
    public void setReducedDebugInfo(boolean reducedDebugInfo) {
        super.setReducedDebugInfo(reducedDebugInfo);
        this.client.debugHudEntryList.updateVisibleEntries();
    }

    @Override
    public boolean isMainPlayer() {
        return true;
    }

    @Override
    public boolean isHoldingOntoLadder() {
        return !this.getAbilities().flying && super.isHoldingOntoLadder();
    }

    @Override
    public boolean shouldSpawnSprintingParticles() {
        return !this.getAbilities().flying && super.shouldSpawnSprintingParticles();
    }

    protected void startRidingJump() {
        this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_RIDING_JUMP, MathHelper.floor(this.getMountJumpStrength() * 100.0f)));
    }

    public void openRidingInventory() {
        this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.OPEN_INVENTORY));
    }

    public StatHandler getStatHandler() {
        return this.statHandler;
    }

    public ClientRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    public void onRecipeDisplayed(NetworkRecipeId recipeId) {
        if (this.recipeBook.isHighlighted(recipeId)) {
            this.recipeBook.unmarkHighlighted(recipeId);
            this.networkHandler.sendPacket(new RecipeBookDataC2SPacket(recipeId));
        }
    }

    @Override
    public int getPermissionLevel() {
        return this.clientPermissionLevel;
    }

    public void setClientPermissionLevel(int clientPermissionLevel) {
        this.clientPermissionLevel = clientPermissionLevel;
    }

    @Override
    public void sendMessage(Text message, boolean overlay) {
        this.client.getMessageHandler().onGameMessage(message, overlay);
    }

    private void pushOutOfBlocks(double x, double z) {
        Direction[] lvs;
        BlockPos lv = BlockPos.ofFloored(x, this.getY(), z);
        if (!this.wouldCollideAt(lv)) {
            return;
        }
        double f = x - (double)lv.getX();
        double g = z - (double)lv.getZ();
        Direction lv2 = null;
        double h = Double.MAX_VALUE;
        for (Direction lv3 : lvs = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}) {
            double j;
            double i = lv3.getAxis().choose(f, 0.0, g);
            double d = j = lv3.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - i : i;
            if (!(j < h) || this.wouldCollideAt(lv.offset(lv3))) continue;
            h = j;
            lv2 = lv3;
        }
        if (lv2 != null) {
            Vec3d lv4 = this.getVelocity();
            if (lv2.getAxis() == Direction.Axis.X) {
                this.setVelocity(0.1 * (double)lv2.getOffsetX(), lv4.y, lv4.z);
            } else {
                this.setVelocity(lv4.x, lv4.y, 0.1 * (double)lv2.getOffsetZ());
            }
        }
    }

    private boolean wouldCollideAt(BlockPos pos) {
        Box lv = this.getBoundingBox();
        Box lv2 = new Box(pos.getX(), lv.minY, pos.getZ(), (double)pos.getX() + 1.0, lv.maxY, (double)pos.getZ() + 1.0).contract(1.0E-7);
        return this.getEntityWorld().canCollide(this, lv2);
    }

    public void setExperience(float progress, int total, int level) {
        this.experienceProgress = progress;
        this.totalExperience = total;
        this.experienceLevel = level;
        this.experienceBarDisplayStartTime = this.age;
    }

    @Override
    public void handleStatus(byte status) {
        if (status >= EntityStatuses.SET_OP_LEVEL_0 && status <= EntityStatuses.SET_OP_LEVEL_4) {
            this.setClientPermissionLevel(status - EntityStatuses.SET_OP_LEVEL_0);
        } else {
            super.handleStatus(status);
        }
    }

    public void setShowsDeathScreen(boolean showsDeathScreen) {
        this.showsDeathScreen = showsDeathScreen;
    }

    public boolean showsDeathScreen() {
        return this.showsDeathScreen;
    }

    public void setLimitedCraftingEnabled(boolean limitedCraftingEnabled) {
        this.limitedCraftingEnabled = limitedCraftingEnabled;
    }

    public boolean isLimitedCraftingEnabled() {
        return this.limitedCraftingEnabled;
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch, false);
    }

    @Override
    public void playSoundToPlayer(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), sound, category, volume, pitch, false);
    }

    @Override
    public void setCurrentHand(Hand hand) {
        ItemStack lv = this.getStackInHand(hand);
        if (lv.isEmpty() || this.isUsingItem()) {
            return;
        }
        super.setCurrentHand(hand);
        this.usingItem = true;
        this.activeHand = hand;
    }

    @Override
    public boolean isUsingItem() {
        return this.usingItem;
    }

    @Override
    public void clearActiveItem() {
        super.clearActiveItem();
        this.usingItem = false;
    }

    @Override
    public Hand getActiveHand() {
        return Objects.requireNonNullElse(this.activeHand, Hand.MAIN_HAND);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (LIVING_FLAGS.equals(data)) {
            Hand lv;
            boolean bl = ((Byte)this.dataTracker.get(LIVING_FLAGS) & 1) > 0;
            Hand hand = lv = ((Byte)this.dataTracker.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
            if (bl && !this.usingItem) {
                this.setCurrentHand(lv);
            } else if (!bl && this.usingItem) {
                this.clearActiveItem();
            }
        }
        if (FLAGS.equals(data) && this.isGliding() && !this.falling) {
            this.client.getSoundManager().play(new ElytraSoundInstance(this));
        }
    }

    @Nullable
    public JumpingMount getJumpingMount() {
        JumpingMount lv;
        Entity entity = this.getControllingVehicle();
        return entity instanceof JumpingMount && (lv = (JumpingMount)((Object)entity)).canJump() ? lv : null;
    }

    public float getMountJumpStrength() {
        return this.mountJumpStrength;
    }

    @Override
    public boolean shouldFilterText() {
        return this.client.shouldFilterText();
    }

    @Override
    public void openEditSignScreen(SignBlockEntity sign, boolean front) {
        if (sign instanceof HangingSignBlockEntity) {
            HangingSignBlockEntity lv = (HangingSignBlockEntity)sign;
            this.client.setScreen(new HangingSignEditScreen(lv, front, this.client.shouldFilterText()));
        } else {
            this.client.setScreen(new SignEditScreen(sign, front, this.client.shouldFilterText()));
        }
    }

    @Override
    public void openCommandBlockMinecartScreen(CommandBlockExecutor commandBlockExecutor) {
        this.client.setScreen(new MinecartCommandBlockScreen(commandBlockExecutor));
    }

    @Override
    public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
        this.client.setScreen(new CommandBlockScreen(commandBlock));
    }

    @Override
    public void openStructureBlockScreen(StructureBlockBlockEntity structureBlock) {
        this.client.setScreen(new StructureBlockScreen(structureBlock));
    }

    @Override
    public void openTestBlockScreen(TestBlockEntity testBlock) {
        this.client.setScreen(new TestBlockScreen(testBlock));
    }

    @Override
    public void openTestInstanceBlockScreen(TestInstanceBlockEntity testInstanceBlock) {
        this.client.setScreen(new TestInstanceBlockScreen(testInstanceBlock));
    }

    @Override
    public void openJigsawScreen(JigsawBlockEntity jigsaw) {
        this.client.setScreen(new JigsawBlockScreen(jigsaw));
    }

    @Override
    public void openDialog(RegistryEntry<Dialog> dialog) {
        this.networkHandler.showDialog(dialog, this.client.currentScreen);
    }

    @Override
    public void useBook(ItemStack book, Hand hand) {
        WritableBookContentComponent lv = book.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        if (lv != null) {
            this.client.setScreen(new BookEditScreen(this, book, hand, lv));
        }
    }

    @Override
    public void addCritParticles(Entity target) {
        this.client.particleManager.addEmitter(target, ParticleTypes.CRIT);
    }

    @Override
    public void addEnchantedHitParticles(Entity target) {
        this.client.particleManager.addEmitter(target, ParticleTypes.ENCHANTED_HIT);
    }

    @Override
    public boolean isSneaking() {
        return this.input.playerInput.sneak();
    }

    @Override
    public boolean isInSneakingPose() {
        return this.inSneakingPose;
    }

    public boolean shouldSlowDown() {
        return this.isInSneakingPose() || this.isCrawling();
    }

    @Override
    public void tickMovementInput() {
        if (this.isCamera()) {
            Vec2f lv = this.applyMovementSpeedFactors(this.input.getMovementInput());
            this.sidewaysSpeed = lv.x;
            this.forwardSpeed = lv.y;
            this.jumping = this.input.playerInput.jump();
            this.lastRenderYaw = this.renderYaw;
            this.lastRenderPitch = this.renderPitch;
            this.renderPitch += (this.getPitch() - this.renderPitch) * 0.5f;
            this.renderYaw += (this.getYaw() - this.renderYaw) * 0.5f;
        } else {
            super.tickMovementInput();
        }
    }

    private Vec2f applyMovementSpeedFactors(Vec2f input) {
        if (input.lengthSquared() == 0.0f) {
            return input;
        }
        Vec2f lv = input.multiply(0.98f);
        if (this.isUsingItem() && !this.hasVehicle()) {
            lv = lv.multiply(0.2f);
        }
        if (this.shouldSlowDown()) {
            float f = (float)this.getAttributeValue(EntityAttributes.SNEAKING_SPEED);
            lv = lv.multiply(f);
        }
        return ClientPlayerEntity.applyDirectionalMovementSpeedFactors(lv);
    }

    private static Vec2f applyDirectionalMovementSpeedFactors(Vec2f vec) {
        float f = vec.length();
        if (f <= 0.0f) {
            return vec;
        }
        Vec2f lv = vec.multiply(1.0f / f);
        float g = ClientPlayerEntity.getDirectionalMovementSpeedMultiplier(lv);
        float h = Math.min(f * g, 1.0f);
        return lv.multiply(h);
    }

    private static float getDirectionalMovementSpeedMultiplier(Vec2f vec) {
        float f = Math.abs(vec.x);
        float g = Math.abs(vec.y);
        float h = g > f ? f / g : g / f;
        return MathHelper.sqrt(1.0f + MathHelper.square(h));
    }

    protected boolean isCamera() {
        return this.client.getCameraEntity() == this;
    }

    public void init() {
        this.setPose(EntityPose.STANDING);
        if (this.getEntityWorld() != null) {
            for (double d = this.getY(); d > (double)this.getEntityWorld().getBottomY() && d <= (double)this.getEntityWorld().getTopYInclusive(); d += 1.0) {
                this.setPosition(this.getX(), d, this.getZ());
                if (this.getEntityWorld().isSpaceEmpty(this)) break;
            }
            this.setVelocity(Vec3d.ZERO);
            this.setPitch(0.0f);
        }
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    @Override
    public void tickMovement() {
        JumpingMount lv2;
        int i;
        if (this.ticksLeftToDoubleTapSprint > 0) {
            --this.ticksLeftToDoubleTapSprint;
        }
        if (!(this.client.currentScreen instanceof LevelLoadingScreen)) {
            this.tickNausea(this.getCurrentPortalEffect() == Portal.Effect.CONFUSION);
            this.tickPortalCooldown();
        }
        boolean bl = this.input.playerInput.jump();
        boolean bl2 = this.input.playerInput.sneak();
        boolean bl3 = this.input.hasForwardMovement();
        PlayerAbilities lv = this.getAbilities();
        this.inSneakingPose = !lv.flying && !this.isSwimming() && !this.hasVehicle() && this.canChangeIntoPose(EntityPose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.canChangeIntoPose(EntityPose.STANDING));
        this.input.tick();
        this.client.getTutorialManager().onMovement(this.input);
        boolean bl4 = false;
        if (this.ticksToNextAutoJump > 0) {
            --this.ticksToNextAutoJump;
            bl4 = true;
            this.input.jump();
        }
        if (!this.noClip) {
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
        }
        if (bl2 || this.isUsingItem() && !this.hasVehicle() || this.input.playerInput.backward()) {
            this.ticksLeftToDoubleTapSprint = 0;
        }
        if (this.canStartSprinting()) {
            if (!bl3) {
                if (this.ticksLeftToDoubleTapSprint > 0) {
                    this.setSprinting(true);
                } else {
                    this.ticksLeftToDoubleTapSprint = this.client.options.getSprintWindow().getValue();
                }
            }
            if (this.input.playerInput.sprint()) {
                this.setSprinting(true);
            }
        }
        if (this.isSprinting()) {
            if (this.isSwimming()) {
                if (this.shouldStopSwimSprinting()) {
                    this.setSprinting(false);
                }
            } else if (this.shouldStopSprinting()) {
                this.setSprinting(false);
            }
        }
        boolean bl5 = false;
        if (lv.allowFlying) {
            if (this.client.interactionManager.isFlyingLocked()) {
                if (!lv.flying) {
                    lv.flying = true;
                    bl5 = true;
                    this.sendAbilitiesUpdate();
                }
            } else if (!bl && this.input.playerInput.jump() && !bl4) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!this.isSwimming()) {
                    boolean bl6 = lv.flying = !lv.flying;
                    if (lv.flying && this.isOnGround()) {
                        this.jump();
                    }
                    bl5 = true;
                    this.sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }
        if (this.input.playerInput.jump() && !bl5 && !bl && !this.isClimbing() && this.checkGliding()) {
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
        this.falling = this.isGliding();
        if (this.isTouchingWater() && this.input.playerInput.sneak() && this.shouldSwimInFluids()) {
            this.knockDownwards();
        }
        if (this.isSubmergedIn(FluidTags.WATER)) {
            i = this.isSpectator() ? 10 : 1;
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + i, 0, 600);
        } else if (this.underwaterVisibilityTicks > 0) {
            this.isSubmergedIn(FluidTags.WATER);
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
        }
        if (lv.flying && this.isCamera()) {
            i = 0;
            if (this.input.playerInput.sneak()) {
                --i;
            }
            if (this.input.playerInput.jump()) {
                ++i;
            }
            if (i != 0) {
                this.setVelocity(this.getVelocity().add(0.0, (float)i * lv.getFlySpeed() * 3.0f, 0.0));
            }
        }
        if ((lv2 = this.getJumpingMount()) != null && lv2.getJumpCooldown() == 0) {
            if (this.mountJumpTicks < 0) {
                ++this.mountJumpTicks;
                if (this.mountJumpTicks == 0) {
                    this.mountJumpStrength = 0.0f;
                }
            }
            if (bl && !this.input.playerInput.jump()) {
                this.mountJumpTicks = -10;
                lv2.setJumpStrength(MathHelper.floor(this.getMountJumpStrength() * 100.0f));
                this.startRidingJump();
            } else if (!bl && this.input.playerInput.jump()) {
                this.mountJumpTicks = 0;
                this.mountJumpStrength = 0.0f;
            } else if (bl) {
                ++this.mountJumpTicks;
                this.mountJumpStrength = this.mountJumpTicks < 10 ? (float)this.mountJumpTicks * 0.1f : 0.8f + 2.0f / (float)(this.mountJumpTicks - 9) * 0.1f;
            }
        } else {
            this.mountJumpStrength = 0.0f;
        }
        super.tickMovement();
        if (this.isOnGround() && lv.flying && !this.client.interactionManager.isFlyingLocked()) {
            lv.flying = false;
            this.sendAbilitiesUpdate();
        }
    }

    private boolean shouldStopSprinting() {
        return !this.canSprint(this.getAbilities().flying) || !this.input.hasForwardMovement() || this.horizontalCollision && !this.collidedSoftly;
    }

    private boolean shouldStopSwimSprinting() {
        return !this.canSprint(true) || !this.isTouchingWater() || !this.input.hasForwardMovement() && !this.isOnGround() && !this.input.playerInput.sneak();
    }

    public Portal.Effect getCurrentPortalEffect() {
        return this.portalManager == null ? Portal.Effect.NONE : this.portalManager.getEffect();
    }

    @Override
    protected void updatePostDeath() {
        ++this.deathTime;
        if (this.deathTime == 20) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    private void tickNausea(boolean fromPortalEffect) {
        this.lastNauseaIntensity = this.nauseaIntensity;
        float f = 0.0f;
        if (fromPortalEffect && this.portalManager != null && this.portalManager.isInPortal()) {
            if (this.client.currentScreen != null && !this.client.currentScreen.keepOpenThroughPortal()) {
                if (this.client.currentScreen instanceof HandledScreen) {
                    this.closeHandledScreen();
                }
                this.client.setScreen(null);
            }
            if (this.nauseaIntensity == 0.0f) {
                this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRIGGER, this.random.nextFloat() * 0.4f + 0.8f, 0.25f));
            }
            f = 0.0125f;
            this.portalManager.setInPortal(false);
        } else if (this.nauseaIntensity > 0.0f) {
            f = -0.05f;
        }
        this.nauseaIntensity = MathHelper.clamp(this.nauseaIntensity + f, 0.0f, 1.0f);
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        this.riding = false;
        Entity entity = this.getControllingVehicle();
        if (entity instanceof AbstractBoatEntity) {
            AbstractBoatEntity lv = (AbstractBoatEntity)entity;
            lv.setInputs(this.input.playerInput.left(), this.input.playerInput.right(), this.input.playerInput.forward(), this.input.playerInput.backward());
            this.riding |= this.input.playerInput.left() || this.input.playerInput.right() || this.input.playerInput.forward() || this.input.playerInput.backward();
        }
    }

    public boolean isRiding() {
        return this.riding;
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        double d = this.getX();
        double e = this.getZ();
        super.move(type, movement);
        float f = (float)(this.getX() - d);
        float g = (float)(this.getZ() - e);
        this.autoJump(f, g);
        this.addDistanceMoved(MathHelper.hypot(f, g) * 0.6f);
    }

    public boolean isAutoJumpEnabled() {
        return this.autoJumpEnabled;
    }

    @Override
    public boolean shouldRotateWithMinecart() {
        return this.client.options.getRotateWithMinecart().getValue();
    }

    protected void autoJump(float dx, float dz) {
        float l;
        if (!this.shouldAutoJump()) {
            return;
        }
        Vec3d lv = this.getEntityPos();
        Vec3d lv2 = lv.add(dx, 0.0, dz);
        Vec3d lv3 = new Vec3d(dx, 0.0, dz);
        float h = this.getMovementSpeed();
        float i = (float)lv3.lengthSquared();
        if (i <= 0.001f) {
            Vec2f lv4 = this.input.getMovementInput();
            float j = h * lv4.x;
            float k = h * lv4.y;
            l = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
            float m = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
            lv3 = new Vec3d(j * m - k * l, lv3.y, k * m + j * l);
            i = (float)lv3.lengthSquared();
            if (i <= 0.001f) {
                return;
            }
        }
        float n = MathHelper.inverseSqrt(i);
        Vec3d lv5 = lv3.multiply(n);
        Vec3d lv6 = this.getRotationVecClient();
        l = (float)(lv6.x * lv5.x + lv6.z * lv5.z);
        if (l < -0.15f) {
            return;
        }
        ShapeContext lv7 = ShapeContext.of(this);
        BlockPos lv8 = BlockPos.ofFloored(this.getX(), this.getBoundingBox().maxY, this.getZ());
        BlockState lv9 = this.getEntityWorld().getBlockState(lv8);
        if (!lv9.getCollisionShape(this.getEntityWorld(), lv8, lv7).isEmpty()) {
            return;
        }
        lv8 = lv8.up();
        BlockState lv10 = this.getEntityWorld().getBlockState(lv8);
        if (!lv10.getCollisionShape(this.getEntityWorld(), lv8, lv7).isEmpty()) {
            return;
        }
        float o = 7.0f;
        float p = 1.2f;
        if (this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            p += (float)(this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.75f;
        }
        float q = Math.max(h * 7.0f, 1.0f / n);
        Vec3d lv11 = lv;
        Vec3d lv12 = lv2.add(lv5.multiply(q));
        float r = this.getWidth();
        float s = this.getHeight();
        Box lv13 = new Box(lv11, lv12.add(0.0, s, 0.0)).expand(r, 0.0, r);
        lv11 = lv11.add(0.0, 0.51f, 0.0);
        lv12 = lv12.add(0.0, 0.51f, 0.0);
        Vec3d lv14 = lv5.crossProduct(new Vec3d(0.0, 1.0, 0.0));
        Vec3d lv15 = lv14.multiply(r * 0.5f);
        Vec3d lv16 = lv11.subtract(lv15);
        Vec3d lv17 = lv12.subtract(lv15);
        Vec3d lv18 = lv11.add(lv15);
        Vec3d lv19 = lv12.add(lv15);
        Iterable<VoxelShape> iterable = this.getEntityWorld().getCollisions(this, lv13);
        Iterator iterator = StreamSupport.stream(iterable.spliterator(), false).flatMap(shape -> shape.getBoundingBoxes().stream()).iterator();
        float t = Float.MIN_VALUE;
        while (iterator.hasNext()) {
            Box lv20 = (Box)iterator.next();
            if (!lv20.intersects(lv16, lv17) && !lv20.intersects(lv18, lv19)) continue;
            t = (float)lv20.maxY;
            Vec3d lv21 = lv20.getCenter();
            BlockPos lv22 = BlockPos.ofFloored(lv21);
            int u = 1;
            while ((float)u < p) {
                BlockPos lv23 = lv22.up(u);
                BlockState lv24 = this.getEntityWorld().getBlockState(lv23);
                VoxelShape lv25 = lv24.getCollisionShape(this.getEntityWorld(), lv23, lv7);
                if (!lv25.isEmpty() && (double)(t = (float)lv25.getMax(Direction.Axis.Y) + (float)lv23.getY()) - this.getY() > (double)p) {
                    return;
                }
                if (u > 1) {
                    lv8 = lv8.up();
                    BlockState lv26 = this.getEntityWorld().getBlockState(lv8);
                    if (!lv26.getCollisionShape(this.getEntityWorld(), lv8, lv7).isEmpty()) {
                        return;
                    }
                }
                ++u;
            }
            break block0;
        }
        if (t == Float.MIN_VALUE) {
            return;
        }
        float v = (float)((double)t - this.getY());
        if (v <= 0.5f || v > p) {
            return;
        }
        this.ticksToNextAutoJump = 1;
    }

    @Override
    protected boolean hasCollidedSoftly(Vec3d adjustedMovement) {
        float f = this.getYaw() * ((float)Math.PI / 180);
        double d = MathHelper.sin(f);
        double e = MathHelper.cos(f);
        double g = (double)this.sidewaysSpeed * e - (double)this.forwardSpeed * d;
        double h = (double)this.forwardSpeed * e + (double)this.sidewaysSpeed * d;
        double i = MathHelper.square(g) + MathHelper.square(h);
        double j = MathHelper.square(adjustedMovement.x) + MathHelper.square(adjustedMovement.z);
        if (i < (double)1.0E-5f || j < (double)1.0E-5f) {
            return false;
        }
        double k = g * adjustedMovement.x + h * adjustedMovement.z;
        double l = Math.acos(k / Math.sqrt(i * j));
        return l < 0.13962633907794952;
    }

    private boolean shouldAutoJump() {
        return this.isAutoJumpEnabled() && this.ticksToNextAutoJump <= 0 && this.isOnGround() && !this.clipAtLedge() && !this.hasVehicle() && this.hasMovementInput() && (double)this.getJumpVelocityMultiplier() >= 1.0;
    }

    private boolean hasMovementInput() {
        return this.input.getMovementInput().lengthSquared() > 0.0f;
    }

    private boolean canSprint(boolean allowTouchingWater) {
        return !(this.hasBlindnessEffect() || !this.canSprint() || this.hasVehicle() && !this.canVehicleSprint(this.getVehicle()) || !allowTouchingWater && this.isPartlyTouchingWater());
    }

    private boolean canStartSprinting() {
        return !(this.isSprinting() || !this.input.hasForwardMovement() || !this.canSprint(this.getAbilities().flying) || this.isUsingItem() || this.isGliding() && !this.isSubmergedInWater() || this.shouldSlowDown() && !this.isSubmergedInWater());
    }

    private boolean canVehicleSprint(Entity vehicle) {
        return vehicle.canSprintAsVehicle() && vehicle.isLogicalSideForUpdatingMovement();
    }

    private boolean canSprint() {
        return this.hasVehicle() || (float)this.getHungerManager().getFoodLevel() > 6.0f || this.getAbilities().allowFlying;
    }

    public float getUnderwaterVisibility() {
        if (!this.isSubmergedIn(FluidTags.WATER)) {
            return 0.0f;
        }
        float f = 600.0f;
        float g = 100.0f;
        if ((float)this.underwaterVisibilityTicks >= 600.0f) {
            return 1.0f;
        }
        float h = MathHelper.clamp((float)this.underwaterVisibilityTicks / 100.0f, 0.0f, 1.0f);
        float i = (float)this.underwaterVisibilityTicks < 100.0f ? 0.0f : MathHelper.clamp(((float)this.underwaterVisibilityTicks - 100.0f) / 500.0f, 0.0f, 1.0f);
        return h * 0.6f + i * 0.39999998f;
    }

    public void onGameModeChanged(GameMode gameMode) {
        if (gameMode == GameMode.SPECTATOR) {
            this.setVelocity(this.getVelocity().withAxis(Direction.Axis.Y, 0.0));
        }
    }

    @Override
    public boolean isSubmergedInWater() {
        return this.isSubmergedInWater;
    }

    @Override
    protected boolean updateWaterSubmersionState() {
        boolean bl = this.isSubmergedInWater;
        boolean bl2 = super.updateWaterSubmersionState();
        if (this.isSpectator()) {
            return this.isSubmergedInWater;
        }
        if (!bl && bl2) {
            this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
            this.client.getSoundManager().play(new AmbientSoundLoops.Underwater(this));
        }
        if (bl && !bl2) {
            this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
        }
        return this.isSubmergedInWater;
    }

    @Override
    public Vec3d getLeashPos(float tickProgress) {
        if (this.client.options.getPerspective().isFirstPerson()) {
            float g = MathHelper.lerp(tickProgress * 0.5f, this.getYaw(), this.lastYaw) * ((float)Math.PI / 180);
            float h = MathHelper.lerp(tickProgress * 0.5f, this.getPitch(), this.lastPitch) * ((float)Math.PI / 180);
            double d = this.getMainArm() == Arm.RIGHT ? -1.0 : 1.0;
            Vec3d lv = new Vec3d(0.39 * d, -0.6, 0.3);
            return lv.rotateX(-h).rotateY(-g).add(this.getCameraPosVec(tickProgress));
        }
        return super.getLeashPos(tickProgress);
    }

    @Override
    public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
        this.client.getTutorialManager().onPickupSlotClick(cursorStack, slotStack, clickType);
    }

    @Override
    public float getBodyYaw() {
        return this.getYaw();
    }

    @Override
    public void dropCreativeStack(ItemStack stack) {
        this.client.interactionManager.dropCreativeStack(stack);
    }

    @Override
    public boolean canDropItems() {
        return this.itemDropCooldown.canUse();
    }

    public Cooldown getItemDropCooldown() {
        return this.itemDropCooldown;
    }

    public PlayerInput getLastPlayerInput() {
        return this.lastPlayerInput;
    }
}

