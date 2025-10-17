/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.item.equipment.trim.ArmorTrimPatterns;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

public class SpawnArmorTrimsCommand {
    private static final List<RegistryKey<ArmorTrimPattern>> PATTERNS = List.of(ArmorTrimPatterns.SENTRY, ArmorTrimPatterns.DUNE, ArmorTrimPatterns.COAST, ArmorTrimPatterns.WILD, ArmorTrimPatterns.WARD, ArmorTrimPatterns.EYE, ArmorTrimPatterns.VEX, ArmorTrimPatterns.TIDE, ArmorTrimPatterns.SNOUT, ArmorTrimPatterns.RIB, ArmorTrimPatterns.SPIRE, ArmorTrimPatterns.WAYFINDER, ArmorTrimPatterns.SHAPER, ArmorTrimPatterns.SILENCE, ArmorTrimPatterns.RAISER, ArmorTrimPatterns.HOST, ArmorTrimPatterns.FLOW, ArmorTrimPatterns.BOLT);
    private static final List<RegistryKey<ArmorTrimMaterial>> MATERIALS = List.of(ArmorTrimMaterials.QUARTZ, ArmorTrimMaterials.IRON, ArmorTrimMaterials.NETHERITE, ArmorTrimMaterials.REDSTONE, ArmorTrimMaterials.COPPER, ArmorTrimMaterials.GOLD, ArmorTrimMaterials.EMERALD, ArmorTrimMaterials.DIAMOND, ArmorTrimMaterials.LAPIS, ArmorTrimMaterials.AMETHYST, ArmorTrimMaterials.RESIN);
    private static final ToIntFunction<RegistryKey<ArmorTrimPattern>> PATTERN_INDEX_GETTER = Util.lastIndexGetter(PATTERNS);
    private static final ToIntFunction<RegistryKey<ArmorTrimMaterial>> MATERIAL_INDEX_GETTER = Util.lastIndexGetter(MATERIALS);
    private static final DynamicCommandExceptionType INVALID_PATTERN_EXCEPTION = new DynamicCommandExceptionType(pattern -> Text.stringifiedTranslatable("Invalid pattern", pattern));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spawn_armor_trims").requires(CommandManager.requirePermissionLevel(2))).then(CommandManager.literal("*_lag_my_game").executes(context -> SpawnArmorTrimsCommand.execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getPlayerOrThrow())))).then(CommandManager.argument("pattern", RegistryKeyArgumentType.registryKey(RegistryKeys.TRIM_PATTERN)).executes(context -> SpawnArmorTrimsCommand.execute((ServerCommandSource)context.getSource(), (PlayerEntity)((ServerCommandSource)context.getSource()).getPlayerOrThrow(), RegistryKeyArgumentType.getKey(context, "pattern", RegistryKeys.TRIM_PATTERN, INVALID_PATTERN_EXCEPTION)))));
    }

    private static int execute(ServerCommandSource source, PlayerEntity player) {
        return SpawnArmorTrimsCommand.execute(source, player, source.getServer().getRegistryManager().getOrThrow(RegistryKeys.TRIM_PATTERN).streamEntries());
    }

    private static int execute(ServerCommandSource source, PlayerEntity player, RegistryKey<ArmorTrimPattern> pattern) {
        return SpawnArmorTrimsCommand.execute(source, player, Stream.of(source.getServer().getRegistryManager().getOrThrow(RegistryKeys.TRIM_PATTERN).getOptional(pattern).orElseThrow()));
    }

    private static int execute(ServerCommandSource source, PlayerEntity player, Stream<RegistryEntry.Reference<ArmorTrimPattern>> patterns) {
        ServerWorld lv = source.getWorld();
        List<RegistryEntry.Reference> list = patterns.sorted(Comparator.comparing(pattern -> PATTERN_INDEX_GETTER.applyAsInt(pattern.registryKey()))).toList();
        List<RegistryEntry.Reference> list2 = lv.getRegistryManager().getOrThrow(RegistryKeys.TRIM_MATERIAL).streamEntries().sorted(Comparator.comparing(material -> MATERIAL_INDEX_GETTER.applyAsInt(material.registryKey()))).toList();
        List<RegistryEntry.Reference<Item>> list3 = SpawnArmorTrimsCommand.getArmorItems(lv.getRegistryManager().getOrThrow(RegistryKeys.ITEM));
        BlockPos lv2 = player.getBlockPos().offset(player.getHorizontalFacing(), 5);
        double d = 3.0;
        for (int i = 0; i < list2.size(); ++i) {
            RegistryEntry.Reference lv3 = list2.get(i);
            for (int j = 0; j < list.size(); ++j) {
                RegistryEntry.Reference lv4 = list.get(j);
                ArmorTrim lv5 = new ArmorTrim(lv3, lv4);
                for (int k = 0; k < list3.size(); ++k) {
                    RegistryEntry.Reference<Item> lv6 = list3.get(k);
                    double e = (double)lv2.getX() + 0.5 - (double)k * 3.0;
                    double f = (double)lv2.getY() + 0.5 + (double)i * 3.0;
                    double g = (double)lv2.getZ() + 0.5 + (double)(j * 10);
                    ArmorStandEntity lv7 = new ArmorStandEntity(lv, e, f, g);
                    lv7.setYaw(180.0f);
                    lv7.setNoGravity(true);
                    ItemStack lv8 = new ItemStack(lv6);
                    EquippableComponent lv9 = Objects.requireNonNull(lv8.get(DataComponentTypes.EQUIPPABLE));
                    lv8.set(DataComponentTypes.TRIM, lv5);
                    lv7.equipStack(lv9.slot(), lv8);
                    if (k == 0) {
                        lv7.setCustomName(lv5.pattern().value().getDescription(lv5.material()).copy().append(" & ").append(lv5.material().value().description()));
                        lv7.setCustomNameVisible(true);
                    } else {
                        lv7.setInvisible(true);
                    }
                    lv.spawnEntity(lv7);
                }
            }
        }
        source.sendFeedback(() -> Text.literal("Armorstands with trimmed armor spawned around you"), true);
        return 1;
    }

    private static List<RegistryEntry.Reference<Item>> getArmorItems(RegistryWrapper<Item> itemRegistry) {
        ArrayList<RegistryEntry.Reference<Item>> list = new ArrayList<RegistryEntry.Reference<Item>>();
        itemRegistry.streamEntries().forEach(entry -> {
            EquippableComponent lv = ((Item)entry.value()).getComponents().get(DataComponentTypes.EQUIPPABLE);
            if (lv != null && lv.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR && lv.assetId().isPresent()) {
                list.add((RegistryEntry.Reference<Item>)entry);
            }
        });
        return list;
    }
}

