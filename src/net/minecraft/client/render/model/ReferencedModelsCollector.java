/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.MissingModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ReferencedModelsCollector {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Object2ObjectMap<Identifier, Holder> modelCache = new Object2ObjectOpenHashMap<Identifier, Holder>();
    private final Holder missingModel;
    private final Object2ObjectFunction<Identifier, Holder> holder;
    private final ResolvableModel.Resolver resolver;
    private final Queue<Holder> queue = new ArrayDeque<Holder>();

    public ReferencedModelsCollector(Map<Identifier, UnbakedModel> unbakedModels, UnbakedModel missingModel) {
        this.missingModel = new Holder(MissingModel.ID, missingModel, true);
        this.modelCache.put(MissingModel.ID, this.missingModel);
        this.holder = id -> {
            Identifier lv = (Identifier)id;
            UnbakedModel lv2 = (UnbakedModel)unbakedModels.get(lv);
            if (lv2 == null) {
                LOGGER.warn("Missing block model: {}", (Object)lv);
                return this.missingModel;
            }
            return this.schedule(lv, lv2);
        };
        this.resolver = this::resolve;
    }

    private static boolean isRootModel(UnbakedModel model) {
        return model.parent() == null;
    }

    private Holder resolve(Identifier id) {
        return this.modelCache.computeIfAbsent(id, this.holder);
    }

    private Holder schedule(Identifier id, UnbakedModel model) {
        boolean bl = ReferencedModelsCollector.isRootModel(model);
        Holder lv = new Holder(id, model, bl);
        if (!bl) {
            this.queue.add(lv);
        }
        return lv;
    }

    public void resolve(ResolvableModel model) {
        model.resolve(this.resolver);
    }

    public void addSpecialModel(Identifier id, UnbakedModel model) {
        if (!ReferencedModelsCollector.isRootModel(model)) {
            LOGGER.warn("Trying to add non-root special model {}, ignoring", (Object)id);
            return;
        }
        Holder lv = this.modelCache.put(id, this.schedule(id, model));
        if (lv != null) {
            LOGGER.warn("Duplicate special model {}", (Object)id);
        }
    }

    public BakedSimpleModel getMissingModel() {
        return this.missingModel;
    }

    public Map<Identifier, BakedSimpleModel> collectModels() {
        ArrayList<Holder> list = new ArrayList<Holder>();
        this.resolveAll(list);
        ReferencedModelsCollector.checkIfValid(list);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        this.modelCache.forEach((id, model) -> {
            if (model.valid) {
                builder.put(id, model);
            } else {
                LOGGER.warn("Model {} ignored due to cyclic dependency", id);
            }
        });
        return builder.build();
    }

    private void resolveAll(List<Holder> models) {
        Holder lv;
        while ((lv = this.queue.poll()) != null) {
            Holder lv3;
            Identifier lv2 = Objects.requireNonNull(lv.model.parent());
            lv.parent = lv3 = this.resolve(lv2);
            if (lv3.valid) {
                lv.valid = true;
                continue;
            }
            models.add(lv);
        }
    }

    private static void checkIfValid(List<Holder> models) {
        boolean bl = true;
        while (bl) {
            bl = false;
            Iterator<Holder> iterator = models.iterator();
            while (iterator.hasNext()) {
                Holder lv = iterator.next();
                if (!Objects.requireNonNull(lv.parent).valid) continue;
                lv.valid = true;
                iterator.remove();
                bl = true;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Holder
    implements BakedSimpleModel {
        private static final Property<Boolean> AMBIENT_OCCLUSION_PROPERTY = Holder.createProperty(0);
        private static final Property<UnbakedModel.GuiLight> GUI_LIGHT_PROPERTY = Holder.createProperty(1);
        private static final Property<Geometry> GEOMETRY_PROPERTY = Holder.createProperty(2);
        private static final Property<ModelTransformation> TRANSFORMATIONS_PROPERTY = Holder.createProperty(3);
        private static final Property<ModelTextures> TEXTURE_PROPERTY = Holder.createProperty(4);
        private static final Property<Sprite> PARTICLE_TEXTURE_PROPERTY = Holder.createProperty(5);
        private static final Property<BakedGeometry> BAKED_GEOMETRY_PROPERTY = Holder.createProperty(6);
        private static final int PROPERTY_COUNT = 7;
        private final Identifier id;
        boolean valid;
        @Nullable
        Holder parent;
        final UnbakedModel model;
        private final AtomicReferenceArray<Object> properties = new AtomicReferenceArray(7);
        private final Map<ModelBakeSettings, BakedGeometry> bakeCache = new ConcurrentHashMap<ModelBakeSettings, BakedGeometry>();

        private static <T> Property<T> createProperty(int i) {
            Objects.checkIndex(i, 7);
            return new Property(i);
        }

        Holder(Identifier id, UnbakedModel model, boolean valid) {
            this.id = id;
            this.model = model;
            this.valid = valid;
        }

        @Override
        public UnbakedModel getModel() {
            return this.model;
        }

        @Override
        @Nullable
        public BakedSimpleModel getParent() {
            return this.parent;
        }

        @Override
        public String name() {
            return this.id.toString();
        }

        @Nullable
        private <T> T getProperty(Property<T> property) {
            return (T)this.properties.get(property.index);
        }

        private <T> T setProperty(Property<T> property, T value) {
            T object2 = this.properties.compareAndExchange(property.index, null, value);
            if (object2 == null) {
                return value;
            }
            return object2;
        }

        private <T> T getProperty(Property<T> property, Function<BakedSimpleModel, T> fallback) {
            T object = this.getProperty(property);
            if (object != null) {
                return object;
            }
            return this.setProperty(property, fallback.apply(this));
        }

        @Override
        public boolean getAmbientOcclusion() {
            return this.getProperty(AMBIENT_OCCLUSION_PROPERTY, BakedSimpleModel::getAmbientOcclusion);
        }

        @Override
        public UnbakedModel.GuiLight getGuiLight() {
            return this.getProperty(GUI_LIGHT_PROPERTY, BakedSimpleModel::getGuiLight);
        }

        @Override
        public ModelTransformation getTransformations() {
            return this.getProperty(TRANSFORMATIONS_PROPERTY, BakedSimpleModel::copyTransformations);
        }

        @Override
        public Geometry getGeometry() {
            return this.getProperty(GEOMETRY_PROPERTY, BakedSimpleModel::getGeometry);
        }

        @Override
        public ModelTextures getTextures() {
            return this.getProperty(TEXTURE_PROPERTY, BakedSimpleModel::getTextures);
        }

        @Override
        public Sprite getParticleTexture(ModelTextures textures, Baker baker) {
            Sprite lv = this.getProperty(PARTICLE_TEXTURE_PROPERTY);
            if (lv != null) {
                return lv;
            }
            return this.setProperty(PARTICLE_TEXTURE_PROPERTY, BakedSimpleModel.getParticleTexture(textures, baker, this));
        }

        private BakedGeometry getBakedGeometry(ModelTextures textures, Baker baker, ModelBakeSettings settings) {
            BakedGeometry lv = this.getProperty(BAKED_GEOMETRY_PROPERTY);
            if (lv != null) {
                return lv;
            }
            return this.setProperty(BAKED_GEOMETRY_PROPERTY, this.getGeometry().bake(textures, baker, settings, this));
        }

        @Override
        public BakedGeometry bakeGeometry(ModelTextures textures, Baker baker, ModelBakeSettings settings) {
            if (settings == ModelRotation.X0_Y0) {
                return this.getBakedGeometry(textures, baker, settings);
            }
            return this.bakeCache.computeIfAbsent(settings, settings1 -> {
                Geometry lv = this.getGeometry();
                return lv.bake(textures, baker, (ModelBakeSettings)settings1, this);
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Property<T>(int index) {
    }
}

