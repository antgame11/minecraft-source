/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public record BlockStateComponent(Map<String, String> properties) implements TooltipAppender
{
    public static final BlockStateComponent DEFAULT = new BlockStateComponent(Map.of());
    public static final Codec<BlockStateComponent> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(BlockStateComponent::new, BlockStateComponent::properties);
    private static final PacketCodec<ByteBuf, Map<String, String>> MAP_PACKET_CODEC = PacketCodecs.map(Object2ObjectOpenHashMap::new, PacketCodecs.STRING, PacketCodecs.STRING);
    public static final PacketCodec<ByteBuf, BlockStateComponent> PACKET_CODEC = MAP_PACKET_CODEC.xmap(BlockStateComponent::new, BlockStateComponent::properties);

    public <T extends Comparable<T>> BlockStateComponent with(Property<T> property, T value) {
        return new BlockStateComponent(Util.mapWith(this.properties, property.getName(), property.name(value)));
    }

    public <T extends Comparable<T>> BlockStateComponent with(Property<T> property, BlockState fromState) {
        return this.with(property, fromState.get(property));
    }

    @Nullable
    public <T extends Comparable<T>> T getValue(Property<T> property) {
        String string = this.properties.get(property.getName());
        if (string == null) {
            return null;
        }
        return (T)((Comparable)property.parse(string).orElse(null));
    }

    public BlockState applyToState(BlockState state) {
        StateManager<Block, BlockState> lv = state.getBlock().getStateManager();
        for (Map.Entry<String, String> entry : this.properties.entrySet()) {
            Property<?> lv2 = lv.getProperty(entry.getKey());
            if (lv2 == null) continue;
            state = BlockStateComponent.applyToState(state, lv2, entry.getValue());
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState applyToState(BlockState state, Property<T> property, String value) {
        return property.parse(value).map(valuex -> (BlockState)state.with(property, valuex)).orElse(state);
    }

    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        Integer integer = this.getValue(BeehiveBlock.HONEY_LEVEL);
        if (integer != null) {
            textConsumer.accept(Text.translatable("container.beehive.honey", integer, 5).formatted(Formatting.GRAY));
        }
    }
}

