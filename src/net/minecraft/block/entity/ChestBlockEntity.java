/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestLidAnimator;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.ContainerUser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ChestBlockEntity
extends LootableContainerBlockEntity
implements LidOpenable {
    private static final int VIEWER_COUNT_UPDATE_EVENT_TYPE = 1;
    private static final Text CONTAINER_NAME_TEXT = Text.translatable("container.chest");
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
    private final ViewerCountManager stateManager = new ViewerCountManager(){

        @Override
        protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
            Block block = state.getBlock();
            if (block instanceof ChestBlock) {
                ChestBlock lv = (ChestBlock)block;
                ChestBlockEntity.playSound(world, pos, state, lv.getOpenSound());
            }
        }

        @Override
        protected void onContainerClose(World world, BlockPos pos, BlockState state) {
            Block block = state.getBlock();
            if (block instanceof ChestBlock) {
                ChestBlock lv = (ChestBlock)block;
                ChestBlockEntity.playSound(world, pos, state, lv.getCloseSound());
            }
        }

        @Override
        protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
            ChestBlockEntity.this.onViewerCountUpdate(world, pos, state, oldViewerCount, newViewerCount);
        }

        @Override
        public boolean isPlayerViewing(PlayerEntity player) {
            if (player.currentScreenHandler instanceof GenericContainerScreenHandler) {
                Inventory lv = ((GenericContainerScreenHandler)player.currentScreenHandler).getInventory();
                return lv == ChestBlockEntity.this || lv instanceof DoubleInventory && ((DoubleInventory)lv).isPart(ChestBlockEntity.this);
            }
            return false;
        }
    };
    private final ChestLidAnimator lidAnimator = new ChestLidAnimator();

    protected ChestBlockEntity(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    public ChestBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntityType.CHEST, pos, state);
    }

    @Override
    public int size() {
        return 27;
    }

    @Override
    protected Text getContainerName() {
        return CONTAINER_NAME_TEXT;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(view)) {
            Inventories.readData(view, this.inventory);
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.writeLootTable(view)) {
            Inventories.writeData(view, this.inventory);
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, ChestBlockEntity blockEntity) {
        blockEntity.lidAnimator.step();
    }

    static void playSound(World world, BlockPos pos, BlockState state, SoundEvent soundEvent) {
        ChestType lv = state.get(ChestBlock.CHEST_TYPE);
        if (lv == ChestType.LEFT) {
            return;
        }
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getY() + 0.5;
        double f = (double)pos.getZ() + 0.5;
        if (lv == ChestType.RIGHT) {
            Direction lv2 = ChestBlock.getFacing(state);
            d += (double)lv2.getOffsetX() * 0.5;
            f += (double)lv2.getOffsetZ() * 0.5;
        }
        world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.lidAnimator.setOpen(data > 0);
            return true;
        }
        return super.onSyncedBlockEvent(type, data);
    }

    @Override
    public void onOpen(ContainerUser user) {
        if (!this.removed && !user.asLivingEntity().isSpectator()) {
            this.stateManager.openContainer(user.asLivingEntity(), this.getWorld(), this.getPos(), this.getCachedState(), user.getContainerInteractionRange());
        }
    }

    @Override
    public void onClose(ContainerUser user) {
        if (!this.removed && !user.asLivingEntity().isSpectator()) {
            this.stateManager.closeContainer(user.asLivingEntity(), this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    @Override
    public List<ContainerUser> getViewingUsers() {
        return this.stateManager.getViewingUsers(this.getWorld(), this.getPos());
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    public float getAnimationProgress(float tickProgress) {
        return this.lidAnimator.getProgress(tickProgress);
    }

    public static int getPlayersLookingInChestCount(BlockView world, BlockPos pos) {
        BlockEntity lv2;
        BlockState lv = world.getBlockState(pos);
        if (lv.hasBlockEntity() && (lv2 = world.getBlockEntity(pos)) instanceof ChestBlockEntity) {
            return ((ChestBlockEntity)lv2).stateManager.getViewerCount();
        }
        return 0;
    }

    public static void copyInventory(ChestBlockEntity from, ChestBlockEntity to) {
        DefaultedList<ItemStack> lv = from.getHeldStacks();
        from.setHeldStacks(to.getHeldStacks());
        to.setHeldStacks(lv);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
    }

    public void onScheduledTick() {
        if (!this.removed) {
            this.stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        Block lv = state.getBlock();
        world.addSyncedBlockEvent(pos, lv, 1, newViewerCount);
    }
}

