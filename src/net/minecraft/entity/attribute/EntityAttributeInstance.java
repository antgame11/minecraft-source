/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.attribute;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class EntityAttributeInstance {
    private final RegistryEntry<EntityAttribute> type;
    private final Map<EntityAttributeModifier.Operation, Map<Identifier, EntityAttributeModifier>> operationToModifiers = Maps.newEnumMap(EntityAttributeModifier.Operation.class);
    private final Map<Identifier, EntityAttributeModifier> idToModifiers = new Object2ObjectArrayMap<Identifier, EntityAttributeModifier>();
    private final Map<Identifier, EntityAttributeModifier> persistentModifiers = new Object2ObjectArrayMap<Identifier, EntityAttributeModifier>();
    private double baseValue;
    private boolean dirty = true;
    private double value;
    private final Consumer<EntityAttributeInstance> updateCallback;

    public EntityAttributeInstance(RegistryEntry<EntityAttribute> type, Consumer<EntityAttributeInstance> updateCallback) {
        this.type = type;
        this.updateCallback = updateCallback;
        this.baseValue = type.value().getDefaultValue();
    }

    public RegistryEntry<EntityAttribute> getAttribute() {
        return this.type;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double baseValue) {
        if (baseValue == this.baseValue) {
            return;
        }
        this.baseValue = baseValue;
        this.onUpdate();
    }

    @VisibleForTesting
    Map<Identifier, EntityAttributeModifier> getModifiers(EntityAttributeModifier.Operation operation) {
        return this.operationToModifiers.computeIfAbsent(operation, operationx -> new Object2ObjectOpenHashMap());
    }

    public Set<EntityAttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.idToModifiers.values());
    }

    public Set<EntityAttributeModifier> getPersistentModifiers() {
        return ImmutableSet.copyOf(this.persistentModifiers.values());
    }

    @Nullable
    public EntityAttributeModifier getModifier(Identifier id) {
        return this.idToModifiers.get(id);
    }

    public boolean hasModifier(Identifier id) {
        return this.idToModifiers.get(id) != null;
    }

    private void addModifier(EntityAttributeModifier modifier) {
        EntityAttributeModifier lv = this.idToModifiers.putIfAbsent(modifier.id(), modifier);
        if (lv != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        this.getModifiers(modifier.operation()).put(modifier.id(), modifier);
        this.onUpdate();
    }

    public void updateModifier(EntityAttributeModifier modifier) {
        EntityAttributeModifier lv = this.idToModifiers.put(modifier.id(), modifier);
        if (modifier == lv) {
            return;
        }
        this.getModifiers(modifier.operation()).put(modifier.id(), modifier);
        this.onUpdate();
    }

    public void addTemporaryModifier(EntityAttributeModifier modifier) {
        this.addModifier(modifier);
    }

    public void overwritePersistentModifier(EntityAttributeModifier modifier) {
        this.removeModifier(modifier.id());
        this.addModifier(modifier);
        this.persistentModifiers.put(modifier.id(), modifier);
    }

    public void addPersistentModifier(EntityAttributeModifier modifier) {
        this.addModifier(modifier);
        this.persistentModifiers.put(modifier.id(), modifier);
    }

    public void addPersistentModifiers(Collection<EntityAttributeModifier> modifiers) {
        for (EntityAttributeModifier lv : modifiers) {
            this.addPersistentModifier(lv);
        }
    }

    protected void onUpdate() {
        this.dirty = true;
        this.updateCallback.accept(this);
    }

    public void removeModifier(EntityAttributeModifier modifier) {
        this.removeModifier(modifier.id());
    }

    public boolean removeModifier(Identifier id) {
        EntityAttributeModifier lv = this.idToModifiers.remove(id);
        if (lv == null) {
            return false;
        }
        this.getModifiers(lv.operation()).remove(id);
        this.persistentModifiers.remove(id);
        this.onUpdate();
        return true;
    }

    public void clearModifiers() {
        for (EntityAttributeModifier lv : this.getModifiers()) {
            this.removeModifier(lv);
        }
    }

    public double getValue() {
        if (this.dirty) {
            this.value = this.computeValue();
            this.dirty = false;
        }
        return this.value;
    }

    private double computeValue() {
        double d = this.getBaseValue();
        for (EntityAttributeModifier lv : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADD_VALUE)) {
            d += lv.value();
        }
        double e = d;
        for (EntityAttributeModifier lv2 : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            e += d * lv2.value();
        }
        for (EntityAttributeModifier lv2 : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            e *= 1.0 + lv2.value();
        }
        return this.type.value().clamp(e);
    }

    private Collection<EntityAttributeModifier> getModifiersByOperation(EntityAttributeModifier.Operation operation) {
        return this.operationToModifiers.getOrDefault(operation, Map.of()).values();
    }

    public void setFrom(EntityAttributeInstance other) {
        this.baseValue = other.baseValue;
        this.idToModifiers.clear();
        this.idToModifiers.putAll(other.idToModifiers);
        this.persistentModifiers.clear();
        this.persistentModifiers.putAll(other.persistentModifiers);
        this.operationToModifiers.clear();
        other.operationToModifiers.forEach((operation, modifiers) -> this.getModifiers((EntityAttributeModifier.Operation)operation).putAll((Map<Identifier, EntityAttributeModifier>)modifiers));
        this.onUpdate();
    }

    public Packed pack() {
        return new Packed(this.type, this.baseValue, List.copyOf(this.persistentModifiers.values()));
    }

    public void unpack(Packed packed) {
        this.baseValue = packed.baseValue;
        for (EntityAttributeModifier lv : packed.modifiers) {
            this.idToModifiers.put(lv.id(), lv);
            this.getModifiers(lv.operation()).put(lv.id(), lv);
            this.persistentModifiers.put(lv.id(), lv);
        }
        this.onUpdate();
    }

    public record Packed(RegistryEntry<EntityAttribute> attribute, double baseValue, List<EntityAttributeModifier> modifiers) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registries.ATTRIBUTE.getEntryCodec().fieldOf("id")).forGetter(Packed::attribute), ((MapCodec)Codec.DOUBLE.fieldOf("base")).orElse(0.0).forGetter(Packed::baseValue), EntityAttributeModifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(Packed::modifiers)).apply((Applicative<Packed, ?>)instance, Packed::new));
        public static final Codec<List<Packed>> LIST_CODEC = CODEC.listOf();
    }
}

