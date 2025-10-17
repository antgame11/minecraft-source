/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.LazyRegistryEntryReference;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public record JukeboxPlayableComponent(LazyRegistryEntryReference<JukeboxSong> song) implements TooltipAppender
{
    public static final Codec<JukeboxPlayableComponent> CODEC = LazyRegistryEntryReference.createCodec(RegistryKeys.JUKEBOX_SONG, JukeboxSong.ENTRY_CODEC).xmap(JukeboxPlayableComponent::new, JukeboxPlayableComponent::song);
    public static final PacketCodec<RegistryByteBuf, JukeboxPlayableComponent> PACKET_CODEC = PacketCodec.tuple(LazyRegistryEntryReference.createPacketCodec(RegistryKeys.JUKEBOX_SONG, JukeboxSong.ENTRY_PACKET_CODEC), JukeboxPlayableComponent::song, JukeboxPlayableComponent::new);

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        RegistryWrapper.WrapperLookup lv = context.getRegistryLookup();
        if (lv != null) {
            this.song.resolveEntry(lv).ifPresent(entry -> {
                MutableText lv = ((JukeboxSong)entry.value()).description().copy();
                Texts.setStyleIfAbsent(lv, Style.EMPTY.withColor(Formatting.GRAY));
                textConsumer.accept(lv);
            });
        }
    }

    public static ActionResult tryPlayStack(World world, BlockPos pos, ItemStack stack, PlayerEntity player) {
        JukeboxPlayableComponent lv = stack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
        if (lv == null) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        BlockState lv2 = world.getBlockState(pos);
        if (!lv2.isOf(Blocks.JUKEBOX) || lv2.get(JukeboxBlock.HAS_RECORD).booleanValue()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!world.isClient()) {
            ItemStack lv3 = stack.splitUnlessCreative(1, player);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof JukeboxBlockEntity) {
                JukeboxBlockEntity lv4 = (JukeboxBlockEntity)blockEntity;
                lv4.setStack(lv3);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, lv2));
            }
            player.incrementStat(Stats.PLAY_RECORD);
        }
        return ActionResult.SUCCESS;
    }
}

