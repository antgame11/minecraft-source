/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChangeGameModeC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

@Environment(value=EnvType.CLIENT)
public class GameModeSwitcherScreen
extends Screen {
    static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("gamemode_switcher/slot");
    static final Identifier SELECTION_TEXTURE = Identifier.ofVanilla("gamemode_switcher/selection");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/gamemode_switcher.png");
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final int BUTTON_SIZE = 26;
    private static final int ICON_OFFSET = 5;
    private static final int field_32314 = 31;
    private static final int field_32315 = 5;
    private static final int UI_WIDTH = GameModeSelection.values().length * 31 - 5;
    private static final Text SELECT_NEXT_TEXT = Text.translatable("debug.gamemodes.select_next", Text.translatable("debug.gamemodes.press_f4").formatted(Formatting.AQUA));
    private final GameModeSelection currentGameMode;
    private GameModeSelection gameMode;
    private int lastMouseX;
    private int lastMouseY;
    private boolean mouseUsedForSelection;
    private final List<ButtonWidget> gameModeButtons = Lists.newArrayList();

    public GameModeSwitcherScreen() {
        super(NarratorManager.EMPTY);
        this.gameMode = this.currentGameMode = GameModeSelection.of(this.getPreviousGameMode());
    }

    private GameMode getPreviousGameMode() {
        ClientPlayerInteractionManager lv = MinecraftClient.getInstance().interactionManager;
        GameMode lv2 = lv.getPreviousGameMode();
        if (lv2 != null) {
            return lv2;
        }
        return lv.getCurrentGameMode() == GameMode.CREATIVE ? GameMode.SURVIVAL : GameMode.CREATIVE;
    }

    @Override
    protected void init() {
        super.init();
        this.gameModeButtons.clear();
        this.gameMode = this.currentGameMode;
        for (int i = 0; i < GameModeSelection.VALUES.length; ++i) {
            GameModeSelection lv = GameModeSelection.VALUES[i];
            this.gameModeButtons.add(new ButtonWidget(lv, this.width / 2 - UI_WIDTH / 2 + i * 31, this.height / 2 - 31));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.checkForClose()) {
            return;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, this.gameMode.text, this.width / 2, this.height / 2 - 31 - 20, Colors.WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, SELECT_NEXT_TEXT, this.width / 2, this.height / 2 + 5, Colors.WHITE);
        if (!this.mouseUsedForSelection) {
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            this.mouseUsedForSelection = true;
        }
        boolean bl = this.lastMouseX == mouseX && this.lastMouseY == mouseY;
        for (ButtonWidget lv : this.gameModeButtons) {
            lv.render(context, mouseX, mouseY, deltaTicks);
            lv.setSelected(this.gameMode == lv.gameMode);
            if (bl || !lv.isSelected()) continue;
            this.gameMode = lv.gameMode;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int k = this.width / 2 - 62;
        int l = this.height / 2 - 31 - 27;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, k, l, 0.0f, 0.0f, 125, 75, 128, 128);
    }

    private void apply() {
        GameModeSwitcherScreen.apply(this.client, this.gameMode);
    }

    private static void apply(MinecraftClient client, GameModeSelection gameModeSelection) {
        if (!client.canSwitchGameMode()) {
            return;
        }
        GameModeSelection lv = GameModeSelection.of(client.interactionManager.getCurrentGameMode());
        if (client.player.hasPermissionLevel(2) && gameModeSelection != lv) {
            client.player.networkHandler.sendPacket(new ChangeGameModeC2SPacket(gameModeSelection.gameMode));
        }
    }

    private boolean checkForClose() {
        if (!InputUtil.isKeyPressed(this.client.getWindow(), InputUtil.GLFW_KEY_F3)) {
            this.apply();
            this.client.setScreen(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == InputUtil.GLFW_KEY_F4) {
            this.mouseUsedForSelection = false;
            this.gameMode = this.gameMode.next();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    static enum GameModeSelection {
        CREATIVE(Text.translatable("gameMode.creative"), GameMode.CREATIVE, new ItemStack(Blocks.GRASS_BLOCK)),
        SURVIVAL(Text.translatable("gameMode.survival"), GameMode.SURVIVAL, new ItemStack(Items.IRON_SWORD)),
        ADVENTURE(Text.translatable("gameMode.adventure"), GameMode.ADVENTURE, new ItemStack(Items.MAP)),
        SPECTATOR(Text.translatable("gameMode.spectator"), GameMode.SPECTATOR, new ItemStack(Items.ENDER_EYE));

        static final GameModeSelection[] VALUES;
        private static final int field_32317 = 16;
        private static final int field_32316 = 5;
        final Text text;
        final GameMode gameMode;
        private final ItemStack icon;

        private GameModeSelection(Text text, GameMode gameMode, ItemStack icon) {
            this.text = text;
            this.gameMode = gameMode;
            this.icon = icon;
        }

        void renderIcon(DrawContext context, int x, int y) {
            context.drawItem(this.icon, x, y);
        }

        GameModeSelection next() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> SURVIVAL;
                case 1 -> ADVENTURE;
                case 2 -> SPECTATOR;
                case 3 -> CREATIVE;
            };
        }

        static GameModeSelection of(GameMode gameMode) {
            return switch (gameMode) {
                default -> throw new MatchException(null, null);
                case GameMode.SPECTATOR -> SPECTATOR;
                case GameMode.SURVIVAL -> SURVIVAL;
                case GameMode.CREATIVE -> CREATIVE;
                case GameMode.ADVENTURE -> ADVENTURE;
            };
        }

        static {
            VALUES = GameModeSelection.values();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ButtonWidget
    extends ClickableWidget {
        final GameModeSelection gameMode;
        private boolean selected;

        public ButtonWidget(GameModeSelection gameMode, int x, int y) {
            super(x, y, 26, 26, gameMode.text);
            this.gameMode = gameMode;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            this.drawBackground(context);
            if (this.selected) {
                this.drawSelectionBox(context);
            }
            this.gameMode.renderIcon(context, this.getX() + 5, this.getY() + 5);
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }

        @Override
        public boolean isSelected() {
            return super.isSelected() || this.selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        private void drawBackground(DrawContext context) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, this.getX(), this.getY(), 26, 26);
        }

        private void drawSelectionBox(DrawContext context) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SELECTION_TEXTURE, this.getX(), this.getY(), 26, 26);
        }
    }
}

