/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.data.BlockModelDefinitionCreator;
import net.minecraft.client.data.BlockStateVariantMap;
import net.minecraft.client.data.PropertiesMap;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.json.BlockModelDefinition;
import net.minecraft.client.render.model.json.ModelVariantOperator;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.state.property.Property;

@Environment(value=EnvType.CLIENT)
public class VariantsBlockModelDefinitionCreator
implements BlockModelDefinitionCreator {
    private final Block block;
    private final List<Entry> variants;
    private final Set<Property<?>> definedProperties;

    VariantsBlockModelDefinitionCreator(Block block, List<Entry> variants, Set<Property<?>> definedProperties) {
        this.block = block;
        this.variants = variants;
        this.definedProperties = definedProperties;
    }

    static Set<Property<?>> validateAndAddProperties(Set<Property<?>> definedProperties, Block block, BlockStateVariantMap<?> variantMap) {
        List<Property<?>> list = variantMap.getProperties();
        list.forEach(property -> {
            if (block.getStateManager().getProperty(property.getName()) != property) {
                throw new IllegalStateException("Property " + String.valueOf(property) + " is not defined for block " + String.valueOf(block));
            }
            if (definedProperties.contains(property)) {
                throw new IllegalStateException("Values of property " + String.valueOf(property) + " already defined for block " + String.valueOf(block));
            }
        });
        HashSet set2 = new HashSet(definedProperties);
        set2.addAll(list);
        return set2;
    }

    public VariantsBlockModelDefinitionCreator apply(BlockStateVariantMap<ModelVariantOperator> operators) {
        Set<Property<?>> set = VariantsBlockModelDefinitionCreator.validateAndAddProperties(this.definedProperties, this.block, operators);
        List<Entry> list = this.variants.stream().flatMap(variant -> variant.apply(operators)).toList();
        return new VariantsBlockModelDefinitionCreator(this.block, list, set);
    }

    public VariantsBlockModelDefinitionCreator apply(ModelVariantOperator operator) {
        List<Entry> list = this.variants.stream().flatMap(variant -> variant.apply(operator)).toList();
        return new VariantsBlockModelDefinitionCreator(this.block, list, this.definedProperties);
    }

    @Override
    public BlockModelDefinition createBlockModelDefinition() {
        HashMap<String, BlockStateModel.Unbaked> map = new HashMap<String, BlockStateModel.Unbaked>();
        for (Entry lv : this.variants) {
            map.put(lv.properties.asString(), lv.variant.toModel());
        }
        return new BlockModelDefinition(Optional.of(new BlockModelDefinition.Variants(map)), Optional.empty());
    }

    @Override
    public Block getBlock() {
        return this.block;
    }

    public static Empty of(Block block) {
        return new Empty(block);
    }

    public static VariantsBlockModelDefinitionCreator of(Block block, WeightedVariant model) {
        return new VariantsBlockModelDefinitionCreator(block, List.of(new Entry(PropertiesMap.EMPTY, model)), Set.of());
    }

    @Environment(value=EnvType.CLIENT)
    record Entry(PropertiesMap properties, WeightedVariant variant) {
        public Stream<Entry> apply(BlockStateVariantMap<ModelVariantOperator> operatorMap) {
            return operatorMap.getVariants().entrySet().stream().map(variant -> {
                PropertiesMap lv = this.properties.copyOf((PropertiesMap)variant.getKey());
                WeightedVariant lv2 = this.variant.apply((ModelVariantOperator)variant.getValue());
                return new Entry(lv, lv2);
            });
        }

        public Stream<Entry> apply(ModelVariantOperator operator) {
            return Stream.of(new Entry(this.properties, this.variant.apply(operator)));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Empty {
        private final Block block;

        public Empty(Block block) {
            this.block = block;
        }

        public VariantsBlockModelDefinitionCreator with(BlockStateVariantMap<WeightedVariant> variantMap) {
            Set<Property<?>> set = VariantsBlockModelDefinitionCreator.validateAndAddProperties(Set.of(), this.block, variantMap);
            List<Entry> list = variantMap.getVariants().entrySet().stream().map(entry -> new Entry((PropertiesMap)entry.getKey(), (WeightedVariant)entry.getValue())).toList();
            return new VariantsBlockModelDefinitionCreator(this.block, list, set);
        }
    }
}

