/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.screen.ScreenTexts;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ScrollableLayoutWidget
implements LayoutWidget {
    private static final int field_60715 = 4;
    private static final int field_61059 = 10;
    final LayoutWidget layout;
    private final Container container;
    private int width;
    private int height;

    public ScrollableLayoutWidget(MinecraftClient client, LayoutWidget layout, int height) {
        this.layout = layout;
        this.container = new Container(client, 0, height);
    }

    public void setWidth(int width) {
        this.width = width;
        this.container.setWidth(Math.max(this.layout.getWidth(), width));
    }

    public void setHeight(int height) {
        this.height = height;
        this.container.setHeight(Math.min(this.layout.getHeight(), height));
        this.container.refreshScroll();
    }

    @Override
    public void refreshPositions() {
        this.layout.refreshPositions();
        int i = this.layout.getWidth();
        this.container.setWidth(Math.max(i + 20, this.width));
        this.container.setHeight(Math.min(this.layout.getHeight(), this.height));
        this.container.refreshScroll();
    }

    @Override
    public void forEachElement(Consumer<Widget> consumer) {
        consumer.accept(this.container);
    }

    @Override
    public void setX(int i) {
        this.container.setX(i);
    }

    @Override
    public void setY(int y) {
        this.container.setY(y);
    }

    @Override
    public int getX() {
        return this.container.getX();
    }

    @Override
    public int getY() {
        return this.container.getY();
    }

    @Override
    public int getWidth() {
        return this.container.getWidth();
    }

    @Override
    public int getHeight() {
        return this.container.getHeight();
    }

    @Environment(value=EnvType.CLIENT)
    class Container
    extends ContainerWidget {
        private final MinecraftClient client;
        private final List<ClickableWidget> children;

        public Container(MinecraftClient client, int width, int height) {
            super(0, 0, width, height, ScreenTexts.EMPTY);
            this.children = new ArrayList<ClickableWidget>();
            this.client = client;
            ScrollableLayoutWidget.this.layout.forEachChild(this.children::add);
        }

        @Override
        protected int getContentsHeightWithPadding() {
            return ScrollableLayoutWidget.this.layout.getHeight();
        }

        @Override
        protected double getDeltaYPerScroll() {
            return 10.0;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            context.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
            for (ClickableWidget lv : this.children) {
                lv.render(context, mouseX, mouseY, deltaTicks);
            }
            context.disableScissor();
            this.drawScrollbar(context, mouseX, mouseY);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }

        @Override
        public ScreenRect getBorder(NavigationDirection direction) {
            return new ScreenRect(this.getX(), this.getY(), this.width, this.getContentsHeightWithPadding());
        }

        @Override
        public void setFocused(@Nullable Element focused) {
            super.setFocused(focused);
            if (focused == null || !this.client.getNavigationType().isKeyboard()) {
                return;
            }
            ScreenRect lv = this.getNavigationFocus();
            ScreenRect lv2 = focused.getNavigationFocus();
            int i = lv2.getTop() - lv.getTop();
            int j = lv2.getBottom() - lv.getBottom();
            if (i < 0) {
                this.setScrollY(this.getScrollY() + (double)i - 14.0);
            } else if (j > 0) {
                this.setScrollY(this.getScrollY() + (double)j + 14.0);
            }
        }

        @Override
        public void setX(int i) {
            super.setX(i);
            ScrollableLayoutWidget.this.layout.setX(i + 10);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            ScrollableLayoutWidget.this.layout.setY(y - (int)this.getScrollY());
        }

        @Override
        public void setScrollY(double scrollY) {
            super.setScrollY(scrollY);
            ScrollableLayoutWidget.this.layout.setY(this.getNavigationFocus().getTop() - (int)this.getScrollY());
        }

        @Override
        public List<? extends Element> children() {
            return this.children;
        }

        @Override
        public Collection<? extends Selectable> getNarratedParts() {
            return this.children;
        }
    }
}

