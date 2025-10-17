/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.floats.FloatComparators;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudEntryCategory;
import net.minecraft.client.gui.hud.debug.DebugHudEntryVisibility;
import net.minecraft.client.gui.hud.debug.DebugProfileType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugOptionsScreen
extends Screen {
    private static final Text TITLE = Text.translatable("debug.options.title");
    private static final Text WARNING_TEXT = Text.translatable("debug.options.warning");
    static final Text ALWAYS_ON_TEXT = Text.translatable("debug.entry.always");
    static final Text IN_F3_TEXT = Text.translatable("debug.entry.f3");
    static final Text NEVER_TEXT = ScreenTexts.OFF;
    static final Text NOT_ALLOWED_TEXT = Text.translatable("debug.options.notAllowed.tooltip");
    private static final Text SEARCH_TEXT = Text.translatable("debug.options.search").fillStyle(TextFieldWidget.SEARCH_STYLE);
    final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 61, 33);
    @Nullable
    private OptionsListWidget optionsListWidget;
    private TextFieldWidget searchStringWidget;
    final List<ButtonWidget> profileButtons = new ArrayList<ButtonWidget>();

    public DebugOptionsScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        DirectionalLayoutWidget lv = this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(8));
        this.optionsListWidget = new OptionsListWidget();
        int i = this.optionsListWidget.getRowWidth();
        DirectionalLayoutWidget lv2 = DirectionalLayoutWidget.horizontal().spacing(8);
        lv2.add(new EmptyWidget(i / 3, 1));
        lv2.add(new TextWidget(TITLE, this.textRenderer), lv2.copyPositioner().alignVerticalCenter());
        this.searchStringWidget = new TextFieldWidget(this.textRenderer, 0, 0, i / 3, 20, this.searchStringWidget, SEARCH_TEXT);
        this.searchStringWidget.setChangedListener(searchString -> this.optionsListWidget.fillEntries((String)searchString));
        this.searchStringWidget.setPlaceholder(SEARCH_TEXT);
        lv2.add(this.searchStringWidget);
        lv.add(lv2, Positioner::alignHorizontalCenter);
        lv.add(new MultilineTextWidget(WARNING_TEXT, this.textRenderer).setMaxWidth(i).setCentered(true).setTextColor(-2142128), Positioner::alignHorizontalCenter);
        this.layout.addBody(this.optionsListWidget);
        DirectionalLayoutWidget lv3 = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        this.addProfile(DebugProfileType.DEFAULT, lv3);
        this.addProfile(DebugProfileType.PERFORMANCE, lv3);
        lv3.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(60).build());
        this.layout.forEachChild(widget -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(widget);
        });
        this.refreshWidgetPositions();
    }

    @Override
    public void applyBlur(DrawContext context) {
        this.client.inGameHud.renderDebugHud(context);
        super.applyBlur(context);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.searchStringWidget);
    }

    private void addProfile(DebugProfileType profileType, DirectionalLayoutWidget widget) {
        ButtonWidget lv = ButtonWidget.builder(Text.translatable(profileType.getTranslationKey()), button -> {
            this.client.debugHudEntryList.setProfileType(profileType);
            this.client.debugHudEntryList.saveProfileFile();
            this.optionsListWidget.init();
            for (ButtonWidget lv : this.profileButtons) {
                lv.active = true;
            }
            button.active = false;
        }).width(120).build();
        lv.active = !this.client.debugHudEntryList.profileTypeMatches(profileType);
        this.profileButtons.add(lv);
        widget.add(lv);
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.optionsListWidget != null) {
            this.optionsListWidget.position(this.width, this.layout);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Environment(value=EnvType.CLIENT)
    class OptionsListWidget
    extends ElementListWidget<AbstractEntry> {
        private static final Comparator<Map.Entry<Identifier, DebugHudEntry>> ENTRY_COMPARATOR = (a, b) -> {
            int i = FloatComparators.NATURAL_COMPARATOR.compare(((DebugHudEntry)a.getValue()).getCategory().sortKey(), ((DebugHudEntry)b.getValue()).getCategory().sortKey());
            if (i != 0) {
                return i;
            }
            return ((Identifier)a.getKey()).compareTo((Identifier)b.getKey());
        };
        private static final int ITEM_HEIGHT = 20;

        public OptionsListWidget() {
            super(MinecraftClient.getInstance(), DebugOptionsScreen.this.width, DebugOptionsScreen.this.layout.getContentHeight(), DebugOptionsScreen.this.layout.getHeaderHeight(), 20);
            this.fillEntries("");
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            super.renderWidget(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        public void init() {
            this.children().forEach(AbstractEntry::init);
        }

        public void fillEntries(String searchString) {
            this.clearEntries();
            ArrayList<Map.Entry<Identifier, DebugHudEntry>> list = new ArrayList<Map.Entry<Identifier, DebugHudEntry>>(DebugHudEntries.getEntries().entrySet());
            list.sort(ENTRY_COMPARATOR);
            DebugHudEntryCategory lv = null;
            for (Map.Entry entry : list) {
                if (!((Identifier)entry.getKey()).getPath().contains(searchString)) continue;
                DebugHudEntryCategory lv2 = ((DebugHudEntry)entry.getValue()).getCategory();
                if (!lv2.equals(lv)) {
                    this.addEntry(new Category(lv2.label()));
                    lv = lv2;
                }
                this.addEntry(new Entry((Identifier)entry.getKey()));
            }
            this.refreshScreen();
        }

        private void refreshScreen() {
            this.refreshScroll();
            DebugOptionsScreen.this.narrateScreenIfNarrationEnabled(true);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class Entry
    extends AbstractEntry {
        private final Identifier label;
        protected final List<ClickableWidget> widgets = Lists.newArrayList();
        private final CyclingButtonWidget<Boolean> alwaysOnButton;
        private final CyclingButtonWidget<Boolean> inF3Button;
        private final CyclingButtonWidget<Boolean> neverButton;
        private final String renderedLabel;
        private final boolean canShow;

        public Entry(Identifier label) {
            this.label = label;
            DebugHudEntry lv = DebugHudEntries.get(label);
            this.canShow = lv != null && lv.canShow(DebugOptionsScreen.this.client.hasReducedDebugInfo());
            String string = label.getPath();
            this.renderedLabel = this.canShow ? string : String.valueOf(Formatting.ITALIC) + string;
            this.alwaysOnButton = CyclingButtonWidget.onOffBuilder(ALWAYS_ON_TEXT.copy().withColor(Colors.LIGHT_RED), ALWAYS_ON_TEXT.copy().withColor(Colors.ALTERNATE_WHITE)).omitKeyText().narration(this::getNarrationMessage).build(10, 5, 44, 16, Text.literal(string), (button, value) -> this.setEntryVisibility(label, DebugHudEntryVisibility.ALWAYS_ON));
            this.inF3Button = CyclingButtonWidget.onOffBuilder(IN_F3_TEXT.copy().withColor(Colors.LIGHT_YELLOW), IN_F3_TEXT.copy().withColor(Colors.ALTERNATE_WHITE)).omitKeyText().narration(this::getNarrationMessage).build(10, 5, 44, 16, Text.literal(string), (button, value) -> this.setEntryVisibility(label, DebugHudEntryVisibility.IN_F3));
            this.neverButton = CyclingButtonWidget.onOffBuilder(NEVER_TEXT.copy().withColor(Colors.WHITE), NEVER_TEXT.copy().withColor(Colors.ALTERNATE_WHITE)).omitKeyText().narration(this::getNarrationMessage).build(10, 5, 44, 16, Text.literal(string), (button, value) -> this.setEntryVisibility(label, DebugHudEntryVisibility.NEVER));
            this.widgets.add(this.neverButton);
            this.widgets.add(this.inF3Button);
            this.widgets.add(this.alwaysOnButton);
            this.init();
        }

        private MutableText getNarrationMessage(CyclingButtonWidget<Boolean> widget) {
            DebugHudEntryVisibility lv = ((DebugOptionsScreen)DebugOptionsScreen.this).client.debugHudEntryList.getVisibility(this.label);
            MutableText lv2 = Text.translatable("debug.entry.currently." + lv.asString(), this.renderedLabel);
            return ScreenTexts.composeGenericOptionText(lv2, widget.getMessage());
        }

        private void setEntryVisibility(Identifier label, DebugHudEntryVisibility visibility) {
            ((DebugOptionsScreen)DebugOptionsScreen.this).client.debugHudEntryList.setEntryVisibility(label, visibility);
            for (ButtonWidget lv : DebugOptionsScreen.this.profileButtons) {
                lv.active = true;
            }
            this.init();
        }

        @Override
        public List<? extends Element> children() {
            return this.widgets;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.widgets;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int k = this.getContentX();
            int l = this.getContentY();
            context.drawTextWithShadow(((DebugOptionsScreen)DebugOptionsScreen.this).client.textRenderer, this.renderedLabel, k, l + 5, this.canShow ? Colors.WHITE : Colors.GRAY);
            int m = k + this.getContentWidth() - this.neverButton.getWidth() - this.inF3Button.getWidth() - this.alwaysOnButton.getWidth();
            if (!this.canShow && hovered && mouseX < m) {
                context.drawTooltip(NOT_ALLOWED_TEXT, mouseX, mouseY);
            }
            this.neverButton.setX(m);
            this.inF3Button.setX(this.neverButton.getX() + this.neverButton.getWidth());
            this.alwaysOnButton.setX(this.inF3Button.getX() + this.inF3Button.getWidth());
            this.alwaysOnButton.setY(l);
            this.inF3Button.setY(l);
            this.neverButton.setY(l);
            this.alwaysOnButton.render(context, mouseX, mouseY, deltaTicks);
            this.inF3Button.render(context, mouseX, mouseY, deltaTicks);
            this.neverButton.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public void init() {
            DebugHudEntryVisibility lv = ((DebugOptionsScreen)DebugOptionsScreen.this).client.debugHudEntryList.getVisibility(this.label);
            this.alwaysOnButton.setValue(lv == DebugHudEntryVisibility.ALWAYS_ON);
            this.inF3Button.setValue(lv == DebugHudEntryVisibility.IN_F3);
            this.neverButton.setValue(lv == DebugHudEntryVisibility.NEVER);
            this.alwaysOnButton.active = this.alwaysOnButton.getValue() == false;
            this.inF3Button.active = this.inF3Button.getValue() == false;
            this.neverButton.active = this.neverButton.getValue() == false;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class Category
    extends AbstractEntry {
        final Text label;

        public Category(Text label) {
            this.label = label;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.drawCenteredTextWithShadow(((DebugOptionsScreen)DebugOptionsScreen.this).client.textRenderer, this.label, this.getContentX() + this.getContentWidth() / 2, this.getContentY() + 5, Colors.WHITE);
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(new Selectable(){

                @Override
                public Selectable.SelectionType getType() {
                    return Selectable.SelectionType.HOVERED;
                }

                @Override
                public void appendNarrations(NarrationMessageBuilder builder) {
                    builder.put(NarrationPart.TITLE, Category.this.label);
                }
            });
        }

        @Override
        public void init() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class AbstractEntry
    extends ElementListWidget.Entry<AbstractEntry> {
        public abstract void init();
    }
}

