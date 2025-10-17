/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsSlot;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.util.RealmsTextureManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsWorldSlotButton
extends ButtonWidget {
    private static final Identifier SLOT_FRAME = Identifier.ofVanilla("widget/slot_frame");
    public static final Identifier EMPTY_FRAME = Identifier.ofVanilla("textures/gui/realms/empty_frame.png");
    public static final Identifier PANORAMA_0 = Identifier.ofVanilla("textures/gui/title/background/panorama_0.png");
    public static final Identifier PANORAMA_2 = Identifier.ofVanilla("textures/gui/title/background/panorama_2.png");
    public static final Identifier PANORAMA_3 = Identifier.ofVanilla("textures/gui/title/background/panorama_3.png");
    private static final Text MINIGAME_TOOLTIP = Text.translatable("mco.configure.world.slot.tooltip.minigame");
    private static final Text TOOLTIP = Text.translatable("mco.configure.world.slot.tooltip");
    static final Text MINIGAME_SLOT_NAME = Text.translatable("mco.worldSlot.minigame");
    private static final int MAX_DISPLAYED_SLOT_NAME_LENGTH = 64;
    private static final String ELLIPSIS = "...";
    private final int slotIndex;
    private State state;

    public RealmsWorldSlotButton(int x, int y, int width, int height, int slotIndex, RealmsServer server, ButtonWidget.PressAction onPress) {
        super(x, y, width, height, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.slotIndex = slotIndex;
        this.state = this.setServer(server);
    }

    public State getState() {
        return this.state;
    }

    public State setServer(RealmsServer server) {
        this.state = new State(server, this.slotIndex);
        this.updateTooltip(this.state, server.minigameName);
        return this.state;
    }

    private void updateTooltip(State state, @Nullable String minigameName) {
        Text lv;
        switch (state.action.ordinal()) {
            case 1: {
                Text text;
                if (state.minigame) {
                    text = MINIGAME_TOOLTIP;
                    break;
                }
                text = TOOLTIP;
                break;
            }
            default: {
                Text text = lv = null;
            }
        }
        if (lv != null) {
            this.setTooltip(Tooltip.of(lv));
        }
        MutableText lv2 = Text.literal(state.slotName);
        if (state.minigame && minigameName != null) {
            lv2 = lv2.append(ScreenTexts.SPACE).append(minigameName);
        }
        this.setMessage(lv2);
    }

    static Action getAction(boolean bl, boolean active, boolean bl3) {
        if (!(bl || active && bl3)) {
            return Action.SWITCH_SLOT;
        }
        return Action.NOTHING;
    }

    @Override
    public boolean isInteractable() {
        return this.state.action != Action.NOTHING && super.isInteractable();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Object string;
        TextRenderer lv2;
        int k = this.getX();
        int l = this.getY();
        boolean bl = this.isSelected();
        Identifier lv = this.state.minigame ? RealmsTextureManager.getTextureId(String.valueOf(this.state.imageId), this.state.image) : (this.state.empty ? EMPTY_FRAME : (this.state.image != null && this.state.imageId != -1L ? RealmsTextureManager.getTextureId(String.valueOf(this.state.imageId), this.state.image) : (this.slotIndex == 1 ? PANORAMA_0 : (this.slotIndex == 2 ? PANORAMA_2 : (this.slotIndex == 3 ? PANORAMA_3 : EMPTY_FRAME)))));
        int m = Colors.WHITE;
        if (!this.state.active) {
            m = ColorHelper.fromFloats(1.0f, 0.56f, 0.56f, 0.56f);
        }
        context.drawTexture(RenderPipelines.GUI_TEXTURED, lv, k + 1, l + 1, 0.0f, 0.0f, this.width - 2, this.height - 2, 74, 74, 74, 74, m);
        if (bl && this.state.action != Action.NOTHING) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_FRAME, k, l, this.width, this.height);
        } else if (this.state.active) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_FRAME, k, l, this.width, this.height, ColorHelper.fromFloats(1.0f, 0.8f, 0.8f, 0.8f));
        } else {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_FRAME, k, l, this.width, this.height, ColorHelper.fromFloats(1.0f, 0.56f, 0.56f, 0.56f));
        }
        if (this.state.hardcore) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, RealmsMainScreen.HARDCORE_ICON_TEXTURE, k + 3, l + 4, 9, 8);
        }
        if ((lv2 = MinecraftClient.getInstance().textRenderer).getWidth((String)(string = this.state.slotName)) > 64) {
            string = lv2.trimToWidth((String)string, 64 - lv2.getWidth(ELLIPSIS)) + ELLIPSIS;
        }
        context.drawCenteredTextWithShadow(lv2, (String)string, k + this.width / 2, l + this.height - 14, Colors.WHITE);
        if (this.state.active) {
            context.drawCenteredTextWithShadow(lv2, RealmsMainScreen.getVersionText(this.state.version, this.state.compatibility.isCompatible()), k + this.width / 2, l + this.height + 2, Colors.WHITE);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class State {
        final String slotName;
        final String version;
        final RealmsServer.Compatibility compatibility;
        final long imageId;
        @Nullable
        final String image;
        public final boolean empty;
        public final boolean minigame;
        public final Action action;
        public final boolean hardcore;
        public final boolean active;

        public State(RealmsServer server, int slot) {
            boolean bl = this.minigame = slot == 4;
            if (this.minigame) {
                this.slotName = MINIGAME_SLOT_NAME.getString();
                this.imageId = server.minigameId;
                this.image = server.minigameImage;
                this.empty = server.minigameId == -1;
                this.version = "";
                this.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
                this.hardcore = false;
                this.active = server.isMinigame();
            } else {
                RealmsSlot lv = server.slots.get(slot);
                this.slotName = lv.options.getSlotName(slot);
                this.imageId = lv.options.templateId;
                this.image = lv.options.templateImage;
                this.empty = lv.options.empty;
                this.version = lv.options.version;
                this.compatibility = lv.options.compatibility;
                this.hardcore = lv.isHardcore();
                this.active = server.activeSlot == slot && !server.isMinigame();
            }
            this.action = RealmsWorldSlotButton.getAction(this.active, this.empty, server.expired);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Action {
        NOTHING,
        SWITCH_SLOT;

    }
}

