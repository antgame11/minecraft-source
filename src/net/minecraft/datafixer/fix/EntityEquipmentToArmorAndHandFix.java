/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class EntityEquipmentToArmorAndHandFix
extends DataFix {
    public EntityEquipmentToArmorAndHandFix(Schema schema) {
        super(schema, true);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixEquipment(this.getInputSchema().getTypeRaw(TypeReferences.ITEM_STACK), this.getOutputSchema().getTypeRaw(TypeReferences.ITEM_STACK));
    }

    private <ItemStackOld, ItemStackNew> TypeRewriteRule fixEquipment(Type<ItemStackOld> type, Type<ItemStackNew> type2) {
        Type<Pair<String, Either<ItemStackOld, Unit>>> type3 = DSL.named(TypeReferences.ENTITY_EQUIPMENT.typeName(), DSL.optional(DSL.field("Equipment", DSL.list(type))));
        Type<Pair<String, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Either<ItemStackNew, Unit>>>>>> type4 = DSL.named(TypeReferences.ENTITY_EQUIPMENT.typeName(), DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(type2))), DSL.optional(DSL.field("HandItems", DSL.list(type2))), DSL.optional(DSL.field("body_armor_item", type2)), DSL.optional(DSL.field("saddle", type2))));
        if (!type3.equals(this.getInputSchema().getType(TypeReferences.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Input entity_equipment type does not match expected");
        }
        if (!type4.equals(this.getOutputSchema().getType(TypeReferences.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Output entity_equipment type does not match expected");
        }
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("EntityEquipmentToArmorAndHandFix - drop chances", this.getInputSchema().getType(TypeReferences.ENTITY), typed -> typed.update(DSL.remainderFinder(), EntityEquipmentToArmorAndHandFix::method_66596)), this.fixTypeEverywhere("EntityEquipmentToArmorAndHandFix - equipment", type3, type4, dynamicOps -> {
            Object object = type2.read(new Dynamic(dynamicOps).emptyMap()).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack.")).getFirst();
            Either either = Either.right(DSL.unit());
            return pair -> pair.mapSecond(either2 -> {
                List list = either2.map(Function.identity(), unit -> List.of());
                Either<Object, Unit> either3 = Either.right(DSL.unit());
                Either<Object, Unit> either4 = Either.right(DSL.unit());
                if (!list.isEmpty()) {
                    either3 = Either.left(Lists.newArrayList(list.getFirst(), object));
                }
                if (list.size() > 1) {
                    ArrayList<Object> list2 = Lists.newArrayList(object, object, object, object);
                    for (int i = 1; i < Math.min(list.size(), 5); ++i) {
                        list2.set(i - 1, list.get(i));
                    }
                    either4 = Either.left(list2);
                }
                return Pair.of(either4, Pair.of(either3, Pair.of(either, either)));
            });
        }));
    }

    private static Dynamic<?> method_66596(Dynamic<?> dynamic2) {
        Optional<Stream<Dynamic<?>>> optional = dynamic2.get("DropChances").asStreamOpt().result();
        dynamic2 = dynamic2.remove("DropChances");
        if (optional.isPresent()) {
            Iterator iterator = Stream.concat(optional.get().map(dynamic -> Float.valueOf(dynamic.asFloat(0.0f))), Stream.generate(() -> Float.valueOf(0.0f))).iterator();
            float f = ((Float)iterator.next()).floatValue();
            if (dynamic2.get("HandDropChances").result().isEmpty()) {
                dynamic2 = dynamic2.set("HandDropChances", dynamic2.createList(Stream.of(Float.valueOf(f), Float.valueOf(0.0f)).map(dynamic2::createFloat)));
            }
            if (dynamic2.get("ArmorDropChances").result().isEmpty()) {
                dynamic2 = dynamic2.set("ArmorDropChances", dynamic2.createList(Stream.of((Float)iterator.next(), (Float)iterator.next(), (Float)iterator.next(), (Float)iterator.next()).map(dynamic2::createFloat)));
            }
        }
        return dynamic2;
    }
}

