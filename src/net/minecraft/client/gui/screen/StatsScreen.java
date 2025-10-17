/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.LoadingTab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.ItemStackWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StatsScreen
extends Screen {
    private static final Text TITLE_TEXT = Text.translatable("gui.stats");
    static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    static final Identifier HEADER_TEXTURE = Identifier.ofVanilla("statistics/header");
    static final Identifier SORT_UP_TEXTURE = Identifier.ofVanilla("statistics/sort_up");
    static final Identifier SORT_DOWN_TEXTURE = Identifier.ofVanilla("statistics/sort_down");
    private static final Text DOWNLOADING_STATS_TEXT = Text.translatable("multiplayer.downloadingStats");
    static final Text NONE_TEXT = Text.translatable("stats.none");
    private static final Text GENERAL_BUTTON_TEXT = Text.translatable("stat.generalButton");
    private static final Text ITEM_BUTTON_TEXT = Text.translatable("stat.itemsButton");
    private static final Text MOBS_BUTTON_TEXT = Text.translatable("stat.mobsButton");
    protected final Screen parent;
    private static final int field_49520 = 280;
    final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final TabManager tabManager = new TabManager(child -> {
        ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
    }, child -> this.remove((Element)child));
    @Nullable
    private TabNavigationWidget tabNavigationWidget;
    final StatHandler statHandler;
    private boolean downloadingStats = true;

    public StatsScreen(Screen parent, StatHandler statHandler) {
        super(TITLE_TEXT);
        this.parent = parent;
        this.statHandler = statHandler;
    }

    @Override
    protected void init() {
        Text lv = DOWNLOADING_STATS_TEXT;
        this.tabNavigationWidget = TabNavigationWidget.builder(this.tabManager, this.width).tabs(new LoadingTab(this.getTextRenderer(), GENERAL_BUTTON_TEXT, lv), new LoadingTab(this.getTextRenderer(), ITEM_BUTTON_TEXT, lv), new LoadingTab(this.getTextRenderer(), MOBS_BUTTON_TEXT, lv)).build();
        this.addDrawableChild(this.tabNavigationWidget);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(200).build());
        this.tabNavigationWidget.setTabActive(0, true);
        this.tabNavigationWidget.setTabActive(1, false);
        this.tabNavigationWidget.setTabActive(2, false);
        this.layout.forEachChild(child -> {
            child.setNavigationOrder(1);
            this.addDrawableChild(child);
        });
        this.tabNavigationWidget.selectTab(0, false);
        this.refreshWidgetPositions();
        this.client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
    }

    public void onStatsReady() {
        if (this.downloadingStats) {
            if (this.tabNavigationWidget != null) {
                this.remove(this.tabNavigationWidget);
            }
            this.tabNavigationWidget = TabNavigationWidget.builder(this.tabManager, this.width).tabs(new StatsTab(GENERAL_BUTTON_TEXT, new GeneralStatsListWidget(this.client)), new StatsTab(ITEM_BUTTON_TEXT, new ItemStatsListWidget(this.client)), new StatsTab(MOBS_BUTTON_TEXT, new EntityStatsListWidget(this.client))).build();
            this.setFocused(this.tabNavigationWidget);
            this.addDrawableChild(this.tabNavigationWidget);
            this.refreshTab(1);
            this.refreshTab(2);
            this.tabNavigationWidget.selectTab(0, false);
            this.refreshWidgetPositions();
            this.downloadingStats = false;
        }
    }

    /*
     * Unable to fully structure code
     */
    private void refreshTab(int tab) {
        if (this.tabNavigationWidget == null) {
            return;
        }
        var4_2 = this.tabNavigationWidget.getTabs().get(tab);
        if (!(var4_2 instanceof StatsTab)) ** GOTO lbl-1000
        lv = (StatsTab)var4_2;
        if (!lv.widget.children().isEmpty()) {
            v0 = true;
        } else lbl-1000:
        // 2 sources

        {
            v0 = false;
        }
        bl = v0;
        this.tabNavigationWidget.setTabActive(tab, bl);
        if (bl) {
            this.tabNavigationWidget.setTabTooltip(tab, null);
        } else {
            this.tabNavigationWidget.setTabTooltip(tab, Tooltip.of(Text.translatable("gui.stats.none_found")));
        }
    }

    @Override
    protected void refreshWidgetPositions() {
        if (this.tabNavigationWidget == null) {
            return;
        }
        this.tabNavigationWidget.setWidth(this.width);
        this.tabNavigationWidget.init();
        int i = this.tabNavigationWidget.getNavigationFocus().getBottom();
        ScreenRect lv = new ScreenRect(0, i, this.width, this.height - this.layout.getFooterHeight() - i);
        this.tabNavigationWidget.getTabs().forEach(tab -> tab.forEachChild(child -> child.setHeight(lv.height())));
        this.tabManager.setTabArea(lv);
        this.layout.setHeaderHeight(i);
        this.layout.refreshPositions();
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.tabNavigationWidget != null && this.tabNavigationWidget.keyPressed(input)) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR_TEXTURE, 0, this.height - this.layout.getFooterHeight(), 0.0f, 0.0f, this.width, 2, 32, 2);
    }

    @Override
    protected void renderDarkening(DrawContext context) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.TAB_HEADER_BACKGROUND_TEXTURE, 0, 0, 0.0f, 0.0f, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderDarkening(context, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    static String getStatTranslationKey(Stat<Identifier> stat) {
        return "stat." + stat.getValue().toString().replace(':', '.');
    }

    @Environment(value=EnvType.CLIENT)
    class StatsTab
    extends GridScreenTab {
        protected final EntryListWidget<?> widget;

        public StatsTab(Text title, EntryListWidget<?> widget) {
            super(title);
            this.grid.add(widget, 1, 1);
            this.widget = widget;
        }

        @Override
        public void refreshGrid(ScreenRect tabArea) {
            this.widget.position(StatsScreen.this.width, StatsScreen.this.layout.getContentHeight(), StatsScreen.this.layout.getHeaderHeight());
            super.refreshGrid(tabArea);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class GeneralStatsListWidget
    extends AlwaysSelectedEntryListWidget<Entry> {
        public GeneralStatsListWidget(MinecraftClient client) {
            super(client, StatsScreen.this.width, StatsScreen.this.layout.getContentHeight(), 33, 14);
            ObjectArrayList<Stat<Identifier>> objectArrayList = new ObjectArrayList<Stat<Identifier>>(Stats.CUSTOM.iterator());
            objectArrayList.sort((Comparator<Stat<Identifier>>)Comparator.comparing(stat -> I18n.translate(StatsScreen.getStatTranslationKey(stat), new Object[0])));
            for (Stat stat2 : objectArrayList) {
                this.addEntry(new Entry(stat2));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Override
        protected void drawMenuListBackground(DrawContext context) {
        }

        @Override
        protected void drawHeaderAndFooterSeparators(DrawContext context) {
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            private final Stat<Identifier> stat;
            private final Text displayName;

            Entry(Stat<Identifier> stat) {
                this.stat = stat;
                this.displayName = Text.translatable(StatsScreen.getStatTranslationKey(stat));
            }

            private String getFormatted() {
                return this.stat.format(StatsScreen.this.statHandler.getStat(this.stat));
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                int k = this.getContentMiddleY() - ((StatsScreen)StatsScreen.this).textRenderer.fontHeight / 2;
                int l = GeneralStatsListWidget.this.children().indexOf(this);
                int m = l % 2 == 0 ? Colors.WHITE : Colors.ALTERNATE_WHITE;
                context.drawTextWithShadow(StatsScreen.this.textRenderer, this.displayName, this.getContentX() + 2, k, m);
                String string = this.getFormatted();
                context.drawTextWithShadow(StatsScreen.this.textRenderer, string, this.getContentRightEnd() - StatsScreen.this.textRenderer.getWidth(string) - 4, k, m);
            }

            @Override
            public Text getNarration() {
                return Text.translatable("narrator.select", Text.empty().append(this.displayName).append(ScreenTexts.SPACE).append(this.getFormatted()));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ItemStatsListWidget
    extends ElementListWidget<Entry> {
        private static final int field_49524 = 18;
        private static final int field_49525 = 22;
        private static final int field_49526 = 1;
        private static final int field_49527 = 0;
        private static final int field_49528 = -1;
        private static final int field_49529 = 1;
        protected final List<StatType<Block>> blockStatTypes;
        protected final List<StatType<Item>> itemStatTypes;
        protected final Comparator<StatEntry> comparator;
        @Nullable
        protected StatType<?> selectedStatType;
        protected int listOrder;

        public ItemStatsListWidget(MinecraftClient client) {
            boolean bl;
            super(client, StatsScreen.this.width, StatsScreen.this.layout.getContentHeight(), 33, 22);
            this.comparator = new ItemComparator();
            this.blockStatTypes = Lists.newArrayList();
            this.blockStatTypes.add(Stats.MINED);
            this.itemStatTypes = Lists.newArrayList(Stats.BROKEN, Stats.CRAFTED, Stats.USED, Stats.PICKED_UP, Stats.DROPPED);
            Set<Item> set = Sets.newIdentityHashSet();
            for (Item lv : Registries.ITEM) {
                bl = false;
                for (StatType<Item> statType : this.itemStatTypes) {
                    if (!statType.hasStat(lv) || StatsScreen.this.statHandler.getStat(statType.getOrCreateStat(lv)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(lv);
            }
            for (Block lv3 : Registries.BLOCK) {
                bl = false;
                for (StatType<ItemConvertible> statType : this.blockStatTypes) {
                    if (!statType.hasStat(lv3) || StatsScreen.this.statHandler.getStat(statType.getOrCreateStat(lv3)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(lv3.asItem());
            }
            set.remove(Items.AIR);
            if (!set.isEmpty()) {
                this.addEntry(new Header());
                for (Item lv : set) {
                    this.addEntry(new StatEntry(lv));
                }
            }
        }

        @Override
        protected void drawMenuListBackground(DrawContext context) {
        }

        int getIconX(int index) {
            return 75 + 40 * index;
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        StatType<?> getStatType(int headerColumn) {
            return headerColumn < this.blockStatTypes.size() ? this.blockStatTypes.get(headerColumn) : this.itemStatTypes.get(headerColumn - this.blockStatTypes.size());
        }

        int getHeaderIndex(StatType<?> statType) {
            int i = this.blockStatTypes.indexOf(statType);
            if (i >= 0) {
                return i;
            }
            int j = this.itemStatTypes.indexOf(statType);
            if (j >= 0) {
                return j + this.blockStatTypes.size();
            }
            return -1;
        }

        protected void selectStatType(StatType<?> statType) {
            if (statType != this.selectedStatType) {
                this.selectedStatType = statType;
                this.listOrder = -1;
            } else if (this.listOrder == -1) {
                this.listOrder = 1;
            } else {
                this.selectedStatType = null;
                this.listOrder = 0;
            }
            this.sortStats(this.comparator);
        }

        protected void sortStats(Comparator<StatEntry> comparator) {
            List<StatEntry> list = this.getStatEntries();
            list.sort(comparator);
            this.clearEntriesExcept((Entry)this.children().getFirst());
            for (StatEntry lv : list) {
                this.addEntry(lv);
            }
        }

        private List<StatEntry> getStatEntries() {
            ArrayList<StatEntry> list = new ArrayList<StatEntry>();
            this.children().forEach(child -> {
                if (child instanceof StatEntry) {
                    StatEntry lv = (StatEntry)child;
                    list.add(lv);
                }
            });
            return list;
        }

        @Override
        protected void drawHeaderAndFooterSeparators(DrawContext context) {
        }

        @Environment(value=EnvType.CLIENT)
        class ItemComparator
        implements Comparator<StatEntry> {
            ItemComparator() {
            }

            @Override
            public int compare(StatEntry arg, StatEntry arg2) {
                int j;
                int i;
                Item lv = arg.getItem();
                Item lv2 = arg2.getItem();
                if (ItemStatsListWidget.this.selectedStatType == null) {
                    i = 0;
                    j = 0;
                } else if (ItemStatsListWidget.this.blockStatTypes.contains(ItemStatsListWidget.this.selectedStatType)) {
                    StatType<?> lv3 = ItemStatsListWidget.this.selectedStatType;
                    i = lv instanceof BlockItem ? StatsScreen.this.statHandler.getStat(lv3, ((BlockItem)lv).getBlock()) : -1;
                    j = lv2 instanceof BlockItem ? StatsScreen.this.statHandler.getStat(lv3, ((BlockItem)lv2).getBlock()) : -1;
                } else {
                    StatType<?> lv3 = ItemStatsListWidget.this.selectedStatType;
                    i = StatsScreen.this.statHandler.getStat(lv3, lv);
                    j = StatsScreen.this.statHandler.getStat(lv3, lv2);
                }
                if (i == j) {
                    return ItemStatsListWidget.this.listOrder * Integer.compare(Item.getRawId(lv), Item.getRawId(lv2));
                }
                return ItemStatsListWidget.this.listOrder * Integer.compare(i, j);
            }

            @Override
            public /* synthetic */ int compare(Object a, Object b) {
                return this.compare((StatEntry)a, (StatEntry)b);
            }
        }

        @Environment(value=EnvType.CLIENT)
        class Header
        extends Entry {
            private static final Identifier BLOCK_MINED_TEXTURE = Identifier.ofVanilla("statistics/block_mined");
            private static final Identifier ITEM_BROKEN_TEXTURE = Identifier.ofVanilla("statistics/item_broken");
            private static final Identifier ITEM_CRAFTED_TEXTURE = Identifier.ofVanilla("statistics/item_crafted");
            private static final Identifier ITEM_USED_TEXTURE = Identifier.ofVanilla("statistics/item_used");
            private static final Identifier ITEM_PICKED_UP_TEXTURE = Identifier.ofVanilla("statistics/item_picked_up");
            private static final Identifier ITEM_DROPPED_TEXTURE = Identifier.ofVanilla("statistics/item_dropped");
            private final HeaderButton blockMinedButton;
            private final HeaderButton itemBrokenButton;
            private final HeaderButton itemCraftedButton;
            private final HeaderButton itemUsedButton;
            private final HeaderButton itemPickedUpButton;
            private final HeaderButton itemDroppedButton;
            private final List<ClickableWidget> buttons = new ArrayList<ClickableWidget>();

            Header() {
                this.blockMinedButton = new HeaderButton(this, 0, BLOCK_MINED_TEXTURE);
                this.itemBrokenButton = new HeaderButton(this, 1, ITEM_BROKEN_TEXTURE);
                this.itemCraftedButton = new HeaderButton(this, 2, ITEM_CRAFTED_TEXTURE);
                this.itemUsedButton = new HeaderButton(this, 3, ITEM_USED_TEXTURE);
                this.itemPickedUpButton = new HeaderButton(this, 4, ITEM_PICKED_UP_TEXTURE);
                this.itemDroppedButton = new HeaderButton(this, 5, ITEM_DROPPED_TEXTURE);
                this.buttons.addAll(List.of(this.blockMinedButton, this.itemBrokenButton, this.itemCraftedButton, this.itemUsedButton, this.itemPickedUpButton, this.itemDroppedButton));
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                this.blockMinedButton.setPosition(this.getContentX() + ItemStatsListWidget.this.getIconX(0) - 18, this.getContentY() + 1);
                this.blockMinedButton.render(context, mouseX, mouseY, deltaTicks);
                this.itemBrokenButton.setPosition(this.getContentX() + ItemStatsListWidget.this.getIconX(1) - 18, this.getContentY() + 1);
                this.itemBrokenButton.render(context, mouseX, mouseY, deltaTicks);
                this.itemCraftedButton.setPosition(this.getContentX() + ItemStatsListWidget.this.getIconX(2) - 18, this.getContentY() + 1);
                this.itemCraftedButton.render(context, mouseX, mouseY, deltaTicks);
                this.itemUsedButton.setPosition(this.getContentX() + ItemStatsListWidget.this.getIconX(3) - 18, this.getContentY() + 1);
                this.itemUsedButton.render(context, mouseX, mouseY, deltaTicks);
                this.itemPickedUpButton.setPosition(this.getContentX() + ItemStatsListWidget.this.getIconX(4) - 18, this.getContentY() + 1);
                this.itemPickedUpButton.render(context, mouseX, mouseY, deltaTicks);
                this.itemDroppedButton.setPosition(this.getContentX() + ItemStatsListWidget.this.getIconX(5) - 18, this.getContentY() + 1);
                this.itemDroppedButton.render(context, mouseX, mouseY, deltaTicks);
                if (ItemStatsListWidget.this.selectedStatType != null) {
                    int k = ItemStatsListWidget.this.getIconX(ItemStatsListWidget.this.getHeaderIndex(ItemStatsListWidget.this.selectedStatType)) - 36;
                    Identifier lv = ItemStatsListWidget.this.listOrder == 1 ? SORT_UP_TEXTURE : SORT_DOWN_TEXTURE;
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv, this.getContentX() + k, this.getContentY() + 1, 18, 18);
                }
            }

            @Override
            public List<? extends Element> children() {
                return this.buttons;
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return this.buttons;
            }

            @Environment(value=EnvType.CLIENT)
            class HeaderButton
            extends TexturedButtonWidget {
                private final Identifier texture;

                HeaderButton(Header arg, int index, Identifier texture) {
                    super(18, 18, new ButtonTextures(HEADER_TEXTURE, SLOT_TEXTURE), button -> arg.ItemStatsListWidget.this.selectStatType(arg.ItemStatsListWidget.this.getStatType(index)), arg.ItemStatsListWidget.this.getStatType(index).getName());
                    this.texture = texture;
                    this.setTooltip(Tooltip.of(this.getMessage()));
                }

                @Override
                public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
                    Identifier lv = this.textures.get(this.isInteractable(), this.isSelected());
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv, this.getX(), this.getY(), this.width, this.height);
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(), this.width, this.height);
                }
            }
        }

        @Environment(value=EnvType.CLIENT)
        class StatEntry
        extends Entry {
            private final Item item;
            private final ItemStackInSlotWidget button;

            StatEntry(Item item) {
                this.item = item;
                this.button = new ItemStackInSlotWidget(item.getDefaultStack());
            }

            protected Item getItem() {
                return this.item;
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                int l;
                this.button.setPosition(this.getContentX(), this.getContentY());
                this.button.render(context, mouseX, mouseY, deltaTicks);
                ItemStatsListWidget lv = ItemStatsListWidget.this;
                int k = lv.children().indexOf(this);
                for (l = 0; l < lv.blockStatTypes.size(); ++l) {
                    Stat<Block> lv3;
                    Item item = this.item;
                    if (item instanceof BlockItem) {
                        BlockItem lv2 = (BlockItem)item;
                        lv3 = lv.blockStatTypes.get(l).getOrCreateStat(lv2.getBlock());
                    } else {
                        lv3 = null;
                    }
                    this.render(context, lv3, this.getContentX() + ItemStatsListWidget.this.getIconX(l), this.getContentMiddleY() - ((StatsScreen)StatsScreen.this).textRenderer.fontHeight / 2, k % 2 == 0);
                }
                for (l = 0; l < lv.itemStatTypes.size(); ++l) {
                    this.render(context, lv.itemStatTypes.get(l).getOrCreateStat(this.item), this.getContentX() + ItemStatsListWidget.this.getIconX(l + lv.blockStatTypes.size()), this.getContentMiddleY() - ((StatsScreen)StatsScreen.this).textRenderer.fontHeight / 2, k % 2 == 0);
                }
            }

            protected void render(DrawContext context, @Nullable Stat<?> stat, int x, int y, boolean white) {
                Text lv = stat == null ? NONE_TEXT : Text.literal(stat.format(StatsScreen.this.statHandler.getStat(stat)));
                context.drawTextWithShadow(StatsScreen.this.textRenderer, lv, x - StatsScreen.this.textRenderer.getWidth(lv), y, white ? Colors.WHITE : Colors.ALTERNATE_WHITE);
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return List.of(this.button);
            }

            @Override
            public List<? extends Element> children() {
                return List.of(this.button);
            }

            @Environment(value=EnvType.CLIENT)
            class ItemStackInSlotWidget
            extends ItemStackWidget {
                ItemStackInSlotWidget(ItemStack stack) {
                    super(ItemStatsListWidget.this.client, 1, 1, 18, 18, stack.getName(), stack, false, true);
                }

                @Override
                protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, StatEntry.this.getContentX(), StatEntry.this.getContentY(), 18, 18);
                    super.renderWidget(context, mouseX, mouseY, deltaTicks);
                }

                @Override
                protected void renderTooltip(DrawContext context, int mouseX, int mouseY) {
                    super.renderTooltip(context, StatEntry.this.getContentX() + 18, StatEntry.this.getContentY() + 18);
                }
            }
        }

        @Environment(value=EnvType.CLIENT)
        static abstract class Entry
        extends ElementListWidget.Entry<Entry> {
            Entry() {
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class EntityStatsListWidget
    extends AlwaysSelectedEntryListWidget<Entry> {
        public EntityStatsListWidget(MinecraftClient client) {
            super(client, StatsScreen.this.width, StatsScreen.this.layout.getContentHeight(), 33, ((StatsScreen)StatsScreen.this).textRenderer.fontHeight * 4);
            for (EntityType entityType : Registries.ENTITY_TYPE) {
                if (StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType)) <= 0 && StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType)) <= 0) continue;
                this.addEntry(new Entry(entityType));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Override
        protected void drawMenuListBackground(DrawContext context) {
        }

        @Override
        protected void drawHeaderAndFooterSeparators(DrawContext context) {
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            private final Text entityTypeName;
            private final Text killedText;
            private final Text killedByText;
            private final boolean killedAny;
            private final boolean killedByAny;

            public Entry(EntityType<?> entityType) {
                this.entityTypeName = entityType.getName();
                int i = StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType));
                if (i == 0) {
                    this.killedText = Text.translatable("stat_type.minecraft.killed.none", this.entityTypeName);
                    this.killedAny = false;
                } else {
                    this.killedText = Text.translatable("stat_type.minecraft.killed", i, this.entityTypeName);
                    this.killedAny = true;
                }
                int j = StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType));
                if (j == 0) {
                    this.killedByText = Text.translatable("stat_type.minecraft.killed_by.none", this.entityTypeName);
                    this.killedByAny = false;
                } else {
                    this.killedByText = Text.translatable("stat_type.minecraft.killed_by", this.entityTypeName, j);
                    this.killedByAny = true;
                }
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                context.drawTextWithShadow(StatsScreen.this.textRenderer, this.entityTypeName, this.getContentX() + 2, this.getContentY() + 1, Colors.WHITE);
                context.drawTextWithShadow(StatsScreen.this.textRenderer, this.killedText, this.getContentX() + 2 + 10, this.getContentY() + 1 + ((StatsScreen)StatsScreen.this).textRenderer.fontHeight, this.killedAny ? Colors.ALTERNATE_WHITE : Colors.GRAY);
                context.drawTextWithShadow(StatsScreen.this.textRenderer, this.killedByText, this.getContentX() + 2 + 10, this.getContentY() + 1 + ((StatsScreen)StatsScreen.this).textRenderer.fontHeight * 2, this.killedByAny ? Colors.ALTERNATE_WHITE : Colors.GRAY);
            }

            @Override
            public Text getNarration() {
                return Text.translatable("narrator.select", ScreenTexts.joinSentences(this.killedText, this.killedByText));
            }
        }
    }
}

