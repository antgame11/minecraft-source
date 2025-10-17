/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelCuboidData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;

@Environment(value=EnvType.CLIENT)
public class ModelPartData {
    private final List<ModelCuboidData> cuboidData;
    private final ModelTransform transform;
    private final Map<String, ModelPartData> children = Maps.newHashMap();

    ModelPartData(List<ModelCuboidData> cuboidData, ModelTransform transform) {
        this.cuboidData = cuboidData;
        this.transform = transform;
    }

    public ModelPartData addChild(String name, ModelPartBuilder builder, ModelTransform transform) {
        ModelPartData lv = new ModelPartData(builder.build(), transform);
        return this.addChild(name, lv);
    }

    public ModelPartData addChild(String name, ModelPartData data) {
        ModelPartData lv = this.children.put(name, data);
        if (lv != null) {
            data.children.putAll(lv.children);
        }
        return data;
    }

    public ModelPartData resetChildrenParts() {
        for (String string : this.children.keySet()) {
            this.resetChildrenParts(string).resetChildrenParts();
        }
        return this;
    }

    public ModelPartData resetChildrenParts(String name) {
        ModelPartData lv = this.children.get(name);
        if (lv == null) {
            throw new IllegalArgumentException("No child with name: " + name);
        }
        return this.addChild(name, ModelPartBuilder.create(), lv.transform);
    }

    public void resetChildrenExcept(Set<String> names) {
        for (Map.Entry<String, ModelPartData> entry : this.children.entrySet()) {
            ModelPartData lv = entry.getValue();
            if (names.contains(entry.getKey())) continue;
            this.addChild(entry.getKey(), ModelPartBuilder.create(), lv.transform).resetChildrenExcept(names);
        }
    }

    public void resetChildrenExceptExact(Set<String> names) {
        for (Map.Entry<String, ModelPartData> entry : this.children.entrySet()) {
            ModelPartData lv = entry.getValue();
            if (names.contains(entry.getKey())) {
                lv.resetChildrenParts();
                continue;
            }
            this.addChild(entry.getKey(), ModelPartBuilder.create(), lv.transform).resetChildrenExceptExact(names);
        }
    }

    public ModelPart createPart(int textureWidth, int textureHeight) {
        Object2ObjectArrayMap object2ObjectArrayMap = this.children.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((ModelPartData)entry.getValue()).createPart(textureWidth, textureHeight), (name, partData) -> name, Object2ObjectArrayMap::new));
        List<ModelPart.Cuboid> list = this.cuboidData.stream().map(data -> data.createCuboid(textureWidth, textureHeight)).toList();
        ModelPart lv = new ModelPart(list, object2ObjectArrayMap);
        lv.setDefaultTransform(this.transform);
        lv.setTransform(this.transform);
        return lv;
    }

    public ModelPartData getChild(String name) {
        return this.children.get(name);
    }

    public Set<Map.Entry<String, ModelPartData>> getChildren() {
        return this.children.entrySet();
    }

    public ModelPartData applyTransformer(UnaryOperator<ModelTransform> transformer) {
        ModelPartData lv = new ModelPartData(this.cuboidData, (ModelTransform)transformer.apply(this.transform));
        lv.children.putAll(this.children);
        return lv;
    }
}

