/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public record NbtPredicate(NbtCompound nbt) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<NbtPredicate> CODEC = StringNbtReader.NBT_COMPOUND_CODEC.xmap(NbtPredicate::new, NbtPredicate::nbt);
    public static final PacketCodec<ByteBuf, NbtPredicate> PACKET_CODEC = PacketCodecs.NBT_COMPOUND.xmap(NbtPredicate::new, NbtPredicate::nbt);
    public static final String SELECTED_ITEM_KEY = "SelectedItem";

    public boolean test(ComponentsAccess components) {
        NbtComponent lv = components.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        return lv.matches(this.nbt);
    }

    public boolean test(Entity entity) {
        return this.test(NbtPredicate.entityToNbt(entity));
    }

    public boolean test(@Nullable NbtElement element) {
        return element != null && NbtHelper.matches(this.nbt, element, true);
    }

    public static NbtCompound entityToNbt(Entity entity) {
        try (ErrorReporter.Logging lv = new ErrorReporter.Logging(entity.getErrorReporterContext(), LOGGER);){
            PlayerEntity lv3;
            ItemStack lv4;
            NbtWriteView lv2 = NbtWriteView.create(lv, entity.getRegistryManager());
            entity.writeData(lv2);
            if (entity instanceof PlayerEntity && !(lv4 = (lv3 = (PlayerEntity)entity).getInventory().getSelectedStack()).isEmpty()) {
                lv2.put(SELECTED_ITEM_KEY, ItemStack.CODEC, lv4);
            }
            NbtCompound nbtCompound = lv2.getNbt();
            return nbtCompound;
        }
    }
}

