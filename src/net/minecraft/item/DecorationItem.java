/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class DecorationItem
extends Item {
    private static final Text RANDOM_TEXT = Text.translatable("painting.random").formatted(Formatting.GRAY);
    private final EntityType<? extends AbstractDecorationEntity> entityType;

    public DecorationItem(EntityType<? extends AbstractDecorationEntity> type, Item.Settings settings) {
        super(settings);
        this.entityType = type;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        AbstractDecorationEntity lv7;
        BlockPos lv = context.getBlockPos();
        Direction lv2 = context.getSide();
        BlockPos lv3 = lv.offset(lv2);
        PlayerEntity lv4 = context.getPlayer();
        ItemStack lv5 = context.getStack();
        if (lv4 != null && !this.canPlaceOn(lv4, lv2, lv5, lv3)) {
            return ActionResult.FAIL;
        }
        World lv6 = context.getWorld();
        if (this.entityType == EntityType.PAINTING) {
            Optional<PaintingEntity> optional = PaintingEntity.placePainting(lv6, lv3, lv2);
            if (optional.isEmpty()) {
                return ActionResult.CONSUME;
            }
            lv7 = optional.get();
        } else if (this.entityType == EntityType.ITEM_FRAME) {
            lv7 = new ItemFrameEntity(lv6, lv3, lv2);
        } else if (this.entityType == EntityType.GLOW_ITEM_FRAME) {
            lv7 = new GlowItemFrameEntity(lv6, lv3, lv2);
        } else {
            return ActionResult.SUCCESS;
        }
        EntityType.copier(lv6, lv5, lv4).accept(lv7);
        if (lv7.canStayAttached()) {
            if (!lv6.isClient()) {
                lv7.onPlace();
                lv6.emitGameEvent((Entity)lv4, GameEvent.ENTITY_PLACE, lv7.getEntityPos());
                lv6.spawnEntity(lv7);
            }
            lv5.decrement(1);
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }

    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        if (this.entityType == EntityType.PAINTING && displayComponent.shouldDisplay(DataComponentTypes.PAINTING_VARIANT)) {
            RegistryEntry<PaintingVariant> lv = stack.get(DataComponentTypes.PAINTING_VARIANT);
            if (lv != null) {
                lv.value().title().ifPresent(textConsumer);
                lv.value().author().ifPresent(textConsumer);
                textConsumer.accept(Text.translatable("painting.dimensions", lv.value().width(), lv.value().height()));
            } else if (type.isCreative()) {
                textConsumer.accept(RANDOM_TEXT);
            }
        }
    }
}

