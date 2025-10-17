/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class EnchantmentScreen
extends HandledScreen<EnchantmentScreenHandler> {
    private static final Identifier[] LEVEL_TEXTURES = new Identifier[]{Identifier.ofVanilla("container/enchanting_table/level_1"), Identifier.ofVanilla("container/enchanting_table/level_2"), Identifier.ofVanilla("container/enchanting_table/level_3")};
    private static final Identifier[] LEVEL_DISABLED_TEXTURES = new Identifier[]{Identifier.ofVanilla("container/enchanting_table/level_1_disabled"), Identifier.ofVanilla("container/enchanting_table/level_2_disabled"), Identifier.ofVanilla("container/enchanting_table/level_3_disabled")};
    private static final Identifier ENCHANTMENT_SLOT_DISABLED_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot_disabled");
    private static final Identifier ENCHANTMENT_SLOT_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot_highlighted");
    private static final Identifier ENCHANTMENT_SLOT_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/enchanting_table.png");
    private static final Identifier BOOK_TEXTURE = Identifier.ofVanilla("textures/entity/enchanting_table_book.png");
    private final Random random = Random.create();
    private BookModel BOOK_MODEL;
    public float nextPageAngle;
    public float pageAngle;
    public float approximatePageAngle;
    public float pageRotationSpeed;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    private ItemStack stack = ItemStack.EMPTY;

    public EnchantmentScreen(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.BOOK_MODEL = new BookModel(this.client.getLoadedEntityModels().getModelPart(EntityModelLayers.BOOK));
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        this.client.player.experienceBarDisplayStartTime = this.client.player.age;
        this.doTick();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        for (int k = 0; k < 3; ++k) {
            double d = click.x() - (double)(i + 60);
            double e = click.y() - (double)(j + 14 + 19 * k);
            if (!(d >= 0.0) || !(e >= 0.0) || !(d < 108.0) || !(e < 19.0) || !((EnchantmentScreenHandler)this.handler).onButtonClick(this.client.player, k)) continue;
            this.client.interactionManager.clickButton(((EnchantmentScreenHandler)this.handler).syncId, k);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int k = (this.width - this.backgroundWidth) / 2;
        int l = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, k, l, 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 256, 256);
        this.drawBook(context, k, l);
        EnchantingPhrases.getInstance().setSeed(((EnchantmentScreenHandler)this.handler).getSeed());
        int m = ((EnchantmentScreenHandler)this.handler).getLapisCount();
        for (int n = 0; n < 3; ++n) {
            int o = k + 60;
            int p = o + 20;
            int q = ((EnchantmentScreenHandler)this.handler).enchantmentPower[n];
            if (q == 0) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_TEXTURE, o, l + 14 + 19 * n, 108, 19);
                continue;
            }
            String string = "" + q;
            int r = 86 - this.textRenderer.getWidth(string);
            StringVisitable lv = EnchantingPhrases.getInstance().generatePhrase(this.textRenderer, r);
            int s = -9937334;
            if (!(m >= n + 1 && this.client.player.experienceLevel >= q || this.client.player.isInCreativeMode())) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_TEXTURE, o, l + 14 + 19 * n, 108, 19);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, LEVEL_DISABLED_TEXTURES[n], o + 1, l + 15 + 19 * n, 16, 16);
                context.drawWrappedText(this.textRenderer, lv, p, l + 16 + 19 * n, r, ColorHelper.fullAlpha((s & 0xFEFEFE) >> 1), false);
                s = -12550384;
            } else {
                int t = mouseX - (k + 60);
                int u = mouseY - (l + 14 + 19 * n);
                if (t >= 0 && u >= 0 && t < 108 && u < 19) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_HIGHLIGHTED_TEXTURE, o, l + 14 + 19 * n, 108, 19);
                    s = -128;
                } else {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_TEXTURE, o, l + 14 + 19 * n, 108, 19);
                }
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, LEVEL_TEXTURES[n], o + 1, l + 15 + 19 * n, 16, 16);
                context.drawWrappedText(this.textRenderer, lv, p, l + 16 + 19 * n, r, s, false);
                s = -8323296;
            }
            context.drawTextWithShadow(this.textRenderer, string, p + 86 - this.textRenderer.getWidth(string), l + 16 + 19 * n + 7, s);
        }
    }

    private void drawBook(DrawContext context, int x, int y) {
        float f = this.client.getRenderTickCounter().getTickProgress(false);
        float g = MathHelper.lerp(f, this.pageTurningSpeed, this.nextPageTurningSpeed);
        float h = MathHelper.lerp(f, this.pageAngle, this.nextPageAngle);
        int k = x + 14;
        int l = y + 14;
        int m = k + 38;
        int n = l + 31;
        context.addBookModel(this.BOOK_MODEL, BOOK_TEXTURE, 40.0f, g, h, k, l, m, n);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float g = this.client.getRenderTickCounter().getTickProgress(false);
        super.render(context, mouseX, mouseY, g);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        boolean bl = this.client.player.isInCreativeMode();
        int k = ((EnchantmentScreenHandler)this.handler).getLapisCount();
        for (int l = 0; l < 3; ++l) {
            int m = ((EnchantmentScreenHandler)this.handler).enchantmentPower[l];
            Optional optional = this.client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(((EnchantmentScreenHandler)this.handler).enchantmentId[l]);
            if (optional.isEmpty()) continue;
            int n = ((EnchantmentScreenHandler)this.handler).enchantmentLevel[l];
            int o = l + 1;
            if (!this.isPointWithinBounds(60, 14 + 19 * l, 108, 17, mouseX, mouseY) || m <= 0 || n < 0 || optional == null) continue;
            ArrayList<Text> list = Lists.newArrayList();
            list.add(Text.translatable("container.enchant.clue", Enchantment.getName(optional.get(), n)).formatted(Formatting.WHITE));
            if (!bl) {
                list.add(ScreenTexts.EMPTY);
                if (this.client.player.experienceLevel < m) {
                    list.add(Text.translatable("container.enchant.level.requirement", ((EnchantmentScreenHandler)this.handler).enchantmentPower[l]).formatted(Formatting.RED));
                } else {
                    MutableText lv = o == 1 ? Text.translatable("container.enchant.lapis.one") : Text.translatable("container.enchant.lapis.many", o);
                    list.add(lv.formatted(k >= o ? Formatting.GRAY : Formatting.RED));
                    MutableText lv2 = o == 1 ? Text.translatable("container.enchant.level.one") : Text.translatable("container.enchant.level.many", o);
                    list.add(lv2.formatted(Formatting.GRAY));
                }
            }
            context.drawTooltip(this.textRenderer, list, mouseX, mouseY);
            break;
        }
    }

    public void doTick() {
        ItemStack lv = ((EnchantmentScreenHandler)this.handler).getSlot(0).getStack();
        if (!ItemStack.areEqual(lv, this.stack)) {
            this.stack = lv;
            do {
                this.approximatePageAngle += (float)(this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.nextPageAngle <= this.approximatePageAngle + 1.0f && this.nextPageAngle >= this.approximatePageAngle - 1.0f);
        }
        this.pageAngle = this.nextPageAngle;
        this.pageTurningSpeed = this.nextPageTurningSpeed;
        boolean bl = false;
        for (int i = 0; i < 3; ++i) {
            if (((EnchantmentScreenHandler)this.handler).enchantmentPower[i] == 0) continue;
            bl = true;
        }
        this.nextPageTurningSpeed = bl ? (this.nextPageTurningSpeed += 0.2f) : (this.nextPageTurningSpeed -= 0.2f);
        this.nextPageTurningSpeed = MathHelper.clamp(this.nextPageTurningSpeed, 0.0f, 1.0f);
        float f = (this.approximatePageAngle - this.nextPageAngle) * 0.4f;
        float g = 0.2f;
        f = MathHelper.clamp(f, -0.2f, 0.2f);
        this.pageRotationSpeed += (f - this.pageRotationSpeed) * 0.9f;
        this.nextPageAngle += this.pageRotationSpeed;
    }
}

