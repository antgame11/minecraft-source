/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class HorseScreen
extends HandledScreen<HorseScreenHandler> {
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    private static final Identifier CHEST_SLOTS_TEXTURE = Identifier.ofVanilla("container/horse/chest_slots");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/horse.png");
    private final AbstractHorseEntity entity;
    private final int slotColumnCount;
    private float mouseX;
    private float mouseY;

    public HorseScreen(HorseScreenHandler handler, PlayerInventory inventory, AbstractHorseEntity entity, int slotColumnCount) {
        super(handler, inventory, entity.getDisplayName());
        this.entity = entity;
        this.slotColumnCount = slotColumnCount;
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int k = (this.width - this.backgroundWidth) / 2;
        int l = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, k, l, 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 256, 256);
        if (this.slotColumnCount > 0) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, CHEST_SLOTS_TEXTURE, 90, 54, 0, 0, k + 79, l + 17, this.slotColumnCount * 18, 54);
        }
        if (this.entity.canUseSlot(EquipmentSlot.SADDLE) && this.entity.getType().isIn(EntityTypeTags.CAN_EQUIP_SADDLE)) {
            this.drawSlot(context, k + 7, l + 35 - 18);
        }
        boolean bl = this.entity instanceof LlamaEntity;
        if (this.entity.canUseSlot(EquipmentSlot.BODY) && (this.entity.getType().isIn(EntityTypeTags.CAN_WEAR_HORSE_ARMOR) || bl)) {
            this.drawSlot(context, k + 7, l + 35);
        }
        InventoryScreen.drawEntity(context, k + 26, l + 18, k + 78, l + 70, 17, 0.25f, this.mouseX, this.mouseY, (LivingEntity)this.entity);
    }

    private void drawSlot(DrawContext context, int x, int y) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, x, y, 18, 18);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

