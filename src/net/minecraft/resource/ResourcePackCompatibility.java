/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import net.minecraft.resource.PackVersion;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Range;

public enum ResourcePackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    UNKNOWN("unknown"),
    COMPATIBLE("compatible");

    public static final int field_61160 = Integer.MAX_VALUE;
    private final Text notification;
    private final Text confirmMessage;

    private ResourcePackCompatibility(String translationSuffix) {
        this.notification = Text.translatable("pack.incompatible." + translationSuffix).formatted(Formatting.GRAY);
        this.confirmMessage = Text.translatable("pack.incompatible.confirm." + translationSuffix);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static ResourcePackCompatibility from(Range<PackVersion> range, PackVersion arg2) {
        if (range.minInclusive().major() == Integer.MAX_VALUE) {
            return UNKNOWN;
        }
        if (range.maxInclusive().compareTo(arg2) < 0) {
            return TOO_OLD;
        }
        if (arg2.compareTo(range.minInclusive()) < 0) {
            return TOO_NEW;
        }
        return COMPATIBLE;
    }

    public Text getNotification() {
        return this.notification;
    }

    public Text getConfirmMessage() {
        return this.confirmMessage;
    }
}

