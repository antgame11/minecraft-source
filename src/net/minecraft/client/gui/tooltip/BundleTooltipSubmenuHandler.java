/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import net.minecraft.client.input.Scroller;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.joml.Vector2i;

@Environment(value=EnvType.CLIENT)
public class BundleTooltipSubmenuHandler
implements TooltipSubmenuHandler {
    private final MinecraftClient client;
    private final Scroller scroller;

    public BundleTooltipSubmenuHandler(MinecraftClient client) {
        this.client = client;
        this.scroller = new Scroller();
    }

    @Override
    public boolean isApplicableTo(Slot slot) {
        return slot.getStack().isIn(ItemTags.BUNDLES);
    }

    @Override
    public boolean onScroll(double horizontal, double vertical, int slotId, ItemStack item) {
        int m;
        int l;
        int k;
        int j = BundleItem.getNumberOfStacksShown(item);
        if (j == 0) {
            return false;
        }
        Vector2i vector2i = this.scroller.update(horizontal, vertical);
        int n = k = vector2i.y == 0 ? -vector2i.x : vector2i.y;
        if (k != 0 && (l = BundleItem.getSelectedStackIndex(item)) != (m = Scroller.scrollCycling(k, l, j))) {
            this.sendPacket(item, slotId, m);
        }
        return true;
    }

    @Override
    public void reset(Slot slot) {
        this.reset(slot.getStack(), slot.id);
    }

    @Override
    public void onMouseClick(Slot slot, SlotActionType actionType) {
        if (actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.SWAP) {
            this.reset(slot.getStack(), slot.id);
        }
    }

    private void sendPacket(ItemStack item, int slotId, int selectedItemIndex) {
        if (this.client.getNetworkHandler() != null && selectedItemIndex < BundleItem.getNumberOfStacksShown(item)) {
            ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
            BundleItem.setSelectedStackIndex(item, selectedItemIndex);
            lv.sendPacket(new BundleItemSelectedC2SPacket(slotId, selectedItemIndex));
        }
    }

    public void reset(ItemStack item, int slotId) {
        this.sendPacket(item, slotId, -1);
    }
}

