/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.decoration;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public abstract class DisplayEntity
extends Entity {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int field_42384 = -1;
    private static final TrackedData<Integer> START_INTERPOLATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> INTERPOLATION_DURATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TELEPORT_DURATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Vector3f> TRANSLATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.VECTOR_3F);
    private static final TrackedData<Vector3f> SCALE = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.VECTOR_3F);
    private static final TrackedData<Quaternionf> LEFT_ROTATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.QUATERNION_F);
    private static final TrackedData<Quaternionf> RIGHT_ROTATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.QUATERNION_F);
    private static final TrackedData<Byte> BILLBOARD = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Integer> BRIGHTNESS = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> VIEW_RANGE = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> SHADOW_RADIUS = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> SHADOW_STRENGTH = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> WIDTH = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> HEIGHT = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> GLOW_COLOR_OVERRIDE = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final IntSet RENDERING_DATA_IDS = IntSet.of(TRANSLATION.id(), SCALE.id(), LEFT_ROTATION.id(), RIGHT_ROTATION.id(), BILLBOARD.id(), BRIGHTNESS.id(), SHADOW_RADIUS.id(), SHADOW_STRENGTH.id());
    private static final int DEFAULT_INTERPOLATION_DURATION = 0;
    private static final int DEFAULT_START_INTERPOLATION = 0;
    private static final int field_56423 = 0;
    private static final float field_42376 = 0.0f;
    private static final float field_42377 = 1.0f;
    private static final float field_57575 = 1.0f;
    private static final float field_57576 = 0.0f;
    private static final float field_57577 = 0.0f;
    private static final int field_42378 = -1;
    public static final String TELEPORT_DURATION_KEY = "teleport_duration";
    public static final String INTERPOLATION_DURATION_KEY = "interpolation_duration";
    public static final String START_INTERPOLATION_KEY = "start_interpolation";
    public static final String TRANSFORMATION_NBT_KEY = "transformation";
    public static final String BILLBOARD_NBT_KEY = "billboard";
    public static final String BRIGHTNESS_NBT_KEY = "brightness";
    public static final String VIEW_RANGE_NBT_KEY = "view_range";
    public static final String SHADOW_RADIUS_NBT_KEY = "shadow_radius";
    public static final String SHADOW_STRENGTH_NBT_KEY = "shadow_strength";
    public static final String WIDTH_NBT_KEY = "width";
    public static final String HEIGHT_NBT_KEY = "height";
    public static final String GLOW_COLOR_OVERRIDE_NBT_KEY = "glow_color_override";
    private long interpolationStart = Integer.MIN_VALUE;
    private int interpolationDuration;
    private float lerpProgress;
    private Box visibilityBoundingBox;
    private boolean tooSmallToRender = true;
    protected boolean renderingDataSet;
    private boolean startInterpolationSet;
    private boolean interpolationDurationSet;
    @Nullable
    private RenderState renderProperties;
    private final PositionInterpolator interpolator = new PositionInterpolator((Entity)this, 0);

    public DisplayEntity(EntityType<?> arg, World arg2) {
        super(arg, arg2);
        this.noClip = true;
        this.visibilityBoundingBox = this.getBoundingBox();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (HEIGHT.equals(data) || WIDTH.equals(data)) {
            this.updateVisibilityBoundingBox();
        }
        if (START_INTERPOLATION.equals(data)) {
            this.startInterpolationSet = true;
        }
        if (TELEPORT_DURATION.equals(data)) {
            this.interpolator.setLerpDuration(this.getTeleportDuration());
        }
        if (INTERPOLATION_DURATION.equals(data)) {
            this.interpolationDurationSet = true;
        }
        if (RENDERING_DATA_IDS.contains(data.id())) {
            this.renderingDataSet = true;
        }
    }

    @Override
    public final boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    private static AffineTransformation getTransformation(DataTracker dataTracker) {
        Vector3f vector3f = dataTracker.get(TRANSLATION);
        Quaternionf quaternionf = dataTracker.get(LEFT_ROTATION);
        Vector3f vector3f2 = dataTracker.get(SCALE);
        Quaternionf quaternionf2 = dataTracker.get(RIGHT_ROTATION);
        return new AffineTransformation(vector3f, quaternionf, vector3f2, quaternionf2);
    }

    @Override
    public void tick() {
        Entity lv = this.getVehicle();
        if (lv != null && lv.isRemoved()) {
            this.stopRiding();
        }
        if (this.getEntityWorld().isClient()) {
            if (this.startInterpolationSet) {
                this.startInterpolationSet = false;
                int i = this.getStartInterpolation();
                this.interpolationStart = this.age + i;
            }
            if (this.interpolationDurationSet) {
                this.interpolationDurationSet = false;
                this.interpolationDuration = this.getInterpolationDuration();
            }
            if (this.renderingDataSet) {
                this.renderingDataSet = false;
                boolean bl = this.interpolationDuration != 0;
                this.renderProperties = bl && this.renderProperties != null ? this.getLerpedRenderState(this.renderProperties, this.lerpProgress) : this.copyRenderState();
                this.refreshData(bl, this.lerpProgress);
            }
            this.interpolator.tick();
        }
    }

    @Override
    public PositionInterpolator getInterpolator() {
        return this.interpolator;
    }

    protected abstract void refreshData(boolean var1, float var2);

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(TELEPORT_DURATION, 0);
        builder.add(START_INTERPOLATION, 0);
        builder.add(INTERPOLATION_DURATION, 0);
        builder.add(TRANSLATION, new Vector3f());
        builder.add(SCALE, new Vector3f(1.0f, 1.0f, 1.0f));
        builder.add(RIGHT_ROTATION, new Quaternionf());
        builder.add(LEFT_ROTATION, new Quaternionf());
        builder.add(BILLBOARD, BillboardMode.FIXED.getIndex());
        builder.add(BRIGHTNESS, -1);
        builder.add(VIEW_RANGE, Float.valueOf(1.0f));
        builder.add(SHADOW_RADIUS, Float.valueOf(0.0f));
        builder.add(SHADOW_STRENGTH, Float.valueOf(1.0f));
        builder.add(WIDTH, Float.valueOf(0.0f));
        builder.add(HEIGHT, Float.valueOf(0.0f));
        builder.add(GLOW_COLOR_OVERRIDE, -1);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setTransformation(view.read(TRANSFORMATION_NBT_KEY, AffineTransformation.ANY_CODEC).orElse(AffineTransformation.identity()));
        this.setInterpolationDuration(view.getInt(INTERPOLATION_DURATION_KEY, 0));
        this.setStartInterpolation(view.getInt(START_INTERPOLATION_KEY, 0));
        int i = view.getInt(TELEPORT_DURATION_KEY, 0);
        this.setTeleportDuration(MathHelper.clamp(i, 0, 59));
        this.setBillboardMode(view.read(BILLBOARD_NBT_KEY, BillboardMode.CODEC).orElse(BillboardMode.FIXED));
        this.setViewRange(view.getFloat(VIEW_RANGE_NBT_KEY, 1.0f));
        this.setShadowRadius(view.getFloat(SHADOW_RADIUS_NBT_KEY, 0.0f));
        this.setShadowStrength(view.getFloat(SHADOW_STRENGTH_NBT_KEY, 1.0f));
        this.setDisplayWidth(view.getFloat(WIDTH_NBT_KEY, 0.0f));
        this.setDisplayHeight(view.getFloat(HEIGHT_NBT_KEY, 0.0f));
        this.setGlowColorOverride(view.getInt(GLOW_COLOR_OVERRIDE_NBT_KEY, -1));
        this.setBrightness(view.read(BRIGHTNESS_NBT_KEY, Brightness.CODEC).orElse(null));
    }

    private void setTransformation(AffineTransformation transformation) {
        this.dataTracker.set(TRANSLATION, transformation.getTranslation());
        this.dataTracker.set(LEFT_ROTATION, transformation.getLeftRotation());
        this.dataTracker.set(SCALE, transformation.getScale());
        this.dataTracker.set(RIGHT_ROTATION, transformation.getRightRotation());
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put(TRANSFORMATION_NBT_KEY, AffineTransformation.ANY_CODEC, DisplayEntity.getTransformation(this.dataTracker));
        view.put(BILLBOARD_NBT_KEY, BillboardMode.CODEC, this.getBillboardMode());
        view.putInt(INTERPOLATION_DURATION_KEY, this.getInterpolationDuration());
        view.putInt(TELEPORT_DURATION_KEY, this.getTeleportDuration());
        view.putFloat(VIEW_RANGE_NBT_KEY, this.getViewRange());
        view.putFloat(SHADOW_RADIUS_NBT_KEY, this.getShadowRadius());
        view.putFloat(SHADOW_STRENGTH_NBT_KEY, this.getShadowStrength());
        view.putFloat(WIDTH_NBT_KEY, this.getDisplayWidth());
        view.putFloat(HEIGHT_NBT_KEY, this.getDisplayHeight());
        view.putInt(GLOW_COLOR_OVERRIDE_NBT_KEY, this.getGlowColorOverride());
        view.putNullable(BRIGHTNESS_NBT_KEY, Brightness.CODEC, this.getBrightnessUnpacked());
    }

    public Box getVisibilityBoundingBox() {
        return this.visibilityBoundingBox;
    }

    public boolean shouldRender() {
        return !this.tooSmallToRender;
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Nullable
    public RenderState getRenderState() {
        return this.renderProperties;
    }

    private void setInterpolationDuration(int interpolationDuration) {
        this.dataTracker.set(INTERPOLATION_DURATION, interpolationDuration);
    }

    private int getInterpolationDuration() {
        return this.dataTracker.get(INTERPOLATION_DURATION);
    }

    private void setStartInterpolation(int startInterpolation) {
        this.dataTracker.set(START_INTERPOLATION, startInterpolation, true);
    }

    private int getStartInterpolation() {
        return this.dataTracker.get(START_INTERPOLATION);
    }

    private void setTeleportDuration(int teleportDuration) {
        this.dataTracker.set(TELEPORT_DURATION, teleportDuration);
    }

    private int getTeleportDuration() {
        return this.dataTracker.get(TELEPORT_DURATION);
    }

    private void setBillboardMode(BillboardMode billboardMode) {
        this.dataTracker.set(BILLBOARD, billboardMode.getIndex());
    }

    private BillboardMode getBillboardMode() {
        return BillboardMode.FROM_INDEX.apply(this.dataTracker.get(BILLBOARD).byteValue());
    }

    private void setBrightness(@Nullable Brightness brightness) {
        this.dataTracker.set(BRIGHTNESS, brightness != null ? brightness.pack() : -1);
    }

    @Nullable
    private Brightness getBrightnessUnpacked() {
        int i = this.dataTracker.get(BRIGHTNESS);
        return i != -1 ? Brightness.unpack(i) : null;
    }

    private int getBrightness() {
        return this.dataTracker.get(BRIGHTNESS);
    }

    private void setViewRange(float viewRange) {
        this.dataTracker.set(VIEW_RANGE, Float.valueOf(viewRange));
    }

    private float getViewRange() {
        return this.dataTracker.get(VIEW_RANGE).floatValue();
    }

    private void setShadowRadius(float shadowRadius) {
        this.dataTracker.set(SHADOW_RADIUS, Float.valueOf(shadowRadius));
    }

    private float getShadowRadius() {
        return this.dataTracker.get(SHADOW_RADIUS).floatValue();
    }

    private void setShadowStrength(float shadowStrength) {
        this.dataTracker.set(SHADOW_STRENGTH, Float.valueOf(shadowStrength));
    }

    private float getShadowStrength() {
        return this.dataTracker.get(SHADOW_STRENGTH).floatValue();
    }

    private void setDisplayWidth(float width) {
        this.dataTracker.set(WIDTH, Float.valueOf(width));
    }

    private float getDisplayWidth() {
        return this.dataTracker.get(WIDTH).floatValue();
    }

    private void setDisplayHeight(float height) {
        this.dataTracker.set(HEIGHT, Float.valueOf(height));
    }

    private int getGlowColorOverride() {
        return this.dataTracker.get(GLOW_COLOR_OVERRIDE);
    }

    private void setGlowColorOverride(int glowColorOverride) {
        this.dataTracker.set(GLOW_COLOR_OVERRIDE, glowColorOverride);
    }

    public float getLerpProgress(float tickProgress) {
        float j;
        int i = this.interpolationDuration;
        if (i <= 0) {
            return 1.0f;
        }
        float g = (long)this.age - this.interpolationStart;
        float h = g + tickProgress;
        this.lerpProgress = j = MathHelper.clamp(MathHelper.getLerpProgress(h, 0.0f, i), 0.0f, 1.0f);
        return j;
    }

    private float getDisplayHeight() {
        return this.dataTracker.get(HEIGHT).floatValue();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        this.updateVisibilityBoundingBox();
    }

    private void updateVisibilityBoundingBox() {
        float f = this.getDisplayWidth();
        float g = this.getDisplayHeight();
        this.tooSmallToRender = f == 0.0f || g == 0.0f;
        float h = f / 2.0f;
        double d = this.getX();
        double e = this.getY();
        double i = this.getZ();
        this.visibilityBoundingBox = new Box(d - (double)h, e, i - (double)h, d + (double)h, e + (double)g, i + (double)h);
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < MathHelper.square((double)this.getViewRange() * 64.0 * DisplayEntity.getRenderDistanceMultiplier());
    }

    @Override
    public int getTeamColorValue() {
        int i = this.getGlowColorOverride();
        return i != -1 ? i : super.getTeamColorValue();
    }

    private RenderState copyRenderState() {
        return new RenderState(AbstractInterpolator.constant(DisplayEntity.getTransformation(this.dataTracker)), this.getBillboardMode(), this.getBrightness(), FloatLerper.constant(this.getShadowRadius()), FloatLerper.constant(this.getShadowStrength()), this.getGlowColorOverride());
    }

    private RenderState getLerpedRenderState(RenderState state, float lerpProgress) {
        AffineTransformation lv = state.transformation.interpolate(lerpProgress);
        float g = state.shadowRadius.lerp(lerpProgress);
        float h = state.shadowStrength.lerp(lerpProgress);
        return new RenderState(new AffineTransformationInterpolator(lv, DisplayEntity.getTransformation(this.dataTracker)), this.getBillboardMode(), this.getBrightness(), new FloatLerperImpl(g, this.getShadowRadius()), new FloatLerperImpl(h, this.getShadowStrength()), this.getGlowColorOverride());
    }

    public record RenderState(AbstractInterpolator<AffineTransformation> transformation, BillboardMode billboardConstraints, int brightnessOverride, FloatLerper shadowRadius, FloatLerper shadowStrength, int glowColorOverride) {
    }

    public static enum BillboardMode implements StringIdentifiable
    {
        FIXED(0, "fixed"),
        VERTICAL(1, "vertical"),
        HORIZONTAL(2, "horizontal"),
        CENTER(3, "center");

        public static final Codec<BillboardMode> CODEC;
        public static final IntFunction<BillboardMode> FROM_INDEX;
        private final byte index;
        private final String name;

        private BillboardMode(byte index, String name) {
            this.name = name;
            this.index = index;
        }

        @Override
        public String asString() {
            return this.name;
        }

        byte getIndex() {
            return this.index;
        }

        static {
            CODEC = StringIdentifiable.createCodec(BillboardMode::values);
            FROM_INDEX = ValueLists.createIndexToValueFunction(BillboardMode::getIndex, BillboardMode.values(), ValueLists.OutOfBoundsHandling.ZERO);
        }
    }

    @FunctionalInterface
    public static interface AbstractInterpolator<T> {
        public static <T> AbstractInterpolator<T> constant(T value) {
            return delta -> value;
        }

        public T interpolate(float var1);
    }

    @FunctionalInterface
    public static interface FloatLerper {
        public static FloatLerper constant(float value) {
            return delta -> value;
        }

        public float lerp(float var1);
    }

    record AffineTransformationInterpolator(AffineTransformation previous, AffineTransformation current) implements AbstractInterpolator<AffineTransformation>
    {
        @Override
        public AffineTransformation interpolate(float f) {
            if ((double)f >= 1.0) {
                return this.current;
            }
            return this.previous.interpolate(this.current, f);
        }

        @Override
        public /* synthetic */ Object interpolate(float delta) {
            return this.interpolate(delta);
        }
    }

    record FloatLerperImpl(float previous, float current) implements FloatLerper
    {
        @Override
        public float lerp(float delta) {
            return MathHelper.lerp(delta, this.previous, this.current);
        }
    }

    record ArgbLerper(int previous, int current) implements IntLerper
    {
        @Override
        public int lerp(float delta) {
            return ColorHelper.lerp(delta, this.previous, this.current);
        }
    }

    record IntLerperImpl(int previous, int current) implements IntLerper
    {
        @Override
        public int lerp(float delta) {
            return MathHelper.lerp(delta, this.previous, this.current);
        }
    }

    @FunctionalInterface
    public static interface IntLerper {
        public static IntLerper constant(int value) {
            return delta -> value;
        }

        public int lerp(float var1);
    }

    public static class TextDisplayEntity
    extends DisplayEntity {
        public static final String TEXT_NBT_KEY = "text";
        private static final String LINE_WIDTH_NBT_KEY = "line_width";
        private static final String TEXT_OPACITY_NBT_KEY = "text_opacity";
        private static final String BACKGROUND_NBT_KEY = "background";
        private static final String SHADOW_NBT_KEY = "shadow";
        private static final String SEE_THROUGH_NBT_KEY = "see_through";
        private static final String DEFAULT_BACKGROUND_NBT_KEY = "default_background";
        private static final String ALIGNMENT_NBT_KEY = "alignment";
        public static final byte SHADOW_FLAG = 1;
        public static final byte SEE_THROUGH_FLAG = 2;
        public static final byte DEFAULT_BACKGROUND_FLAG = 4;
        public static final byte LEFT_ALIGNMENT_FLAG = 8;
        public static final byte RIGHT_ALIGNMENT_FLAG = 16;
        private static final byte INITIAL_TEXT_OPACITY = -1;
        public static final int INITIAL_BACKGROUND = 0x40000000;
        private static final int DEFAULT_LINE_WIDTH = 200;
        private static final TrackedData<Text> TEXT = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.TEXT_COMPONENT);
        private static final TrackedData<Integer> LINE_WIDTH = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
        private static final TrackedData<Integer> BACKGROUND = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
        private static final TrackedData<Byte> TEXT_OPACITY = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.BYTE);
        private static final TrackedData<Byte> TEXT_DISPLAY_FLAGS = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.BYTE);
        private static final IntSet TEXT_RENDERING_DATA_IDS = IntSet.of(TEXT.id(), LINE_WIDTH.id(), BACKGROUND.id(), TEXT_OPACITY.id(), TEXT_DISPLAY_FLAGS.id());
        @Nullable
        private TextLines textLines;
        @Nullable
        private Data data;

        public TextDisplayEntity(EntityType<?> arg, World arg2) {
            super(arg, arg2);
        }

        @Override
        protected void initDataTracker(DataTracker.Builder builder) {
            super.initDataTracker(builder);
            builder.add(TEXT, Text.empty());
            builder.add(LINE_WIDTH, 200);
            builder.add(BACKGROUND, 0x40000000);
            builder.add(TEXT_OPACITY, (byte)-1);
            builder.add(TEXT_DISPLAY_FLAGS, (byte)0);
        }

        @Override
        public void onTrackedDataSet(TrackedData<?> data) {
            super.onTrackedDataSet(data);
            if (TEXT_RENDERING_DATA_IDS.contains(data.id())) {
                this.renderingDataSet = true;
            }
        }

        private Text getText() {
            return this.dataTracker.get(TEXT);
        }

        private void setText(Text text) {
            this.dataTracker.set(TEXT, text);
        }

        private int getLineWidth() {
            return this.dataTracker.get(LINE_WIDTH);
        }

        private void setLineWidth(int lineWidth) {
            this.dataTracker.set(LINE_WIDTH, lineWidth);
        }

        private byte getTextOpacity() {
            return this.dataTracker.get(TEXT_OPACITY);
        }

        private void setTextOpacity(byte textOpacity) {
            this.dataTracker.set(TEXT_OPACITY, textOpacity);
        }

        private int getBackground() {
            return this.dataTracker.get(BACKGROUND);
        }

        private void setBackground(int background) {
            this.dataTracker.set(BACKGROUND, background);
        }

        private byte getDisplayFlags() {
            return this.dataTracker.get(TEXT_DISPLAY_FLAGS);
        }

        private void setDisplayFlags(byte flags) {
            this.dataTracker.set(TEXT_DISPLAY_FLAGS, flags);
        }

        private static byte readFlag(byte flags, ReadView view, String nbtKey, byte flag) {
            if (view.getBoolean(nbtKey, false)) {
                return (byte)(flags | flag);
            }
            return flags;
        }

        @Override
        protected void readCustomData(ReadView view) {
            super.readCustomData(view);
            this.setLineWidth(view.getInt(LINE_WIDTH_NBT_KEY, 200));
            this.setTextOpacity(view.getByte(TEXT_OPACITY_NBT_KEY, (byte)-1));
            this.setBackground(view.getInt(BACKGROUND_NBT_KEY, 0x40000000));
            byte b = TextDisplayEntity.readFlag((byte)0, view, SHADOW_NBT_KEY, SHADOW_FLAG);
            b = TextDisplayEntity.readFlag(b, view, SEE_THROUGH_NBT_KEY, SEE_THROUGH_FLAG);
            b = TextDisplayEntity.readFlag(b, view, DEFAULT_BACKGROUND_NBT_KEY, DEFAULT_BACKGROUND_FLAG);
            Optional<TextAlignment> optional = view.read(ALIGNMENT_NBT_KEY, TextAlignment.CODEC);
            if (optional.isPresent()) {
                b = switch (optional.get().ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> b;
                    case 1 -> (byte)(b | 8);
                    case 2 -> (byte)(b | 0x10);
                };
            }
            this.setDisplayFlags(b);
            Optional<Text> optional2 = view.read(TEXT_NBT_KEY, TextCodecs.CODEC);
            if (optional2.isPresent()) {
                try {
                    World world = this.getEntityWorld();
                    if (world instanceof ServerWorld) {
                        ServerWorld lv = (ServerWorld)world;
                        ServerCommandSource lv2 = this.getCommandSource(lv).withLevel(2);
                        MutableText lv3 = Texts.parse(lv2, optional2.get(), (Entity)this, 0);
                        this.setText(lv3);
                    } else {
                        this.setText(Text.empty());
                    }
                } catch (Exception exception) {
                    LOGGER.warn("Failed to parse display entity text {}", (Object)optional2, (Object)exception);
                }
            }
        }

        private static void writeFlag(byte flags, WriteView view, String nbtKey, byte flag) {
            view.putBoolean(nbtKey, (flags & flag) != 0);
        }

        @Override
        protected void writeCustomData(WriteView view) {
            super.writeCustomData(view);
            view.put(TEXT_NBT_KEY, TextCodecs.CODEC, this.getText());
            view.putInt(LINE_WIDTH_NBT_KEY, this.getLineWidth());
            view.putInt(BACKGROUND_NBT_KEY, this.getBackground());
            view.putByte(TEXT_OPACITY_NBT_KEY, this.getTextOpacity());
            byte b = this.getDisplayFlags();
            TextDisplayEntity.writeFlag(b, view, SHADOW_NBT_KEY, SHADOW_FLAG);
            TextDisplayEntity.writeFlag(b, view, SEE_THROUGH_NBT_KEY, SEE_THROUGH_FLAG);
            TextDisplayEntity.writeFlag(b, view, DEFAULT_BACKGROUND_NBT_KEY, DEFAULT_BACKGROUND_FLAG);
            view.put(ALIGNMENT_NBT_KEY, TextAlignment.CODEC, TextDisplayEntity.getAlignment(b));
        }

        @Override
        protected void refreshData(boolean shouldLerp, float lerpProgress) {
            this.data = shouldLerp && this.data != null ? this.getLerpedRenderState(this.data, lerpProgress) : this.copyData();
            this.textLines = null;
        }

        @Nullable
        public Data getData() {
            return this.data;
        }

        private Data copyData() {
            return new Data(this.getText(), this.getLineWidth(), IntLerper.constant(this.getTextOpacity()), IntLerper.constant(this.getBackground()), this.getDisplayFlags());
        }

        private Data getLerpedRenderState(Data data, float lerpProgress) {
            int i = data.backgroundColor.lerp(lerpProgress);
            int j = data.textOpacity.lerp(lerpProgress);
            return new Data(this.getText(), this.getLineWidth(), new IntLerperImpl(j, this.getTextOpacity()), new ArgbLerper(i, this.getBackground()), this.getDisplayFlags());
        }

        public TextLines splitLines(LineSplitter splitter) {
            if (this.textLines == null) {
                this.textLines = this.data != null ? splitter.split(this.data.text(), this.data.lineWidth()) : new TextLines(List.of(), 0);
            }
            return this.textLines;
        }

        public static TextAlignment getAlignment(byte flags) {
            if ((flags & LEFT_ALIGNMENT_FLAG) != 0) {
                return TextAlignment.LEFT;
            }
            if ((flags & RIGHT_ALIGNMENT_FLAG) != 0) {
                return TextAlignment.RIGHT;
            }
            return TextAlignment.CENTER;
        }

        public static enum TextAlignment implements StringIdentifiable
        {
            CENTER("center"),
            LEFT("left"),
            RIGHT("right");

            public static final Codec<TextAlignment> CODEC;
            private final String name;

            private TextAlignment(String name) {
                this.name = name;
            }

            @Override
            public String asString() {
                return this.name;
            }

            static {
                CODEC = StringIdentifiable.createCodec(TextAlignment::values);
            }
        }

        public record Data(Text text, int lineWidth, IntLerper textOpacity, IntLerper backgroundColor, byte flags) {
        }

        public record TextLines(List<TextLine> lines, int width) {
        }

        @FunctionalInterface
        public static interface LineSplitter {
            public TextLines split(Text var1, int var2);
        }

        public record TextLine(OrderedText contents, int width) {
        }
    }

    public static class BlockDisplayEntity
    extends DisplayEntity {
        public static final String BLOCK_STATE_NBT_KEY = "block_state";
        private static final TrackedData<BlockState> BLOCK_STATE = DataTracker.registerData(BlockDisplayEntity.class, TrackedDataHandlerRegistry.BLOCK_STATE);
        @Nullable
        private Data data;

        public BlockDisplayEntity(EntityType<?> arg, World arg2) {
            super(arg, arg2);
        }

        @Override
        protected void initDataTracker(DataTracker.Builder builder) {
            super.initDataTracker(builder);
            builder.add(BLOCK_STATE, Blocks.AIR.getDefaultState());
        }

        @Override
        public void onTrackedDataSet(TrackedData<?> data) {
            super.onTrackedDataSet(data);
            if (data.equals(BLOCK_STATE)) {
                this.renderingDataSet = true;
            }
        }

        private BlockState getBlockState() {
            return this.dataTracker.get(BLOCK_STATE);
        }

        private void setBlockState(BlockState state) {
            this.dataTracker.set(BLOCK_STATE, state);
        }

        @Override
        protected void readCustomData(ReadView view) {
            super.readCustomData(view);
            this.setBlockState(view.read(BLOCK_STATE_NBT_KEY, BlockState.CODEC).orElse(Blocks.AIR.getDefaultState()));
        }

        @Override
        protected void writeCustomData(WriteView view) {
            super.writeCustomData(view);
            view.put(BLOCK_STATE_NBT_KEY, BlockState.CODEC, this.getBlockState());
        }

        @Nullable
        public Data getData() {
            return this.data;
        }

        @Override
        protected void refreshData(boolean shouldLerp, float lerpProgress) {
            this.data = new Data(this.getBlockState());
        }

        public record Data(BlockState blockState) {
        }
    }

    public static class ItemDisplayEntity
    extends DisplayEntity {
        private static final String ITEM_NBT_KEY = "item";
        private static final String ITEM_DISPLAY_NBT_KEY = "item_display";
        private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(ItemDisplayEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
        private static final TrackedData<Byte> ITEM_DISPLAY = DataTracker.registerData(ItemDisplayEntity.class, TrackedDataHandlerRegistry.BYTE);
        private final StackReference stackReference = StackReference.of(this::getItemStack, this::setItemStack);
        @Nullable
        private Data data;

        public ItemDisplayEntity(EntityType<?> arg, World arg2) {
            super(arg, arg2);
        }

        @Override
        protected void initDataTracker(DataTracker.Builder builder) {
            super.initDataTracker(builder);
            builder.add(ITEM, ItemStack.EMPTY);
            builder.add(ITEM_DISPLAY, ItemDisplayContext.NONE.getIndex());
        }

        @Override
        public void onTrackedDataSet(TrackedData<?> data) {
            super.onTrackedDataSet(data);
            if (ITEM.equals(data) || ITEM_DISPLAY.equals(data)) {
                this.renderingDataSet = true;
            }
        }

        private ItemStack getItemStack() {
            return this.dataTracker.get(ITEM);
        }

        private void setItemStack(ItemStack stack) {
            this.dataTracker.set(ITEM, stack);
        }

        private void setItemDisplayContext(ItemDisplayContext context) {
            this.dataTracker.set(ITEM_DISPLAY, context.getIndex());
        }

        private ItemDisplayContext getItemDisplayContext() {
            return ItemDisplayContext.FROM_INDEX.apply(this.dataTracker.get(ITEM_DISPLAY).byteValue());
        }

        @Override
        protected void readCustomData(ReadView view) {
            super.readCustomData(view);
            this.setItemStack(view.read(ITEM_NBT_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY));
            this.setItemDisplayContext(view.read(ITEM_DISPLAY_NBT_KEY, ItemDisplayContext.CODEC).orElse(ItemDisplayContext.NONE));
        }

        @Override
        protected void writeCustomData(WriteView view) {
            super.writeCustomData(view);
            ItemStack lv = this.getItemStack();
            if (!lv.isEmpty()) {
                view.put(ITEM_NBT_KEY, ItemStack.CODEC, lv);
            }
            view.put(ITEM_DISPLAY_NBT_KEY, ItemDisplayContext.CODEC, this.getItemDisplayContext());
        }

        @Override
        public StackReference getStackReference(int mappedIndex) {
            if (mappedIndex == 0) {
                return this.stackReference;
            }
            return StackReference.EMPTY;
        }

        @Nullable
        public Data getData() {
            return this.data;
        }

        @Override
        protected void refreshData(boolean shouldLerp, float lerpProgress) {
            ItemStack lv = this.getItemStack();
            lv.setHolder(this);
            this.data = new Data(lv, this.getItemDisplayContext());
        }

        public record Data(ItemStack itemStack, ItemDisplayContext itemTransform) {
        }
    }
}

