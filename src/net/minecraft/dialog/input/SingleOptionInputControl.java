/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog.input;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.dialog.input.InputControl;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.dynamic.Codecs;

public record SingleOptionInputControl(int width, List<Entry> entries, Text label, boolean labelVisible) implements InputControl
{
    public static final MapCodec<SingleOptionInputControl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Dialog.WIDTH_CODEC.optionalFieldOf("width", 200).forGetter(SingleOptionInputControl::width), ((MapCodec)Codecs.nonEmptyList(Entry.CODEC.listOf()).fieldOf("options")).forGetter(SingleOptionInputControl::entries), ((MapCodec)TextCodecs.CODEC.fieldOf("label")).forGetter(SingleOptionInputControl::label), Codec.BOOL.optionalFieldOf("label_visible", true).forGetter(SingleOptionInputControl::labelVisible)).apply((Applicative<SingleOptionInputControl, ?>)instance, SingleOptionInputControl::new)).validate(inputControl -> {
        long l = inputControl.entries.stream().filter(Entry::initial).count();
        if (l > 1L) {
            return DataResult.error(() -> "Multiple initial values");
        }
        return DataResult.success(inputControl);
    });

    public MapCodec<SingleOptionInputControl> getCodec() {
        return CODEC;
    }

    public Optional<Entry> getInitialEntry() {
        return this.entries.stream().filter(Entry::initial).findFirst();
    }

    public record Entry(String id, Optional<Text> display, boolean initial) {
        public static final Codec<Entry> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("id")).forGetter(Entry::id), TextCodecs.CODEC.optionalFieldOf("display").forGetter(Entry::display), Codec.BOOL.optionalFieldOf("initial", false).forGetter(Entry::initial)).apply((Applicative<Entry, ?>)instance, Entry::new));
        public static final Codec<Entry> CODEC = Codec.withAlternative(BASE_CODEC, Codec.STRING, id -> new Entry((String)id, Optional.empty(), false));

        public Text getDisplay() {
            return this.display.orElseGet(() -> Text.literal(this.id));
        }
    }
}

