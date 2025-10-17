/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.schema;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.datafixer.schema.Schema1451v6;
import net.minecraft.datafixer.schema.Schema1458;
import net.minecraft.datafixer.schema.Schema705;
import net.minecraft.datafixer.schema.Schema99;

public class Schema1460
extends IdentifierNormalizingSchema {
    public Schema1460(int i, Schema schema) {
        super(i, schema);
    }

    protected static void targetEntityItems(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
        schema.registerSimple(map, entityId);
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.register(map, name, () -> Schema1458.itemsAndCustomName(schema));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        schema.register(map, "minecraft:area_effect_cloud", (String string) -> DSL.optionalFields("Particle", TypeReferences.PARTICLE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:armor_stand");
        schema.register(map, "minecraft:arrow", (String name) -> DSL.optionalFields("inBlockState", TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:bat");
        Schema1460.targetEntityItems(schema, map, "minecraft:blaze");
        schema.registerSimple(map, "minecraft:boat");
        Schema1460.targetEntityItems(schema, map, "minecraft:cave_spider");
        schema.register(map, "minecraft:chest_minecart", (String name) -> DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema))));
        Schema1460.targetEntityItems(schema, map, "minecraft:chicken");
        schema.register(map, "minecraft:commandblock_minecart", (String name) -> DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema), "LastOutput", TypeReferences.TEXT_COMPONENT.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:cow");
        Schema1460.targetEntityItems(schema, map, "minecraft:creeper");
        schema.register(map, "minecraft:donkey", (String name) -> DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "SaddleItem", TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "minecraft:dragon_fireball");
        schema.registerSimple(map, "minecraft:egg");
        Schema1460.targetEntityItems(schema, map, "minecraft:elder_guardian");
        schema.registerSimple(map, "minecraft:ender_crystal");
        Schema1460.targetEntityItems(schema, map, "minecraft:ender_dragon");
        schema.register(map, "minecraft:enderman", (String name) -> DSL.optionalFields("carriedBlockState", TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:endermite");
        schema.registerSimple(map, "minecraft:ender_pearl");
        schema.registerSimple(map, "minecraft:evocation_fangs");
        Schema1460.targetEntityItems(schema, map, "minecraft:evocation_illager");
        schema.registerSimple(map, "minecraft:eye_of_ender_signal");
        schema.register(map, "minecraft:falling_block", (String name) -> DSL.optionalFields("BlockState", TypeReferences.BLOCK_STATE.in(schema), "TileEntityData", TypeReferences.BLOCK_ENTITY.in(schema)));
        schema.registerSimple(map, "minecraft:fireball");
        schema.register(map, "minecraft:fireworks_rocket", (String name) -> DSL.optionalFields("FireworksItem", TypeReferences.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:furnace_minecart", (String name) -> DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:ghast");
        Schema1460.targetEntityItems(schema, map, "minecraft:giant");
        Schema1460.targetEntityItems(schema, map, "minecraft:guardian");
        schema.register(map, "minecraft:hopper_minecart", (String name) -> DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema))));
        schema.register(map, "minecraft:horse", (String string) -> DSL.optionalFields("ArmorItem", TypeReferences.ITEM_STACK.in(schema), "SaddleItem", TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:husk");
        Schema1460.targetEntityItems(schema, map, "minecraft:illusion_illager");
        schema.register(map, "minecraft:item", (String name) -> DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:item_frame", (String name) -> DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "minecraft:leash_knot");
        schema.register(map, "minecraft:llama", (String name) -> DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), "DecorItem", TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "minecraft:llama_spit");
        Schema1460.targetEntityItems(schema, map, "minecraft:magma_cube");
        schema.register(map, "minecraft:minecart", (String name) -> DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:mooshroom");
        schema.register(map, "minecraft:mule", (String name) -> DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "SaddleItem", TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:ocelot");
        schema.registerSimple(map, "minecraft:painting");
        Schema1460.targetEntityItems(schema, map, "minecraft:parrot");
        Schema1460.targetEntityItems(schema, map, "minecraft:pig");
        Schema1460.targetEntityItems(schema, map, "minecraft:polar_bear");
        schema.register(map, "minecraft:potion", (String name) -> DSL.optionalFields("Potion", TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:rabbit");
        Schema1460.targetEntityItems(schema, map, "minecraft:sheep");
        Schema1460.targetEntityItems(schema, map, "minecraft:shulker");
        schema.registerSimple(map, "minecraft:shulker_bullet");
        Schema1460.targetEntityItems(schema, map, "minecraft:silverfish");
        Schema1460.targetEntityItems(schema, map, "minecraft:skeleton");
        schema.register(map, "minecraft:skeleton_horse", (String string) -> DSL.optionalFields("SaddleItem", TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:slime");
        schema.registerSimple(map, "minecraft:small_fireball");
        schema.registerSimple(map, "minecraft:snowball");
        Schema1460.targetEntityItems(schema, map, "minecraft:snowman");
        schema.register(map, "minecraft:spawner_minecart", (String name) -> DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema), TypeReferences.UNTAGGED_SPAWNER.in(schema)));
        schema.register(map, "minecraft:spectral_arrow", (String name) -> DSL.optionalFields("inBlockState", TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:spider");
        Schema1460.targetEntityItems(schema, map, "minecraft:squid");
        Schema1460.targetEntityItems(schema, map, "minecraft:stray");
        schema.registerSimple(map, "minecraft:tnt");
        schema.register(map, "minecraft:tnt_minecart", (String name) -> DSL.optionalFields("DisplayState", TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:vex");
        schema.register(map, "minecraft:villager", (String name) -> DSL.optionalFields("Inventory", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(TypeReferences.VILLAGER_TRADE.in(schema)))));
        Schema1460.targetEntityItems(schema, map, "minecraft:villager_golem");
        Schema1460.targetEntityItems(schema, map, "minecraft:vindication_illager");
        Schema1460.targetEntityItems(schema, map, "minecraft:witch");
        Schema1460.targetEntityItems(schema, map, "minecraft:wither");
        Schema1460.targetEntityItems(schema, map, "minecraft:wither_skeleton");
        schema.registerSimple(map, "minecraft:wither_skull");
        Schema1460.targetEntityItems(schema, map, "minecraft:wolf");
        schema.registerSimple(map, "minecraft:xp_bottle");
        schema.registerSimple(map, "minecraft:xp_orb");
        Schema1460.targetEntityItems(schema, map, "minecraft:zombie");
        schema.register(map, "minecraft:zombie_horse", (String string) -> DSL.optionalFields("SaddleItem", TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:zombie_pigman");
        schema.register(map, "minecraft:zombie_villager", (String string) -> DSL.optionalFields("Offers", DSL.optionalFields("Recipes", DSL.list(TypeReferences.VILLAGER_TRADE.in(schema)))));
        return map;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        Schema1460.registerInventory(schema, map, "minecraft:furnace");
        Schema1460.registerInventory(schema, map, "minecraft:chest");
        Schema1460.registerInventory(schema, map, "minecraft:trapped_chest");
        schema.registerSimple(map, "minecraft:ender_chest");
        schema.register(map, "minecraft:jukebox", (String name) -> DSL.optionalFields("RecordItem", TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.registerInventory(schema, map, "minecraft:dispenser");
        Schema1460.registerInventory(schema, map, "minecraft:dropper");
        schema.register(map, "minecraft:sign", () -> Schema99.method_66194(schema));
        schema.register(map, "minecraft:mob_spawner", (String name) -> TypeReferences.UNTAGGED_SPAWNER.in(schema));
        schema.register(map, "minecraft:piston", (String name) -> DSL.optionalFields("blockState", TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.registerInventory(schema, map, "minecraft:brewing_stand");
        schema.register(map, "minecraft:enchanting_table", () -> Schema1458.customName(schema));
        schema.registerSimple(map, "minecraft:end_portal");
        schema.register(map, "minecraft:beacon", () -> Schema1458.customName(schema));
        schema.register(map, "minecraft:skull", () -> DSL.optionalFields("custom_name", TypeReferences.TEXT_COMPONENT.in(schema)));
        schema.registerSimple(map, "minecraft:daylight_detector");
        Schema1460.registerInventory(schema, map, "minecraft:hopper");
        schema.registerSimple(map, "minecraft:comparator");
        schema.register(map, "minecraft:banner", () -> Schema1458.customName(schema));
        schema.registerSimple(map, "minecraft:structure_block");
        schema.registerSimple(map, "minecraft:end_gateway");
        schema.register(map, "minecraft:command_block", () -> DSL.optionalFields("LastOutput", TypeReferences.TEXT_COMPONENT.in(schema)));
        Schema1460.registerInventory(schema, map, "minecraft:shulker_box");
        schema.registerSimple(map, "minecraft:bed");
        return map;
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        schema.registerType(false, TypeReferences.LEVEL, () -> DSL.optionalFields("CustomBossEvents", DSL.compoundList(DSL.optionalFields("Name", TypeReferences.TEXT_COMPONENT.in(schema))), TypeReferences.LIGHTWEIGHT_LEVEL.in(schema)));
        schema.registerType(false, TypeReferences.LIGHTWEIGHT_LEVEL, DSL::remainder);
        schema.registerType(false, TypeReferences.RECIPE, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.PLAYER, () -> DSL.optionalFields(Pair.of("RootVehicle", DSL.optionalFields("Entity", TypeReferences.ENTITY_TREE.in(schema))), Pair.of("ender_pearls", DSL.list(TypeReferences.ENTITY_TREE.in(schema))), Pair.of("Inventory", DSL.list(TypeReferences.ITEM_STACK.in(schema))), Pair.of("EnderItems", DSL.list(TypeReferences.ITEM_STACK.in(schema))), Pair.of("ShoulderEntityLeft", TypeReferences.ENTITY_TREE.in(schema)), Pair.of("ShoulderEntityRight", TypeReferences.ENTITY_TREE.in(schema)), Pair.of("recipeBook", DSL.optionalFields("recipes", DSL.list(TypeReferences.RECIPE.in(schema)), "toBeDisplayed", DSL.list(TypeReferences.RECIPE.in(schema))))));
        schema.registerType(false, TypeReferences.CHUNK, () -> DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(TypeReferences.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(DSL.or(TypeReferences.BLOCK_ENTITY.in(schema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", TypeReferences.BLOCK_NAME.in(schema))), "Sections", DSL.list(DSL.optionalFields("Palette", DSL.list(TypeReferences.BLOCK_STATE.in(schema)))))));
        schema.registerType(true, TypeReferences.BLOCK_ENTITY, () -> DSL.optionalFields("components", TypeReferences.DATA_COMPONENTS.in(schema), DSL.taggedChoiceLazy("id", Schema1460.getIdentifierType(), blockEntityTypes)));
        schema.registerType(true, TypeReferences.ENTITY_TREE, () -> DSL.optionalFields("Passengers", DSL.list(TypeReferences.ENTITY_TREE.in(schema)), TypeReferences.ENTITY.in(schema)));
        schema.registerType(true, TypeReferences.ENTITY, () -> DSL.and(TypeReferences.ENTITY_EQUIPMENT.in(schema), DSL.optionalFields("CustomName", TypeReferences.TEXT_COMPONENT.in(schema), DSL.taggedChoiceLazy("id", Schema1460.getIdentifierType(), entityTypes))));
        schema.registerType(true, TypeReferences.ITEM_STACK, () -> DSL.hook(DSL.optionalFields("id", TypeReferences.ITEM_NAME.in(schema), "tag", Schema99.method_66195(schema)), Schema705.field_5746, Hook.HookFunction.IDENTITY));
        schema.registerType(false, TypeReferences.HOTBAR, () -> DSL.compoundList(DSL.list(TypeReferences.ITEM_STACK.in(schema))));
        schema.registerType(false, TypeReferences.OPTIONS, DSL::remainder);
        schema.registerType(false, TypeReferences.STRUCTURE, () -> DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", TypeReferences.ENTITY_TREE.in(schema))), "blocks", DSL.list(DSL.optionalFields("nbt", TypeReferences.BLOCK_ENTITY.in(schema))), "palette", DSL.list(TypeReferences.BLOCK_STATE.in(schema))));
        schema.registerType(false, TypeReferences.BLOCK_NAME, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.ITEM_NAME, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.BLOCK_STATE, DSL::remainder);
        schema.registerType(false, TypeReferences.FLAT_BLOCK_STATE, DSL::remainder);
        Supplier<TypeTemplate> supplier = () -> DSL.compoundList(TypeReferences.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
        schema.registerType(false, TypeReferences.STATS, () -> DSL.optionalFields("stats", DSL.optionalFields(Pair.of("minecraft:mined", DSL.compoundList(TypeReferences.BLOCK_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:crafted", (TypeTemplate)supplier.get()), Pair.of("minecraft:used", (TypeTemplate)supplier.get()), Pair.of("minecraft:broken", (TypeTemplate)supplier.get()), Pair.of("minecraft:picked_up", (TypeTemplate)supplier.get()), Pair.of("minecraft:dropped", (TypeTemplate)supplier.get()), Pair.of("minecraft:killed", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:killed_by", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:custom", DSL.compoundList(DSL.constType(Schema1460.getIdentifierType()), DSL.constType(DSL.intType()))))));
        schema.registerType(false, TypeReferences.SAVED_DATA_COMMAND_STORAGE, DSL::remainder);
        schema.registerType(false, TypeReferences.TICKETS_SAVED_DATA, DSL::remainder);
        schema.registerType(false, TypeReferences.SAVED_DATA_MAP_DATA, () -> DSL.optionalFields("data", DSL.optionalFields("banners", DSL.list(DSL.optionalFields("Name", TypeReferences.TEXT_COMPONENT.in(schema))))));
        schema.registerType(false, TypeReferences.SAVED_DATA_IDCOUNTS, DSL::remainder);
        schema.registerType(false, TypeReferences.SAVED_DATA_RAIDS, DSL::remainder);
        schema.registerType(false, TypeReferences.SAVED_DATA_RANDOM_SEQUENCES, DSL::remainder);
        schema.registerType(false, TypeReferences.SAVED_DATA_SCOREBOARD, () -> DSL.optionalFields("data", DSL.optionalFields("Objectives", DSL.list(TypeReferences.OBJECTIVE.in(schema)), "Teams", DSL.list(TypeReferences.TEAM.in(schema)), "PlayerScores", DSL.list(DSL.optionalFields("display", TypeReferences.TEXT_COMPONENT.in(schema))))));
        schema.registerType(false, TypeReferences.SAVED_DATA_STRUCTURE_FEATURE_INDICES, () -> DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(TypeReferences.STRUCTURE_FEATURE.in(schema)))));
        schema.registerType(false, TypeReferences.WORLD_BORDER_SAVED_DATA, DSL::remainder);
        schema.registerType(false, TypeReferences.STRUCTURE_FEATURE, DSL::remainder);
        Map<String, Supplier<TypeTemplate>> map3 = Schema1451v6.method_37389(schema);
        schema.registerType(false, TypeReferences.OBJECTIVE, () -> DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map3), "DisplayName", TypeReferences.TEXT_COMPONENT.in(schema)), Schema1451v6.field_34014, Schema1451v6.field_34015));
        schema.registerType(false, TypeReferences.TEAM, () -> DSL.optionalFields("MemberNamePrefix", TypeReferences.TEXT_COMPONENT.in(schema), "MemberNameSuffix", TypeReferences.TEXT_COMPONENT.in(schema), "DisplayName", TypeReferences.TEXT_COMPONENT.in(schema)));
        schema.registerType(true, TypeReferences.UNTAGGED_SPAWNER, () -> DSL.optionalFields("SpawnPotentials", DSL.list(DSL.fields("Entity", TypeReferences.ENTITY_TREE.in(schema))), "SpawnData", TypeReferences.ENTITY_TREE.in(schema)));
        schema.registerType(false, TypeReferences.ADVANCEMENTS, () -> DSL.optionalFields("minecraft:adventure/adventuring_time", DSL.optionalFields("criteria", DSL.compoundList(TypeReferences.BIOME.in(schema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_a_mob", DSL.optionalFields("criteria", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_all_mobs", DSL.optionalFields("criteria", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))), "minecraft:husbandry/bred_all_animals", DSL.optionalFields("criteria", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.string())))));
        schema.registerType(false, TypeReferences.BIOME, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.ENTITY_NAME, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.POI_CHUNK, DSL::remainder);
        schema.registerType(false, TypeReferences.WORLD_GEN_SETTINGS, DSL::remainder);
        schema.registerType(false, TypeReferences.ENTITY_CHUNK, () -> DSL.optionalFields("Entities", DSL.list(TypeReferences.ENTITY_TREE.in(schema))));
        schema.registerType(true, TypeReferences.DATA_COMPONENTS, DSL::remainder);
        schema.registerType(true, TypeReferences.VILLAGER_TRADE, () -> DSL.optionalFields("buy", TypeReferences.ITEM_STACK.in(schema), "buyB", TypeReferences.ITEM_STACK.in(schema), "sell", TypeReferences.ITEM_STACK.in(schema)));
        schema.registerType(true, TypeReferences.PARTICLE, () -> DSL.constType(DSL.string()));
        schema.registerType(true, TypeReferences.TEXT_COMPONENT, () -> DSL.constType(DSL.string()));
        schema.registerType(true, TypeReferences.ENTITY_EQUIPMENT, () -> DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(TypeReferences.ITEM_STACK.in(schema)))), DSL.optional(DSL.field("HandItems", DSL.list(TypeReferences.ITEM_STACK.in(schema)))), DSL.optional(DSL.field("body_armor_item", TypeReferences.ITEM_STACK.in(schema))), DSL.optional(DSL.field("saddle", TypeReferences.ITEM_STACK.in(schema)))));
    }
}

