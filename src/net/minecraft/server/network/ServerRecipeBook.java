/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.network.packet.s2c.play.RecipeBookAddS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookSettingsS2CPacket;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.recipe.book.RecipeBookOptions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;

public class ServerRecipeBook
extends RecipeBook {
    public static final String RECIPE_BOOK_KEY = "recipeBook";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DisplayCollector collector;
    @VisibleForTesting
    protected final Set<RegistryKey<Recipe<?>>> unlocked = Sets.newIdentityHashSet();
    @VisibleForTesting
    protected final Set<RegistryKey<Recipe<?>>> highlighted = Sets.newIdentityHashSet();

    public ServerRecipeBook(DisplayCollector collector) {
        this.collector = collector;
    }

    public void unlock(RegistryKey<Recipe<?>> recipeKey) {
        this.unlocked.add(recipeKey);
    }

    public boolean isUnlocked(RegistryKey<Recipe<?>> recipeKey) {
        return this.unlocked.contains(recipeKey);
    }

    public void lock(RegistryKey<Recipe<?>> recipeKey) {
        this.unlocked.remove(recipeKey);
        this.highlighted.remove(recipeKey);
    }

    public void unmarkHighlighted(RegistryKey<Recipe<?>> recipeKey) {
        this.highlighted.remove(recipeKey);
    }

    private void markHighlighted(RegistryKey<Recipe<?>> recipeKey) {
        this.highlighted.add(recipeKey);
    }

    public int unlockRecipes(Collection<RecipeEntry<?>> recipes, ServerPlayerEntity player) {
        ArrayList<RecipeBookAddS2CPacket.Entry> list = new ArrayList<RecipeBookAddS2CPacket.Entry>();
        for (RecipeEntry<?> lv : recipes) {
            RegistryKey<Recipe<?>> lv2 = lv.id();
            if (this.unlocked.contains(lv2) || lv.value().isIgnoredInRecipeBook()) continue;
            this.unlock(lv2);
            this.markHighlighted(lv2);
            this.collector.displaysForRecipe(lv2, display -> list.add(new RecipeBookAddS2CPacket.Entry((RecipeDisplayEntry)display, lv.value().showNotification(), true)));
            Criteria.RECIPE_UNLOCKED.trigger(player, lv);
        }
        if (!list.isEmpty()) {
            player.networkHandler.sendPacket(new RecipeBookAddS2CPacket(list, false));
        }
        return list.size();
    }

    public int lockRecipes(Collection<RecipeEntry<?>> recipes, ServerPlayerEntity player) {
        ArrayList<NetworkRecipeId> list = Lists.newArrayList();
        for (RecipeEntry<?> lv : recipes) {
            RegistryKey<Recipe<?>> lv2 = lv.id();
            if (!this.unlocked.contains(lv2)) continue;
            this.lock(lv2);
            this.collector.displaysForRecipe(lv2, display -> list.add(display.id()));
        }
        if (!list.isEmpty()) {
            player.networkHandler.sendPacket(new RecipeBookRemoveS2CPacket(list));
        }
        return list.size();
    }

    private void handleList(List<RegistryKey<Recipe<?>>> recipes, Consumer<RegistryKey<Recipe<?>>> handler, Predicate<RegistryKey<Recipe<?>>> validPredicate) {
        for (RegistryKey<Recipe<?>> lv : recipes) {
            if (!validPredicate.test(lv)) {
                LOGGER.error("Tried to load unrecognized recipe: {} removed now.", (Object)lv);
                continue;
            }
            handler.accept(lv);
        }
    }

    public void sendInitRecipesPacket(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new RecipeBookSettingsS2CPacket(this.getOptions().copy()));
        ArrayList<RecipeBookAddS2CPacket.Entry> list = new ArrayList<RecipeBookAddS2CPacket.Entry>(this.unlocked.size());
        for (RegistryKey<Recipe<?>> lv : this.unlocked) {
            this.collector.displaysForRecipe(lv, display -> list.add(new RecipeBookAddS2CPacket.Entry((RecipeDisplayEntry)display, false, this.highlighted.contains(lv))));
        }
        player.networkHandler.sendPacket(new RecipeBookAddS2CPacket(list, true));
    }

    public void copyFrom(ServerRecipeBook recipeBook) {
        this.unpack(recipeBook.pack());
    }

    public Packed pack() {
        return new Packed(this.options.copy(), List.copyOf(this.unlocked), List.copyOf(this.highlighted));
    }

    private void unpack(Packed packed) {
        this.unlocked.clear();
        this.highlighted.clear();
        this.options.copyFrom(packed.settings);
        this.unlocked.addAll(packed.known);
        this.highlighted.addAll(packed.highlight);
    }

    public void unpack(Packed packed, Predicate<RegistryKey<Recipe<?>>> validPredicate) {
        this.options.copyFrom(packed.settings);
        this.handleList(packed.known, this.unlocked::add, validPredicate);
        this.handleList(packed.highlight, this.highlighted::add, validPredicate);
    }

    @FunctionalInterface
    public static interface DisplayCollector {
        public void displaysForRecipe(RegistryKey<Recipe<?>> var1, Consumer<RecipeDisplayEntry> var2);
    }

    public record Packed(RecipeBookOptions settings, List<RegistryKey<Recipe<?>>> known, List<RegistryKey<Recipe<?>>> highlight) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group(RecipeBookOptions.CODEC.forGetter(Packed::settings), ((MapCodec)Recipe.KEY_CODEC.listOf().fieldOf("recipes")).forGetter(Packed::known), ((MapCodec)Recipe.KEY_CODEC.listOf().fieldOf("toBeDisplayed")).forGetter(Packed::highlight)).apply((Applicative<Packed, ?>)instance, Packed::new));
    }
}

