/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.recipebook;

import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.recipebook.GhostRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.recipebook.RecipeBookType;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapedCraftingRecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameterMap;

@Environment(value=EnvType.CLIENT)
public class CraftingRecipeBookWidget
extends RecipeBookWidget<AbstractCraftingScreenHandler> {
    private static final ButtonTextures FILTER_BUTTON_TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/filter_enabled"), Identifier.ofVanilla("recipe_book/filter_disabled"), Identifier.ofVanilla("recipe_book/filter_enabled_highlighted"), Identifier.ofVanilla("recipe_book/filter_disabled_highlighted"));
    private static final Text TOGGLE_CRAFTABLE_TEXT = Text.translatable("gui.recipebook.toggleRecipes.craftable");
    private static final List<RecipeBookWidget.Tab> TABS = List.of(new RecipeBookWidget.Tab(RecipeBookType.CRAFTING), new RecipeBookWidget.Tab(Items.IRON_AXE, Items.GOLDEN_SWORD, RecipeBookCategories.CRAFTING_EQUIPMENT), new RecipeBookWidget.Tab(Items.BRICKS, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS), new RecipeBookWidget.Tab(Items.LAVA_BUCKET, Items.APPLE, RecipeBookCategories.CRAFTING_MISC), new RecipeBookWidget.Tab(Items.REDSTONE, RecipeBookCategories.CRAFTING_REDSTONE));

    public CraftingRecipeBookWidget(AbstractCraftingScreenHandler screenHandler) {
        super(screenHandler, TABS);
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        return ((AbstractCraftingScreenHandler)this.craftingScreenHandler).getOutputSlot() == slot || ((AbstractCraftingScreenHandler)this.craftingScreenHandler).getInputSlots().contains(slot);
    }

    private boolean canDisplay(RecipeDisplay display) {
        int i = ((AbstractCraftingScreenHandler)this.craftingScreenHandler).getWidth();
        int j = ((AbstractCraftingScreenHandler)this.craftingScreenHandler).getHeight();
        RecipeDisplay recipeDisplay = display;
        Objects.requireNonNull(recipeDisplay);
        RecipeDisplay recipeDisplay2 = recipeDisplay;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class}, (Object)recipeDisplay2, n)) {
            case 0 -> {
                ShapedCraftingRecipeDisplay lv = (ShapedCraftingRecipeDisplay)recipeDisplay2;
                if (i >= lv.width() && j >= lv.height()) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                ShapelessCraftingRecipeDisplay lv2 = (ShapelessCraftingRecipeDisplay)recipeDisplay2;
                if (i * j >= lv2.ingredients().size()) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    protected void showGhostRecipe(GhostRecipe ghostRecipe, RecipeDisplay display, ContextParameterMap context) {
        ghostRecipe.addResults(((AbstractCraftingScreenHandler)this.craftingScreenHandler).getOutputSlot(), context, display.result());
        RecipeDisplay recipeDisplay = display;
        Objects.requireNonNull(recipeDisplay);
        RecipeDisplay recipeDisplay2 = recipeDisplay;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class}, (Object)recipeDisplay2, n)) {
            case 0: {
                ShapedCraftingRecipeDisplay lv = (ShapedCraftingRecipeDisplay)recipeDisplay2;
                List<Slot> list = ((AbstractCraftingScreenHandler)this.craftingScreenHandler).getInputSlots();
                RecipeGridAligner.alignRecipeToGrid(((AbstractCraftingScreenHandler)this.craftingScreenHandler).getWidth(), ((AbstractCraftingScreenHandler)this.craftingScreenHandler).getHeight(), lv.width(), lv.height(), lv.ingredients(), (slot, index, x, y) -> {
                    Slot lv = (Slot)list.get(index);
                    ghostRecipe.addInputs(lv, context, (SlotDisplay)slot);
                });
                break;
            }
            case 1: {
                ShapelessCraftingRecipeDisplay lv2 = (ShapelessCraftingRecipeDisplay)recipeDisplay2;
                List<Slot> list2 = ((AbstractCraftingScreenHandler)this.craftingScreenHandler).getInputSlots();
                int i = Math.min(lv2.ingredients().size(), list2.size());
                for (int j = 0; j < i; ++j) {
                    ghostRecipe.addInputs(list2.get(j), context, lv2.ingredients().get(j));
                }
                break;
            }
        }
    }

    @Override
    protected void setBookButtonTexture() {
        this.toggleCraftableButton.setTextures(FILTER_BUTTON_TEXTURES);
    }

    @Override
    protected Text getToggleCraftableButtonText() {
        return TOGGLE_CRAFTABLE_TEXT;
    }

    @Override
    protected void populateRecipes(RecipeResultCollection recipeResultCollection, RecipeFinder recipeFinder) {
        recipeResultCollection.populateRecipes(recipeFinder, this::canDisplay);
    }
}

