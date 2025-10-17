/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.render.block.entity.AbstractEndPortalBlockEntityRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CreditsScreen
extends Screen {
    private static final Identifier VIGNETTE_TEXTURE = Identifier.ofVanilla("textures/misc/credits_vignette.png");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text SEPARATOR_LINE = Text.literal("============").formatted(Formatting.WHITE);
    private static final String CENTERED_LINE_PREFIX = "           ";
    private static final String OBFUSCATION_PLACEHOLDER = String.valueOf(Formatting.WHITE) + String.valueOf(Formatting.OBFUSCATED) + String.valueOf(Formatting.GREEN) + String.valueOf(Formatting.AQUA);
    private static final float SPACE_BAR_SPEED_MULTIPLIER = 5.0f;
    private static final float CTRL_KEY_SPEED_MULTIPLIER = 15.0f;
    private static final Identifier END_POEM_TEXT_LOCATION = Identifier.ofVanilla("texts/end.txt");
    private static final Identifier CREDITS_TEXT_LOCATION = Identifier.ofVanilla("texts/credits.json");
    private static final Identifier POST_CREDITS_TEXT_LOCATION = Identifier.ofVanilla("texts/postcredits.txt");
    private final boolean endCredits;
    private final Runnable finishAction;
    private float time;
    private List<OrderedText> credits;
    private List<Text> narratedCredits;
    private IntSet centeredLines;
    private int creditsHeight;
    private boolean spaceKeyPressed;
    private final IntSet pressedCtrlKeys = new IntOpenHashSet();
    private float speed;
    private final float baseSpeed;
    private int speedMultiplier;
    private final LogoDrawer logoDrawer = new LogoDrawer(false);

    public CreditsScreen(boolean endCredits, Runnable finishAction) {
        super(NarratorManager.EMPTY);
        this.endCredits = endCredits;
        this.finishAction = finishAction;
        this.baseSpeed = !endCredits ? 0.75f : 0.5f;
        this.speedMultiplier = 1;
        this.speed = this.baseSpeed;
    }

    private float getSpeed() {
        if (this.spaceKeyPressed) {
            return this.baseSpeed * (5.0f + (float)this.pressedCtrlKeys.size() * 15.0f) * (float)this.speedMultiplier;
        }
        return this.baseSpeed * (float)this.speedMultiplier;
    }

    @Override
    public void tick() {
        this.client.getMusicTracker().tick();
        this.client.getSoundManager().tick(false);
        float f = this.creditsHeight + this.height + this.height + 24;
        if (this.time > f) {
            this.closeScreen();
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isUp()) {
            this.speedMultiplier = -1;
        } else if (input.key() == InputUtil.GLFW_KEY_LEFT_CONTROL || input.key() == InputUtil.GLFW_KEY_RIGHT_CONTROL) {
            this.pressedCtrlKeys.add(input.key());
        } else if (input.key() == InputUtil.GLFW_KEY_SPACE) {
            this.spaceKeyPressed = true;
        }
        this.speed = this.getSpeed();
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        if (input.isUp()) {
            this.speedMultiplier = 1;
        }
        if (input.key() == InputUtil.GLFW_KEY_SPACE) {
            this.spaceKeyPressed = false;
        } else if (input.key() == InputUtil.GLFW_KEY_LEFT_CONTROL || input.key() == InputUtil.GLFW_KEY_RIGHT_CONTROL) {
            this.pressedCtrlKeys.remove(input.key());
        }
        this.speed = this.getSpeed();
        return super.keyReleased(input);
    }

    @Override
    public void close() {
        this.closeScreen();
    }

    private void closeScreen() {
        this.finishAction.run();
    }

    @Override
    protected void init() {
        if (this.credits != null) {
            return;
        }
        this.credits = Lists.newArrayList();
        this.narratedCredits = Lists.newArrayList();
        this.centeredLines = new IntOpenHashSet();
        if (this.endCredits) {
            this.load(END_POEM_TEXT_LOCATION, this::readPoem);
        }
        this.load(CREDITS_TEXT_LOCATION, this::readCredits);
        if (this.endCredits) {
            this.load(POST_CREDITS_TEXT_LOCATION, this::readPoem);
        }
        this.creditsHeight = this.credits.size() * 12;
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences((Text[])this.narratedCredits.toArray(Text[]::new));
    }

    private void load(Identifier fileLocation, CreditsReader reader) {
        try (BufferedReader reader2 = this.client.getResourceManager().openAsReader(fileLocation);){
            reader.read(reader2);
        } catch (Exception exception) {
            LOGGER.error("Couldn't load credits from file {}", (Object)fileLocation, (Object)exception);
        }
    }

    private void readPoem(Reader reader) throws IOException {
        int i;
        Object string;
        BufferedReader bufferedReader = new BufferedReader(reader);
        Random lv = Random.create(8124371L);
        while ((string = bufferedReader.readLine()) != null) {
            string = ((String)string).replaceAll("PLAYERNAME", this.client.getSession().getUsername());
            while ((i = ((String)string).indexOf(OBFUSCATION_PLACEHOLDER)) != -1) {
                String string2 = ((String)string).substring(0, i);
                String string3 = ((String)string).substring(i + OBFUSCATION_PLACEHOLDER.length());
                string = string2 + String.valueOf(Formatting.WHITE) + String.valueOf(Formatting.OBFUSCATED) + "XXXXXXXX".substring(0, lv.nextInt(4) + 3) + string3;
            }
            this.addText((String)string);
            this.addEmptyLine();
        }
        for (i = 0; i < 8; ++i) {
            this.addEmptyLine();
        }
    }

    private void readCredits(Reader reader) {
        JsonArray jsonArray = JsonHelper.deserializeArray(reader);
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String string = jsonObject.get("section").getAsString();
            this.addText(SEPARATOR_LINE, true, false);
            this.addText(Text.literal(string).formatted(Formatting.YELLOW), true, true);
            this.addText(SEPARATOR_LINE, true, false);
            this.addEmptyLine();
            this.addEmptyLine();
            JsonArray jsonArray2 = jsonObject.getAsJsonArray("disciplines");
            for (JsonElement jsonElement2 : jsonArray2) {
                JsonObject jsonObject2 = jsonElement2.getAsJsonObject();
                String string2 = jsonObject2.get("discipline").getAsString();
                if (StringUtils.isNotEmpty(string2)) {
                    this.addText(Text.literal(string2).formatted(Formatting.YELLOW), true, true);
                    this.addEmptyLine();
                    this.addEmptyLine();
                }
                JsonArray jsonArray3 = jsonObject2.getAsJsonArray("titles");
                for (JsonElement jsonElement3 : jsonArray3) {
                    JsonObject jsonObject3 = jsonElement3.getAsJsonObject();
                    String string3 = jsonObject3.get("title").getAsString();
                    JsonArray jsonArray4 = jsonObject3.getAsJsonArray("names");
                    this.addText(Text.literal(string3).formatted(Formatting.GRAY), false, true);
                    for (JsonElement jsonElement4 : jsonArray4) {
                        String string4 = jsonElement4.getAsString();
                        this.addText(Text.literal(CENTERED_LINE_PREFIX).append(string4).formatted(Formatting.WHITE), false, true);
                    }
                    this.addEmptyLine();
                    this.addEmptyLine();
                }
            }
        }
    }

    private void addEmptyLine() {
        this.credits.add(OrderedText.EMPTY);
        this.narratedCredits.add(ScreenTexts.EMPTY);
    }

    private void addText(String text) {
        MutableText lv = Text.literal(text);
        this.credits.addAll(this.client.textRenderer.wrapLines(lv, 256));
        this.narratedCredits.add(lv);
    }

    private void addText(Text text, boolean centered, boolean narrate) {
        if (centered) {
            this.centeredLines.add(this.credits.size());
        }
        this.credits.add(text.asOrderedText());
        if (narrate) {
            this.narratedCredits.add(text);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.renderVignette(context);
        this.time = Math.max(0.0f, this.time + deltaTicks * this.speed);
        int k = this.width / 2 - 128;
        int l = this.height + 50;
        float g = -this.time;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0.0f, g);
        context.createNewRootLayer();
        this.logoDrawer.draw(context, this.width, 1.0f, l);
        int m = l + 100;
        for (int n = 0; n < this.credits.size(); ++n) {
            float h;
            if (n == this.credits.size() - 1 && (h = (float)m + g - (float)(this.height / 2 - 6)) < 0.0f) {
                context.getMatrices().translate(0.0f, -h);
            }
            if ((float)m + g + 12.0f + 8.0f > 0.0f && (float)m + g < (float)this.height) {
                OrderedText lv = this.credits.get(n);
                if (this.centeredLines.contains(n)) {
                    context.drawCenteredTextWithShadow(this.textRenderer, lv, k + 128, m, Colors.WHITE);
                } else {
                    context.drawTextWithShadow(this.textRenderer, lv, k, m, -1);
                }
            }
            m += 12;
        }
        context.getMatrices().popMatrix();
    }

    private void renderVignette(DrawContext context) {
        context.drawTexture(RenderPipelines.VIGNETTE, VIGNETTE_TEXTURE, 0, 0, 0.0f, 0.0f, this.width, this.height, this.width, this.height);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.endCredits) {
            TextureManager lv = MinecraftClient.getInstance().getTextureManager();
            TextureSetup lv2 = TextureSetup.of(lv.getTexture(AbstractEndPortalBlockEntityRenderer.SKY_TEXTURE).getGlTextureView(), lv.getTexture(AbstractEndPortalBlockEntityRenderer.PORTAL_TEXTURE).getGlTextureView());
            context.fill(RenderPipelines.END_PORTAL, lv2, 0, 0, this.width, this.height);
        } else {
            super.renderBackground(context, mouseX, mouseY, deltaTicks);
        }
    }

    @Override
    protected void renderDarkening(DrawContext context, int x, int y, int width, int height) {
        float f = this.time * 0.5f;
        Screen.renderBackgroundTexture(context, Screen.MENU_BACKGROUND_TEXTURE, 0, 0, 0.0f, f, width, height);
    }

    @Override
    public boolean shouldPause() {
        return !this.endCredits;
    }

    @Override
    public boolean keepOpenThroughPortal() {
        return true;
    }

    @Override
    public void removed() {
        this.client.getMusicTracker().stop(MusicType.CREDITS);
    }

    @Override
    public MusicSound getMusic() {
        return MusicType.CREDITS;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface CreditsReader {
        public void read(Reader var1) throws IOException;
    }
}

