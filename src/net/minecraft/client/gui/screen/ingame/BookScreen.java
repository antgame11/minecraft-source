/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import java.lang.runtime.SwitchBootstraps;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BookScreen
extends Screen {
    public static final int field_32328 = 16;
    public static final int field_32329 = 36;
    public static final int field_32330 = 30;
    private static final int field_52807 = 256;
    private static final int field_52808 = 256;
    private static final Text TITLE_TEXT = Text.translatable("book.view.title");
    public static final Contents EMPTY_PROVIDER = new Contents(List.of());
    public static final Identifier BOOK_TEXTURE = Identifier.ofVanilla("textures/gui/book.png");
    protected static final int MAX_TEXT_WIDTH = 114;
    protected static final int MAX_TEXT_HEIGHT = 128;
    protected static final int WIDTH = 192;
    protected static final int HEIGHT = 192;
    private Contents contents;
    private int pageIndex;
    private List<OrderedText> cachedPage = Collections.emptyList();
    private int cachedPageIndex = -1;
    private Text pageIndexText = ScreenTexts.EMPTY;
    private PageTurnWidget nextPageButton;
    private PageTurnWidget previousPageButton;
    private final boolean pageTurnSound;

    public BookScreen(Contents pageProvider) {
        this(pageProvider, true);
    }

    public BookScreen() {
        this(EMPTY_PROVIDER, false);
    }

    private BookScreen(Contents contents, boolean playPageTurnSound) {
        super(TITLE_TEXT);
        this.contents = contents;
        this.pageTurnSound = playPageTurnSound;
    }

    public void setPageProvider(Contents pageProvider) {
        this.contents = pageProvider;
        this.pageIndex = MathHelper.clamp(this.pageIndex, 0, pageProvider.getPageCount());
        this.updatePageButtons();
        this.cachedPageIndex = -1;
    }

    public boolean setPage(int index) {
        int j = MathHelper.clamp(index, 0, this.contents.getPageCount() - 1);
        if (j != this.pageIndex) {
            this.pageIndex = j;
            this.updatePageButtons();
            this.cachedPageIndex = -1;
            return true;
        }
        return false;
    }

    protected boolean jumpToPage(int page) {
        return this.setPage(page);
    }

    @Override
    protected void init() {
        this.addCloseButton();
        this.addPageButtons();
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinLines(super.getNarratedTitle(), this.getPageIndicatorText(), this.contents.getPage(this.pageIndex));
    }

    private Text getPageIndicatorText() {
        return Text.translatable("book.pageIndicator", this.pageIndex + 1, Math.max(this.getPageCount(), 1));
    }

    protected void addCloseButton() {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(this.width / 2 - 100, 196, 200, 20).build());
    }

    protected void addPageButtons() {
        int i = (this.width - 192) / 2;
        int j = 2;
        this.nextPageButton = this.addDrawableChild(new PageTurnWidget(i + 116, 159, true, button -> this.goToNextPage(), this.pageTurnSound));
        this.previousPageButton = this.addDrawableChild(new PageTurnWidget(i + 43, 159, false, button -> this.goToPreviousPage(), this.pageTurnSound));
        this.updatePageButtons();
    }

    private int getPageCount() {
        return this.contents.getPageCount();
    }

    protected void goToPreviousPage() {
        if (this.pageIndex > 0) {
            --this.pageIndex;
        }
        this.updatePageButtons();
    }

    protected void goToNextPage() {
        if (this.pageIndex < this.getPageCount() - 1) {
            ++this.pageIndex;
        }
        this.updatePageButtons();
    }

    private void updatePageButtons() {
        this.nextPageButton.visible = this.pageIndex < this.getPageCount() - 1;
        this.previousPageButton.visible = this.pageIndex > 0;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (super.keyPressed(input)) {
            return true;
        }
        switch (input.key()) {
            case 266: {
                this.previousPageButton.onPress(input);
                return true;
            }
            case 267: {
                this.nextPageButton.onPress(input);
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        int k = (this.width - 192) / 2;
        int l = 2;
        if (this.cachedPageIndex != this.pageIndex) {
            Text lv = this.contents.getPage(this.pageIndex);
            this.cachedPage = this.textRenderer.wrapLines(lv, 114);
            this.pageIndexText = this.getPageIndicatorText();
        }
        this.cachedPageIndex = this.pageIndex;
        int m = this.textRenderer.getWidth(this.pageIndexText);
        context.drawText(this.textRenderer, this.pageIndexText, k - m + 192 - 44, 18, Colors.BLACK, false);
        int n = Math.min(128 / this.textRenderer.fontHeight, this.cachedPage.size());
        for (int o = 0; o < n; ++o) {
            OrderedText lv2 = this.cachedPage.get(o);
            context.drawText(this.textRenderer, lv2, k + 36, 32 + o * this.textRenderer.fontHeight, -16777216, false);
        }
        Style lv3 = this.getTextStyleAt(mouseX, mouseY);
        if (lv3 != null) {
            context.drawHoverEvent(this.textRenderer, lv3, mouseX, mouseY);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BOOK_TEXTURE, (this.width - 192) / 2, 2, 0.0f, 0.0f, 192, 192, 256, 256);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        Style lv;
        if (click.button() == 0 && (lv = this.getTextStyleAt(click.x(), click.y())) != null && this.handleTextClick(lv)) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected void handleClickEvent(MinecraftClient client, ClickEvent clickEvent) {
        ClientPlayerEntity lv = Objects.requireNonNull(client.player, "Player not available");
        ClickEvent clickEvent2 = clickEvent;
        Objects.requireNonNull(clickEvent2);
        ClickEvent clickEvent3 = clickEvent2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.ChangePage.class, ClickEvent.RunCommand.class}, (Object)clickEvent3, n)) {
            case 0: {
                ClickEvent.ChangePage changePage = (ClickEvent.ChangePage)clickEvent3;
                try {
                    int n2;
                    int i = n2 = changePage.page();
                    this.jumpToPage(i - 1);
                    return;
                } catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 1: {
                String string2;
                ClickEvent.RunCommand runCommand = (ClickEvent.RunCommand)clickEvent3;
                {
                    String string;
                    string2 = string = runCommand.command();
                    this.closeScreen();
                }
                BookScreen.handleRunCommand(lv, string2, null);
                return;
            }
        }
        BookScreen.handleClickEvent(clickEvent, client, this);
    }

    protected void closeScreen() {
    }

    @Override
    public boolean deferSubtitles() {
        return true;
    }

    @Nullable
    public Style getTextStyleAt(double x, double y) {
        if (this.cachedPage.isEmpty()) {
            return null;
        }
        int i = MathHelper.floor(x - (double)((this.width - 192) / 2) - 36.0);
        int j = MathHelper.floor(y - 2.0 - 30.0);
        if (i < 0 || j < 0) {
            return null;
        }
        int k = Math.min(128 / this.textRenderer.fontHeight, this.cachedPage.size());
        if (i <= 114 && j < this.client.textRenderer.fontHeight * k + k) {
            int l = j / this.client.textRenderer.fontHeight;
            if (l >= 0 && l < this.cachedPage.size()) {
                OrderedText lv = this.cachedPage.get(l);
                return this.client.textRenderer.getTextHandler().getStyleAt(lv, i);
            }
            return null;
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public record Contents(List<Text> pages) {
        public int getPageCount() {
            return this.pages.size();
        }

        public Text getPage(int index) {
            if (index >= 0 && index < this.getPageCount()) {
                return this.pages.get(index);
            }
            return ScreenTexts.EMPTY;
        }

        @Nullable
        public static Contents create(ItemStack stack) {
            boolean bl = MinecraftClient.getInstance().shouldFilterText();
            WrittenBookContentComponent lv = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
            if (lv != null) {
                return new Contents(lv.getPages(bl));
            }
            WritableBookContentComponent lv2 = stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
            if (lv2 != null) {
                return new Contents(lv2.stream(bl).map(Text::literal).toList());
            }
            return null;
        }
    }
}

