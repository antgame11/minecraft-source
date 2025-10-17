/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.pack;

import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PackListWidget
extends AlwaysSelectedEntryListWidget<Entry> {
    static final Identifier SELECT_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/select_highlighted");
    static final Identifier SELECT_TEXTURE = Identifier.ofVanilla("transferable_list/select");
    static final Identifier UNSELECT_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/unselect_highlighted");
    static final Identifier UNSELECT_TEXTURE = Identifier.ofVanilla("transferable_list/unselect");
    static final Identifier MOVE_UP_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/move_up_highlighted");
    static final Identifier MOVE_UP_TEXTURE = Identifier.ofVanilla("transferable_list/move_up");
    static final Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/move_down_highlighted");
    static final Identifier MOVE_DOWN_TEXTURE = Identifier.ofVanilla("transferable_list/move_down");
    static final Text INCOMPATIBLE = Text.translatable("pack.incompatible");
    static final Text INCOMPATIBLE_CONFIRM = Text.translatable("pack.incompatible.confirm.title");
    private static final int field_62180 = 2;
    private final Text title;
    final PackScreen screen;

    public PackListWidget(MinecraftClient client, PackScreen screen, int width, int height, Text title) {
        super(client, width, height, 33, 36);
        this.screen = screen;
        this.title = title;
        this.centerListVertically = false;
    }

    @Override
    public int getRowWidth() {
        return this.width - 4;
    }

    @Override
    protected int getScrollbarX() {
        return this.getRight() - 6;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.getSelectedOrNull() != null) {
            return ((Entry)this.getSelectedOrNull()).keyPressed(input);
        }
        return super.keyPressed(input);
    }

    public void set(Stream<ResourcePackOrganizer.Pack> packs, @Nullable ResourcePackOrganizer.AbstractPack focused) {
        this.clearEntries();
        MutableText lv = Text.empty().append(this.title).formatted(Formatting.UNDERLINE, Formatting.BOLD);
        HeaderEntry headerEntry = new HeaderEntry(this, this.client.textRenderer, lv);
        Objects.requireNonNull(this.client.textRenderer);
        this.addEntry(headerEntry, (int)(9.0f * 1.5f));
        this.setSelected(null);
        packs.forEach(pack -> {
            ResourcePackEntry lv = new ResourcePackEntry(this.client, this, (ResourcePackOrganizer.Pack)pack);
            this.addEntry(lv);
            if (focused != null && focused.getName().equals(pack.getName())) {
                this.screen.setFocused(this);
                this.setFocused(lv);
            }
        });
    }

    @Environment(value=EnvType.CLIENT)
    public abstract class Entry
    extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        @Override
        public int getWidth() {
            return super.getWidth() - (PackListWidget.this.overflows() ? 6 : 0);
        }

        public abstract String getName();
    }

    @Environment(value=EnvType.CLIENT)
    public class HeaderEntry
    extends Entry {
        private final TextRenderer textRenderer;
        private final Text text;

        public HeaderEntry(PackListWidget arg, TextRenderer textRenderer, Text text) {
            this.textRenderer = textRenderer;
            this.text = text;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.drawCenteredTextWithShadow(this.textRenderer, this.text, this.getX() + this.getWidth() / 2, this.getContentMiddleY() - this.textRenderer.fontHeight / 2, Colors.WHITE);
        }

        @Override
        public Text getNarration() {
            return this.text;
        }

        @Override
        public String getName() {
            return "";
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class ResourcePackEntry
    extends Entry {
        private static final int field_32403 = 157;
        private static final int field_32404 = 157;
        private static final String ELLIPSIS = "...";
        private final PackListWidget widget;
        protected final MinecraftClient client;
        private final ResourcePackOrganizer.Pack pack;
        private final OrderedText displayName;
        private final MultilineText description;
        private final OrderedText incompatibleText;
        private final MultilineText compatibilityNotificationText;

        public ResourcePackEntry(MinecraftClient client, PackListWidget widget, ResourcePackOrganizer.Pack pack) {
            this.client = client;
            this.pack = pack;
            this.widget = widget;
            this.displayName = ResourcePackEntry.trimTextToWidth(client, pack.getDisplayName());
            this.description = ResourcePackEntry.createMultilineText(client, pack.getDecoratedDescription());
            this.incompatibleText = ResourcePackEntry.trimTextToWidth(client, INCOMPATIBLE);
            this.compatibilityNotificationText = ResourcePackEntry.createMultilineText(client, pack.getCompatibility().getNotification());
        }

        private static OrderedText trimTextToWidth(MinecraftClient client, Text text) {
            int i = client.textRenderer.getWidth(text);
            if (i > 157) {
                StringVisitable lv = StringVisitable.concat(client.textRenderer.trimToWidth(text, 157 - client.textRenderer.getWidth(ELLIPSIS)), StringVisitable.plain(ELLIPSIS));
                return Language.getInstance().reorder(lv);
            }
            return text.asOrderedText();
        }

        private static MultilineText createMultilineText(MinecraftClient client, Text text) {
            return MultilineText.create(client.textRenderer, 157, 2, text);
        }

        @Override
        public Text getNarration() {
            return Text.translatable("narrator.select", this.pack.getDisplayName());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int n;
            int m;
            ResourcePackCompatibility lv = this.pack.getCompatibility();
            if (!lv.isCompatible()) {
                int k = this.getContentX() - 1;
                int l = this.getContentY() - 1;
                m = this.getContentRightEnd() + 1;
                n = this.getContentBottomEnd() + 1;
                context.fill(k, l, m, n, -8978432);
            }
            context.drawTexture(RenderPipelines.GUI_TEXTURED, this.pack.getIconId(), this.getContentX(), this.getContentY(), 0.0f, 0.0f, 32, 32, 32, 32);
            OrderedText lv2 = this.displayName;
            MultilineText lv3 = this.description;
            if (this.isSelectable() && (this.client.options.getTouchscreen().getValue().booleanValue() || hovered || this.widget.getSelectedOrNull() == this && this.widget.isFocused())) {
                context.fill(this.getContentX(), this.getContentY(), this.getContentX() + 32, this.getContentY() + 32, -1601138544);
                m = mouseX - this.getContentX();
                n = mouseY - this.getContentY();
                if (!this.pack.getCompatibility().isCompatible()) {
                    lv2 = this.incompatibleText;
                    lv3 = this.compatibilityNotificationText;
                }
                if (this.pack.canBeEnabled()) {
                    if (m < 32) {
                        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SELECT_HIGHLIGHTED_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
                    } else {
                        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SELECT_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
                    }
                } else {
                    if (this.pack.canBeDisabled()) {
                        if (m < 16) {
                            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, UNSELECT_HIGHLIGHTED_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
                        } else {
                            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, UNSELECT_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
                        }
                    }
                    if (this.pack.canMoveTowardStart()) {
                        if (m < 32 && m > 16 && n < 16) {
                            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MOVE_UP_HIGHLIGHTED_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
                        } else {
                            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MOVE_UP_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
                        }
                    }
                    if (this.pack.canMoveTowardEnd()) {
                        if (m < 32 && m > 16 && n > 16) {
                            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_HIGHLIGHTED_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
                        } else {
                            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
                        }
                    }
                }
            }
            context.drawTextWithShadow(this.client.textRenderer, lv2, this.getContentX() + 32 + 2, this.getContentY() + 1, -1);
            lv3.draw(context, MultilineText.Alignment.LEFT, this.getContentX() + 32 + 2, this.getContentY() + 12, 10, true, -8355712);
        }

        @Override
        public String getName() {
            return this.pack.getName();
        }

        private boolean isSelectable() {
            return !this.pack.isPinned() || !this.pack.isAlwaysEnabled();
        }

        @Override
        public boolean keyPressed(KeyInput input) {
            if (input.isEnter()) {
                this.toggle();
                return true;
            }
            if (input.hasShift()) {
                if (input.isUp()) {
                    this.moveTowardStart();
                    return true;
                }
                if (input.isDown()) {
                    this.moveTowardEnd();
                    return true;
                }
            }
            return super.keyPressed(input);
        }

        public void toggle() {
            if (this.pack.canBeEnabled()) {
                this.enable();
            } else if (this.pack.canBeDisabled()) {
                this.pack.disable();
            }
        }

        private void moveTowardStart() {
            if (this.pack.canMoveTowardStart()) {
                this.pack.moveTowardStart();
            }
        }

        private void moveTowardEnd() {
            if (this.pack.canMoveTowardEnd()) {
                this.pack.moveTowardEnd();
            }
        }

        private void enable() {
            if (this.pack.getCompatibility().isCompatible()) {
                this.pack.enable();
            } else {
                Text lv = this.pack.getCompatibility().getConfirmMessage();
                this.client.setScreen(new ConfirmScreen(confirmed -> {
                    this.client.setScreen(this.widget.screen);
                    if (confirmed) {
                        this.pack.enable();
                    }
                }, INCOMPATIBLE_CONFIRM, lv));
            }
        }

        @Override
        public boolean isClickable() {
            return PackListWidget.this.children().stream().anyMatch(entry -> entry.getName().equals(this.getName()));
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            double d = click.x() - (double)this.getX();
            double e = click.y() - (double)this.getY();
            if (this.isSelectable() && d <= 32.0) {
                this.widget.screen.clearSelection();
                if (this.pack.canBeEnabled()) {
                    this.enable();
                    return true;
                }
                if (d < 16.0 && this.pack.canBeDisabled()) {
                    this.pack.disable();
                    return true;
                }
                if (d > 16.0 && e < 16.0 && this.pack.canMoveTowardStart()) {
                    this.pack.moveTowardStart();
                    return true;
                }
                if (d > 16.0 && e > 16.0 && this.pack.canMoveTowardEnd()) {
                    this.pack.moveTowardEnd();
                    return true;
                }
            }
            return super.mouseClicked(click, doubled);
        }
    }
}

