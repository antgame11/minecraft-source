/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapPostProcessingComponent;
import net.minecraft.item.Item;
import net.minecraft.item.map.MapState;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record MapIdComponent(int id) implements TooltipAppender
{
    public static final Codec<MapIdComponent> CODEC = Codec.INT.xmap(MapIdComponent::new, MapIdComponent::id);
    public static final PacketCodec<ByteBuf, MapIdComponent> PACKET_CODEC = PacketCodecs.VAR_INT.xmap(MapIdComponent::new, MapIdComponent::id);
    private static final Text LOCKED_TOOLTIP_TEXT = Text.translatable("filled_map.locked").formatted(Formatting.GRAY);

    public String asString() {
        return "map_" + this.id;
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        MapState lv = context.getMapState(this);
        if (lv == null) {
            textConsumer.accept(Text.translatable("filled_map.unknown").formatted(Formatting.GRAY));
            return;
        }
        MapPostProcessingComponent lv2 = components.get(DataComponentTypes.MAP_POST_PROCESSING);
        if (components.get(DataComponentTypes.CUSTOM_NAME) == null && lv2 == null) {
            textConsumer.accept(Text.translatable("filled_map.id", this.id).formatted(Formatting.GRAY));
        }
        if (lv.locked || lv2 == MapPostProcessingComponent.LOCK) {
            textConsumer.accept(LOCKED_TOOLTIP_TEXT);
        }
        if (type.isAdvanced()) {
            byte i = lv2 == MapPostProcessingComponent.SCALE ? (byte)1 : 0;
            int j = Math.min(lv.scale + i, 4);
            textConsumer.accept(Text.translatable("filled_map.scale", 1 << j).formatted(Formatting.GRAY));
            textConsumer.accept(Text.translatable("filled_map.level", j, 4).formatted(Formatting.GRAY));
        }
    }
}

