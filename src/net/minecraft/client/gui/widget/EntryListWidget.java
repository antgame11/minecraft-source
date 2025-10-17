/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class EntryListWidget<E extends Entry<E>>
extends ContainerWidget {
    private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");
    private static final int SPACER_HEIGHT = 2;
    protected final MinecraftClient client;
    protected final int itemHeight;
    private final List<E> children = new Entries();
    protected boolean centerListVertically = true;
    @Nullable
    private E selected;
    @Nullable
    private E hoveredEntry;

    public EntryListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(0, y, width, height, ScreenTexts.EMPTY);
        this.client = client;
        this.itemHeight = itemHeight;
    }

    @Nullable
    public E getSelectedOrNull() {
        return this.selected;
    }

    public void setSelected(@Nullable E entry) {
        this.selected = entry;
        if (entry != null) {
            boolean bl2;
            boolean bl = ((Entry)entry).getContentY() < this.getY();
            boolean bl3 = bl2 = ((Entry)entry).getContentBottomEnd() > this.getBottom();
            if (this.client.getNavigationType().isKeyboard() || bl || bl2) {
                this.scrollTo(entry);
            }
        }
    }

    @Nullable
    public E getFocused() {
        return (E)((Entry)super.getFocused());
    }

    public final List<E> children() {
        return Collections.unmodifiableList(this.children);
    }

    protected void sort(Comparator<E> comparator) {
        this.children.sort(comparator);
        this.recalculateAllChildrenPositions();
    }

    protected void swapEntriesOnPositions(int pos1, int pos2) {
        Collections.swap(this.children, pos1, pos2);
        this.recalculateAllChildrenPositions();
        this.scrollTo((Entry)this.children.get(pos2));
    }

    protected void clearEntries() {
        this.children.clear();
        this.selected = null;
    }

    protected void clearEntriesExcept(E entryToKeep) {
        this.children.removeIf(entry -> entry != entryToKeep);
        if (this.selected != entryToKeep) {
            this.setSelected(null);
        }
    }

    public void replaceEntries(Collection<E> collection) {
        this.clearEntries();
        for (Entry lv : collection) {
            this.addEntry(lv);
        }
    }

    private int getYOfFirstEntry() {
        return this.getY() + 2;
    }

    public int getYOfNextEntry() {
        int i = this.getYOfFirstEntry() - (int)this.getScrollY();
        for (Entry lv : this.children) {
            i += lv.getHeight();
        }
        return i;
    }

    protected int addEntry(E entry) {
        return this.addEntry(entry, this.itemHeight);
    }

    protected int addEntry(E entry, int entryHeight) {
        ((Entry)entry).setX(this.getRowLeft());
        ((Entry)entry).setWidth(this.getRowWidth());
        ((Entry)entry).setY(this.getYOfNextEntry());
        ((Entry)entry).setHeight(entryHeight);
        this.children.add(entry);
        return this.children.size() - 1;
    }

    protected void addEntryToTop(E entry) {
        this.addEntryToTop(entry, this.itemHeight);
    }

    protected void addEntryToTop(E entry, int entryHeight) {
        double d = (double)this.getMaxScrollY() - this.getScrollY();
        ((Entry)entry).setHeight(entryHeight);
        this.children.addFirst(entry);
        this.recalculateAllChildrenPositions();
        this.setScrollY((double)this.getMaxScrollY() - d);
    }

    private void recalculateAllChildrenPositions() {
        int i = this.getYOfFirstEntry() - (int)this.getScrollY();
        for (Entry lv : this.children) {
            lv.setY(i);
            i += lv.getHeight();
            lv.setX(this.getRowLeft());
            lv.setWidth(this.getRowWidth());
        }
    }

    protected void removeEntryWithoutScrolling(E entry) {
        double d = (double)this.getMaxScrollY() - this.getScrollY();
        this.removeEntry(entry);
        this.setScrollY((double)this.getMaxScrollY() - d);
    }

    protected int getEntryCount() {
        return this.children().size();
    }

    protected boolean isEntrySelectionAllowed() {
        return true;
    }

    @Nullable
    protected final E getEntryAtPosition(double x, double y) {
        for (Entry lv : this.children) {
            if (!lv.isMouseOver(x, y)) continue;
            return (E)lv;
        }
        return null;
    }

    public void position(int width, ThreePartsLayoutWidget layout) {
        this.position(width, layout.getContentHeight(), layout.getHeaderHeight());
    }

    public void position(int width, int height, int y) {
        this.position(width, height, 0, y);
    }

    public void position(int width, int height, int x, int y) {
        this.setDimensions(width, height);
        this.setPosition(x, y);
        this.recalculateAllChildrenPositions();
        if (this.getSelectedOrNull() != null) {
            this.scrollTo(this.getSelectedOrNull());
        }
        this.refreshScroll();
    }

    @Override
    protected int getContentsHeightWithPadding() {
        int i = 0;
        for (Entry lv : this.children) {
            i += lv.getHeight();
        }
        return i + 4;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.hoveredEntry = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;
        this.drawMenuListBackground(context);
        this.enableScissor(context);
        this.renderList(context, mouseX, mouseY, deltaTicks);
        context.disableScissor();
        this.drawHeaderAndFooterSeparators(context);
        this.drawScrollbar(context, mouseX, mouseY);
    }

    protected void drawHeaderAndFooterSeparators(DrawContext context) {
        Identifier lv = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier lv2 = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, lv, this.getX(), this.getY() - 2, 0.0f, 0.0f, this.getWidth(), 2, 32, 2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, lv2, this.getX(), this.getBottom(), 0.0f, 0.0f, this.getWidth(), 2, 32, 2);
    }

    protected void drawMenuListBackground(DrawContext context) {
        Identifier lv = this.client.world == null ? MENU_LIST_BACKGROUND_TEXTURE : INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, lv, this.getX(), this.getY(), this.getRight(), this.getBottom() + (int)this.getScrollY(), this.getWidth(), this.getHeight(), 32, 32);
    }

    protected void enableScissor(DrawContext context) {
        context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
    }

    protected void scrollTo(E entry) {
        int j;
        int i = ((Entry)entry).getY() - this.getY() - 2;
        if (i < 0) {
            this.scroll(i);
        }
        if ((j = this.getBottom() - ((Entry)entry).getY() - ((Entry)entry).getHeight() - 2) < 0) {
            this.scroll(-j);
        }
    }

    protected void centerScrollOn(E entry) {
        int i = 0;
        for (Entry lv : this.children) {
            if (lv == entry) {
                i += lv.getHeight() / 2;
                break;
            }
            i += lv.getHeight();
        }
        this.setScrollY((double)i - (double)this.height / 2.0);
    }

    private void scroll(int amount) {
        this.setScrollY(this.getScrollY() + (double)amount);
    }

    @Override
    public void setScrollY(double scrollY) {
        super.setScrollY(scrollY);
        this.recalculateAllChildrenPositions();
    }

    @Override
    protected double getDeltaYPerScroll() {
        return (double)this.itemHeight / 2.0;
    }

    @Override
    protected int getScrollbarX() {
        return this.getRowRight() + 6 + 2;
    }

    @Override
    public Optional<Element> hoveredElement(double mouseX, double mouseY) {
        return Optional.ofNullable(this.getEntryAtPosition(mouseX, mouseY));
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.setFocused(null);
        }
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        Element lv = this.getFocused();
        if (lv != focused && lv instanceof ParentElement) {
            ParentElement lv2 = (ParentElement)lv;
            lv2.setFocused(null);
        }
        super.setFocused(focused);
        int i = this.children.indexOf(focused);
        if (i >= 0) {
            Entry lv3 = (Entry)this.children.get(i);
            this.setSelected(lv3);
        }
    }

    @Nullable
    protected E getNeighboringEntry(NavigationDirection direction) {
        return (E)this.getNeighboringEntry(direction, entry -> true);
    }

    @Nullable
    protected E getNeighboringEntry(NavigationDirection direction, Predicate<E> predicate) {
        return this.getNeighboringEntry(direction, predicate, this.getSelectedOrNull());
    }

    @Nullable
    protected E getNeighboringEntry(NavigationDirection direction, Predicate<E> predicate, @Nullable E selected) {
        int i;
        switch (direction) {
            default: {
                throw new MatchException(null, null);
            }
            case RIGHT: 
            case LEFT: {
                int n = 0;
                break;
            }
            case UP: {
                int n = -1;
                break;
            }
            case DOWN: {
                int n = i = 1;
            }
        }
        if (!this.children().isEmpty() && i != 0) {
            int j = selected == null ? (i > 0 ? 0 : this.children().size() - 1) : this.children().indexOf(selected) + i;
            for (int k = j; k >= 0 && k < this.children.size(); k += i) {
                Entry lv = (Entry)this.children().get(k);
                if (!predicate.test(lv)) continue;
                return (E)lv;
            }
        }
        return null;
    }

    protected void renderList(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        for (Entry lv : this.children) {
            if (lv.getY() + lv.getHeight() < this.getY() || lv.getY() > this.getBottom()) continue;
            this.renderEntry(context, mouseX, mouseY, deltaTicks, lv);
        }
    }

    protected void renderEntry(DrawContext context, int mouseX, int mouseY, float delta, E entry) {
        if (this.isEntrySelectionAllowed() && this.getSelectedOrNull() == entry) {
            int k = this.isFocused() ? -1 : -8355712;
            this.drawSelectionHighlight(context, entry, k);
        }
        ((Entry)entry).render(context, mouseX, mouseY, Objects.equals(this.hoveredEntry, entry), delta);
    }

    protected void drawSelectionHighlight(DrawContext context, E entry, int color) {
        int j = ((Entry)entry).getX();
        int k = ((Entry)entry).getY();
        int l = j + ((Entry)entry).getWidth();
        int m = k + ((Entry)entry).getHeight();
        context.fill(j, k, l, m, color);
        context.fill(j + 1, k + 1, l - 1, m - 1, Colors.BLACK);
    }

    public int getRowLeft() {
        return this.getX() + this.width / 2 - this.getRowWidth() / 2;
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
    }

    public int getRowTop(int index) {
        return ((Entry)this.children.get(index)).getY();
    }

    public int getRowBottom(int index) {
        Entry lv = (Entry)this.children.get(index);
        return lv.getY() + lv.getHeight();
    }

    public int getRowWidth() {
        return 220;
    }

    @Override
    public Selectable.SelectionType getType() {
        if (this.isFocused()) {
            return Selectable.SelectionType.FOCUSED;
        }
        if (this.hoveredEntry != null) {
            return Selectable.SelectionType.HOVERED;
        }
        return Selectable.SelectionType.NONE;
    }

    protected void removeEntries(List<E> entries) {
        entries.forEach(this::removeEntry);
    }

    protected void removeEntry(E entry) {
        boolean bl = this.children.remove(entry);
        if (bl) {
            this.recalculateAllChildrenPositions();
            if (entry == this.getSelectedOrNull()) {
                this.setSelected(null);
            }
        }
    }

    @Nullable
    protected E getHoveredEntry() {
        return this.hoveredEntry;
    }

    void setEntryParentList(Entry<E> entry) {
        entry.parentList = this;
    }

    protected void appendNarrations(NarrationMessageBuilder builder, E entry) {
        int i;
        List<E> list = this.children();
        if (list.size() > 1 && (i = list.indexOf(entry)) != -1) {
            builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.list", i + 1, list.size()));
        }
    }

    @Override
    @Nullable
    public /* synthetic */ Element getFocused() {
        return this.getFocused();
    }

    @Environment(value=EnvType.CLIENT)
    class Entries
    extends AbstractList<E> {
        private final List<E> entries = Lists.newArrayList();

        Entries() {
        }

        @Override
        public E get(int i) {
            return (Entry)this.entries.get(i);
        }

        @Override
        public int size() {
            return this.entries.size();
        }

        @Override
        public E set(int i, E arg) {
            Entry lv = (Entry)this.entries.set(i, arg);
            EntryListWidget.this.setEntryParentList(arg);
            return lv;
        }

        @Override
        public void add(int i, E arg) {
            this.entries.add(i, arg);
            EntryListWidget.this.setEntryParentList(arg);
        }

        @Override
        public E remove(int i) {
            return (Entry)this.entries.remove(i);
        }

        @Override
        public /* synthetic */ Object remove(int index) {
            return this.remove(index);
        }

        @Override
        public /* synthetic */ void add(int index, Object entry) {
            this.add(index, (E)((Entry)entry));
        }

        @Override
        public /* synthetic */ Object set(int index, Object entry) {
            return this.set(index, (E)((Entry)entry));
        }

        @Override
        public /* synthetic */ Object get(int index) {
            return this.get(index);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static abstract class Entry<E extends Entry<E>>
    implements Element,
    Widget {
        public static final int PADDING = 2;
        private int x = 0;
        private int y = 0;
        private int width = 0;
        private int height;
        @Deprecated
        EntryListWidget<E> parentList;

        protected Entry() {
        }

        @Override
        public void setFocused(boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return this.parentList.getFocused() == this;
        }

        public abstract void render(DrawContext var1, int var2, int var3, boolean var4, float var5);

        @Override
        public boolean isMouseOver(double d, double e) {
            return this.getNavigationFocus().contains((int)d, (int)e);
        }

        @Override
        public void setX(int i) {
            this.x = i;
        }

        @Override
        public void setY(int y) {
            this.y = y;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getContentX() {
            return this.getX() + 2;
        }

        public int getContentY() {
            return this.getY() + 2;
        }

        public int getContentHeight() {
            return this.getHeight() - 4;
        }

        public int getContentMiddleY() {
            return this.getContentY() + this.getContentHeight() / 2;
        }

        public int getContentBottomEnd() {
            return this.getContentY() + this.getContentHeight();
        }

        public int getContentWidth() {
            return this.getWidth() - 4;
        }

        public int getContentMiddleX() {
            return this.getContentX() + this.getContentWidth() / 2;
        }

        public int getContentRightEnd() {
            return this.getContentX() + this.getContentWidth();
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        @Override
        public void forEachChild(Consumer<ClickableWidget> consumer) {
        }

        @Override
        public ScreenRect getNavigationFocus() {
            return Widget.super.getNavigationFocus();
        }
    }
}

