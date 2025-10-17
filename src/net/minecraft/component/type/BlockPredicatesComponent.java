/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BlockPredicatesComponent {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<BlockPredicatesComponent> CODEC = Codecs.listOrSingle(BlockPredicate.CODEC, Codecs.nonEmptyList(BlockPredicate.CODEC.listOf())).xmap(BlockPredicatesComponent::new, checker -> checker.predicates);
    public static final PacketCodec<RegistryByteBuf, BlockPredicatesComponent> PACKET_CODEC = PacketCodec.tuple(BlockPredicate.PACKET_CODEC.collect(PacketCodecs.toList()), blockPredicatesChecker -> blockPredicatesChecker.predicates, BlockPredicatesComponent::new);
    public static final Text CAN_BREAK_TEXT = Text.translatable("item.canBreak").formatted(Formatting.GRAY);
    public static final Text CAN_PLACE_TEXT = Text.translatable("item.canPlace").formatted(Formatting.GRAY);
    private static final Text CAN_USE_UNKNOWN_TEXT = Text.translatable("item.canUse.unknown").formatted(Formatting.GRAY);
    private final List<BlockPredicate> predicates;
    @Nullable
    private List<Text> tooltipText;
    @Nullable
    private CachedBlockPosition cachedPos;
    private boolean lastResult;
    private boolean nbtAware;

    public BlockPredicatesComponent(List<BlockPredicate> predicates) {
        this.predicates = predicates;
    }

    private static boolean canUseCache(CachedBlockPosition pos, @Nullable CachedBlockPosition cachedPos, boolean nbtAware) {
        if (cachedPos == null || pos.getBlockState() != cachedPos.getBlockState()) {
            return false;
        }
        if (!nbtAware) {
            return true;
        }
        if (pos.getBlockEntity() == null && cachedPos.getBlockEntity() == null) {
            return true;
        }
        if (pos.getBlockEntity() == null || cachedPos.getBlockEntity() == null) {
            return false;
        }
        try (ErrorReporter.Logging lv = new ErrorReporter.Logging(LOGGER);){
            DynamicRegistryManager lv2 = pos.getWorld().getRegistryManager();
            NbtCompound lv3 = BlockPredicatesComponent.getNbt(pos.getBlockEntity(), lv2, lv);
            NbtCompound lv4 = BlockPredicatesComponent.getNbt(cachedPos.getBlockEntity(), lv2, lv);
            boolean bl = Objects.equals(lv3, lv4);
            return bl;
        }
    }

    private static NbtCompound getNbt(BlockEntity blockEntity, DynamicRegistryManager registries, ErrorReporter errorReporter) {
        NbtWriteView lv = NbtWriteView.create(errorReporter.makeChild(blockEntity.getReporterContext()), registries);
        blockEntity.writeDataWithId(lv);
        return lv.getNbt();
    }

    public boolean check(CachedBlockPosition cachedPos) {
        if (BlockPredicatesComponent.canUseCache(cachedPos, this.cachedPos, this.nbtAware)) {
            return this.lastResult;
        }
        this.cachedPos = cachedPos;
        this.nbtAware = false;
        for (BlockPredicate lv : this.predicates) {
            if (!lv.test(cachedPos)) continue;
            this.nbtAware |= lv.hasNbt();
            this.lastResult = true;
            return true;
        }
        this.lastResult = false;
        return false;
    }

    private List<Text> getOrCreateTooltipText() {
        if (this.tooltipText == null) {
            this.tooltipText = BlockPredicatesComponent.createTooltipText(this.predicates);
        }
        return this.tooltipText;
    }

    public void addTooltips(Consumer<Text> adder) {
        this.getOrCreateTooltipText().forEach(adder);
    }

    private static List<Text> createTooltipText(List<BlockPredicate> blockPredicates) {
        for (BlockPredicate lv : blockPredicates) {
            if (!lv.blocks().isEmpty()) continue;
            return List.of(CAN_USE_UNKNOWN_TEXT);
        }
        return blockPredicates.stream().flatMap(predicate -> predicate.blocks().orElseThrow().stream()).distinct().map(block -> ((Block)block.value()).getName().formatted(Formatting.DARK_GRAY)).toList();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BlockPredicatesComponent) {
            BlockPredicatesComponent lv = (BlockPredicatesComponent)o;
            return this.predicates.equals(lv.predicates);
        }
        return false;
    }

    public int hashCode() {
        return this.predicates.hashCode();
    }

    public String toString() {
        return "AdventureModePredicate{predicates=" + String.valueOf(this.predicates) + "}";
    }
}

