/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.FurnaceFuelSlot;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class AbstractFurnaceScreenHandler
extends AbstractRecipeScreenHandler {
    public static final int field_30738 = 0;
    public static final int field_30739 = 1;
    public static final int field_30740 = 2;
    public static final int field_30741 = 3;
    public static final int field_30742 = 4;
    private static final int field_30743 = 3;
    private static final int field_30744 = 30;
    private static final int field_30745 = 30;
    private static final int field_30746 = 39;
    final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    protected final World world;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;
    private final RecipePropertySet recipePropertySet;
    private final RecipeBookType category;

    protected AbstractFurnaceScreenHandler(ScreenHandlerType<?> type, RecipeType<? extends AbstractCookingRecipe> recipeType, RegistryKey<RecipePropertySet> recipePropertySetKey, RecipeBookType category, int syncId, PlayerInventory playerInventory) {
        this(type, recipeType, recipePropertySetKey, category, syncId, playerInventory, new SimpleInventory(3), new ArrayPropertyDelegate(4));
    }

    protected AbstractFurnaceScreenHandler(ScreenHandlerType<?> type, RecipeType<? extends AbstractCookingRecipe> recipeType, RegistryKey<RecipePropertySet> recipePropertySetKey, RecipeBookType category, int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(type, syncId);
        this.recipeType = recipeType;
        this.category = category;
        AbstractFurnaceScreenHandler.checkSize(inventory, 3);
        AbstractFurnaceScreenHandler.checkDataCount(propertyDelegate, 4);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.world = playerInventory.player.getEntityWorld();
        this.recipePropertySet = this.world.getRecipeManager().getPropertySet(recipePropertySetKey);
        this.addSlot(new Slot(inventory, 0, 56, 17));
        this.addSlot(new FurnaceFuelSlot(this, inventory, 1, 56, 53));
        this.addSlot(new FurnaceOutputSlot(playerInventory.player, inventory, 2, 116, 35));
        this.addPlayerSlots(playerInventory, 8, 84);
        this.addProperties(propertyDelegate);
    }

    @Override
    public void populateRecipeFinder(RecipeFinder finder) {
        if (this.inventory instanceof RecipeInputProvider) {
            ((RecipeInputProvider)((Object)this.inventory)).provideRecipeInputs(finder);
        }
    }

    public Slot getOutputSlot() {
        return (Slot)this.slots.get(2);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot == 2) {
                if (!this.insertItem(lv3, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (slot == 1 || slot == 0 ? !this.insertItem(lv3, 3, 39, false) : (this.isSmeltable(lv3) ? !this.insertItem(lv3, 0, 1, false) : (this.isFuel(lv3) ? !this.insertItem(lv3, 1, 2, false) : (slot >= 3 && slot < 30 ? !this.insertItem(lv3, 30, 39, false) : slot >= 30 && slot < 39 && !this.insertItem(lv3, 3, 30, false))))) {
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStack(ItemStack.EMPTY);
            } else {
                lv2.markDirty();
            }
            if (lv3.getCount() == lv.getCount()) {
                return ItemStack.EMPTY;
            }
            lv2.onTakeItem(player, lv3);
        }
        return lv;
    }

    protected boolean isSmeltable(ItemStack itemStack) {
        return this.recipePropertySet.canUse(itemStack);
    }

    protected boolean isFuel(ItemStack item) {
        return this.world.getFuelRegistry().isFuel(item);
    }

    public float getCookProgress() {
        int i = this.propertyDelegate.get(2);
        int j = this.propertyDelegate.get(3);
        if (j == 0 || i == 0) {
            return 0.0f;
        }
        return MathHelper.clamp((float)i / (float)j, 0.0f, 1.0f);
    }

    public float getFuelProgress() {
        int i = this.propertyDelegate.get(1);
        if (i == 0) {
            i = 200;
        }
        return MathHelper.clamp((float)this.propertyDelegate.get(0) / (float)i, 0.0f, 1.0f);
    }

    public boolean isBurning() {
        return this.propertyDelegate.get(0) > 0;
    }

    @Override
    public RecipeBookType getCategory() {
        return this.category;
    }

    @Override
    public AbstractRecipeScreenHandler.PostFillAction fillInputSlots(boolean craftAll, boolean creative, RecipeEntry<?> recipe, final ServerWorld world, PlayerInventory inventory) {
        final List<Slot> list = List.of(this.getSlot(0), this.getSlot(2));
        RecipeEntry<?> lv = recipe;
        return InputSlotFiller.fill(new InputSlotFiller.Handler<AbstractCookingRecipe>(){

            @Override
            public void populateRecipeFinder(RecipeFinder finder) {
                AbstractFurnaceScreenHandler.this.populateRecipeFinder(finder);
            }

            @Override
            public void clear() {
                list.forEach(slot -> slot.setStackNoCallbacks(ItemStack.EMPTY));
            }

            @Override
            public boolean matches(RecipeEntry<AbstractCookingRecipe> entry) {
                return entry.value().matches(new SingleStackRecipeInput(AbstractFurnaceScreenHandler.this.inventory.getStack(0)), (World)world);
            }
        }, 1, 1, List.of(this.getSlot(0)), list, inventory, lv, craftAll, creative);
    }
}

