/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ColorHelper;

public record DyedColorComponent(int rgb) implements TooltipAppender
{
    public static final Codec<DyedColorComponent> CODEC = Codecs.RGB.xmap(DyedColorComponent::new, DyedColorComponent::rgb);
    public static final PacketCodec<ByteBuf, DyedColorComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, DyedColorComponent::rgb, DyedColorComponent::new);
    public static final int DEFAULT_COLOR = -6265536;

    public static int getColor(ItemStack stack, int defaultColor) {
        DyedColorComponent lv = stack.get(DataComponentTypes.DYED_COLOR);
        return lv != null ? ColorHelper.fullAlpha(lv.rgb()) : defaultColor;
    }

    public static ItemStack setColor(ItemStack stack, List<DyeItem> dyes) {
        int s;
        int p;
        int o;
        if (!stack.isIn(ItemTags.DYEABLE)) {
            return ItemStack.EMPTY;
        }
        ItemStack lv = stack.copyWithCount(1);
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        int m = 0;
        DyedColorComponent lv2 = lv.get(DataComponentTypes.DYED_COLOR);
        if (lv2 != null) {
            int n = ColorHelper.getRed(lv2.rgb());
            o = ColorHelper.getGreen(lv2.rgb());
            p = ColorHelper.getBlue(lv2.rgb());
            l += Math.max(n, Math.max(o, p));
            i += n;
            j += o;
            k += p;
            ++m;
        }
        for (DyeItem lv3 : dyes) {
            p = lv3.getColor().getEntityColor();
            int q = ColorHelper.getRed(p);
            int r = ColorHelper.getGreen(p);
            s = ColorHelper.getBlue(p);
            l += Math.max(q, Math.max(r, s));
            i += q;
            j += r;
            k += s;
            ++m;
        }
        int n = i / m;
        o = j / m;
        p = k / m;
        float f = (float)l / (float)m;
        float g = Math.max(n, Math.max(o, p));
        n = (int)((float)n * f / g);
        o = (int)((float)o * f / g);
        p = (int)((float)p * f / g);
        s = ColorHelper.getArgb(0, n, o, p);
        lv.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(s));
        return lv;
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        if (type.isAdvanced()) {
            textConsumer.accept(Text.translatable("item.color", String.format(Locale.ROOT, "#%06X", this.rgb)).formatted(Formatting.GRAY));
        } else {
            textConsumer.accept(Text.translatable("item.dyed").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
    }
}

