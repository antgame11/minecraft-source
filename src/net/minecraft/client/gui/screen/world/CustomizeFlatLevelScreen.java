/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.world;

import java.util.List;
import java.util.SequencedCollection;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.PresetsScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CustomizeFlatLevelScreen
extends Screen {
    private static final Text TITLE = Text.translatable("createWorld.customize.flat.title");
    static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    private static final int ICON_SIZE = 18;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ICON_BACKGROUND_OFFSET_X = 1;
    private static final int ICON_BACKGROUND_OFFSET_Y = 1;
    private static final int ICON_OFFSET_X = 2;
    private static final int ICON_OFFSET_Y = 2;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 33, 64);
    protected final CreateWorldScreen parent;
    private final Consumer<FlatChunkGeneratorConfig> configConsumer;
    FlatChunkGeneratorConfig config;
    @Nullable
    private SuperflatLayersListWidget layers;
    @Nullable
    private ButtonWidget widgetButtonRemoveLayer;

    public CustomizeFlatLevelScreen(CreateWorldScreen parent, Consumer<FlatChunkGeneratorConfig> configConsumer, FlatChunkGeneratorConfig config) {
        super(TITLE);
        this.parent = parent;
        this.configConsumer = configConsumer;
        this.config = config;
    }

    public FlatChunkGeneratorConfig getConfig() {
        return this.config;
    }

    public void setConfig(FlatChunkGeneratorConfig config) {
        this.config = config;
        if (this.layers != null) {
            this.layers.updateLayers();
            this.updateRemoveLayerButton();
        }
    }

    @Override
    protected void init() {
        this.layout.addHeader(this.title, this.textRenderer);
        this.layers = this.layout.addBody(new SuperflatLayersListWidget());
        DirectionalLayoutWidget lv = this.layout.addFooter(DirectionalLayoutWidget.vertical().spacing(4));
        lv.getMainPositioner().alignVerticalCenter();
        DirectionalLayoutWidget lv2 = lv.add(DirectionalLayoutWidget.horizontal().spacing(8));
        DirectionalLayoutWidget lv3 = lv.add(DirectionalLayoutWidget.horizontal().spacing(8));
        this.widgetButtonRemoveLayer = lv2.add(ButtonWidget.builder(Text.translatable("createWorld.customize.flat.removeLayer"), button -> {
            Object lv;
            if (this.layers != null && (lv = this.layers.getSelectedOrNull()) instanceof SuperflatLayersListWidget.SuperflatLayerEntry) {
                SuperflatLayersListWidget.SuperflatLayerEntry lv2 = (SuperflatLayersListWidget.SuperflatLayerEntry)lv;
                this.layers.removeLayer(lv2);
            }
        }).build());
        lv2.add(ButtonWidget.builder(Text.translatable("createWorld.customize.presets"), button -> {
            this.client.setScreen(new PresetsScreen(this));
            this.config.updateLayerBlocks();
            this.updateRemoveLayerButton();
        }).build());
        lv3.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            this.configConsumer.accept(this.config);
            this.close();
            this.config.updateLayerBlocks();
        }).build());
        lv3.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            this.close();
            this.config.updateLayerBlocks();
        }).build());
        this.config.updateLayerBlocks();
        this.updateRemoveLayerButton();
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        if (this.layers != null) {
            this.layers.position(this.width, this.layout);
        }
        this.layout.refreshPositions();
    }

    void updateRemoveLayerButton() {
        if (this.widgetButtonRemoveLayer != null) {
            this.widgetButtonRemoveLayer.active = this.hasLayerSelected();
        }
    }

    private boolean hasLayerSelected() {
        return this.layers != null && this.layers.getSelectedOrNull() instanceof SuperflatLayersListWidget.SuperflatLayerEntry;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Environment(value=EnvType.CLIENT)
    class SuperflatLayersListWidget
    extends AlwaysSelectedEntryListWidget<Entry> {
        static final Text LAYER_MATERIAL_TEXT = Text.translatable("createWorld.customize.flat.tile").formatted(Formatting.UNDERLINE);
        static final Text HEIGHT_TEXT = Text.translatable("createWorld.customize.flat.height").formatted(Formatting.UNDERLINE);

        public SuperflatLayersListWidget() {
            super(CustomizeFlatLevelScreen.this.client, CustomizeFlatLevelScreen.this.width, CustomizeFlatLevelScreen.this.height - 103, 43, 24);
            this.refreshLayers();
        }

        private void refreshLayers() {
            this.addEntry(new HeaderEntry(CustomizeFlatLevelScreen.this.textRenderer), (int)((double)CustomizeFlatLevelScreen.this.textRenderer.fontHeight * 1.5));
            SequencedCollection list = CustomizeFlatLevelScreen.this.config.getLayers().reversed();
            for (int i = 0; i < list.size(); ++i) {
                this.addEntry(new SuperflatLayerEntry((FlatChunkGeneratorLayer)list.get(i), i));
            }
        }

        @Override
        public void setSelected(@Nullable Entry arg) {
            super.setSelected(arg);
            CustomizeFlatLevelScreen.this.updateRemoveLayerButton();
        }

        public void updateLayers() {
            int i = this.children().indexOf(this.getSelectedOrNull());
            this.clearEntries();
            this.refreshLayers();
            List list = this.children();
            if (i >= 0 && i < list.size()) {
                this.setSelected((Entry)list.get(i));
            }
        }

        void removeLayer(SuperflatLayerEntry layer) {
            List<FlatChunkGeneratorLayer> list = CustomizeFlatLevelScreen.this.config.getLayers();
            int i = this.children().indexOf(layer);
            this.removeEntry(layer);
            list.remove(layer.layer);
            this.setSelected(list.isEmpty() ? null : (Entry)this.children().get(Math.min(i, list.size())));
            CustomizeFlatLevelScreen.this.config.updateLayerBlocks();
            this.updateLayers();
            CustomizeFlatLevelScreen.this.updateRemoveLayerButton();
        }

        @Environment(value=EnvType.CLIENT)
        static class HeaderEntry
        extends Entry {
            private final TextRenderer textRenderer;

            public HeaderEntry(TextRenderer textRenderer) {
                this.textRenderer = textRenderer;
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                context.drawTextWithShadow(this.textRenderer, LAYER_MATERIAL_TEXT, this.getContentX(), this.getContentY(), Colors.WHITE);
                context.drawTextWithShadow(this.textRenderer, HEIGHT_TEXT, this.getContentRightEnd() - this.textRenderer.getWidth(HEIGHT_TEXT), this.getContentY(), Colors.WHITE);
            }

            @Override
            public Text getNarration() {
                return ScreenTexts.joinSentences(LAYER_MATERIAL_TEXT, HEIGHT_TEXT);
            }
        }

        @Environment(value=EnvType.CLIENT)
        class SuperflatLayerEntry
        extends Entry {
            final FlatChunkGeneratorLayer layer;
            private final int index;

            public SuperflatLayerEntry(FlatChunkGeneratorLayer layer, int index) {
                this.layer = layer;
                this.index = index;
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                BlockState lv = this.layer.getBlockState();
                ItemStack lv2 = this.createItemStackFor(lv);
                this.renderIcon(context, this.getContentX(), this.getContentY(), lv2);
                int k = this.getContentMiddleY() - CustomizeFlatLevelScreen.this.textRenderer.fontHeight / 2;
                context.drawTextWithShadow(CustomizeFlatLevelScreen.this.textRenderer, lv2.getName(), this.getContentX() + 18 + 5, k, Colors.WHITE);
                MutableText lv3 = this.index == 0 ? Text.translatable("createWorld.customize.flat.layer.top", this.layer.getThickness()) : (this.index == CustomizeFlatLevelScreen.this.config.getLayers().size() - 1 ? Text.translatable("createWorld.customize.flat.layer.bottom", this.layer.getThickness()) : Text.translatable("createWorld.customize.flat.layer", this.layer.getThickness()));
                context.drawTextWithShadow(CustomizeFlatLevelScreen.this.textRenderer, lv3, this.getContentRightEnd() - CustomizeFlatLevelScreen.this.textRenderer.getWidth(lv3), k, Colors.WHITE);
            }

            private ItemStack createItemStackFor(BlockState state) {
                Item lv = state.getBlock().asItem();
                if (lv == Items.AIR) {
                    if (state.isOf(Blocks.WATER)) {
                        lv = Items.WATER_BUCKET;
                    } else if (state.isOf(Blocks.LAVA)) {
                        lv = Items.LAVA_BUCKET;
                    }
                }
                return new ItemStack(lv);
            }

            @Override
            public Text getNarration() {
                ItemStack lv = this.createItemStackFor(this.layer.getBlockState());
                if (!lv.isEmpty()) {
                    return ScreenTexts.joinSentences(Text.translatable("narrator.select", lv.getName()), HEIGHT_TEXT, Text.literal(String.valueOf(this.layer.getThickness())));
                }
                return ScreenTexts.EMPTY;
            }

            @Override
            public boolean mouseClicked(Click click, boolean doubled) {
                SuperflatLayersListWidget.this.setSelected(this);
                return super.mouseClicked(click, doubled);
            }

            private void renderIcon(DrawContext context, int x, int y, ItemStack iconItem) {
                this.renderIconBackgroundTexture(context, x + 1, y + 1);
                if (!iconItem.isEmpty()) {
                    context.drawItemWithoutEntity(iconItem, x + 2, y + 2);
                }
            }

            private void renderIconBackgroundTexture(DrawContext context, int x, int y) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, x, y, 18, 18);
            }
        }

        @Environment(value=EnvType.CLIENT)
        static abstract class Entry
        extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            Entry() {
            }
        }
    }
}

