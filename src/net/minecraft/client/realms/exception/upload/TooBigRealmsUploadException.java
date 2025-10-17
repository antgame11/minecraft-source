/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.exception.upload;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.SizeUnit;
import net.minecraft.client.realms.exception.RealmsUploadException;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class TooBigRealmsUploadException
extends RealmsUploadException {
    final long maxSizeInBytes;

    public TooBigRealmsUploadException(long maxSizeInBytes) {
        this.maxSizeInBytes = maxSizeInBytes;
    }

    @Override
    public Text[] getStatusTexts() {
        return new Text[]{Text.translatable("mco.upload.failed.too_big.title"), Text.translatable("mco.upload.failed.too_big.description", SizeUnit.humanReadableSize(this.maxSizeInBytes, SizeUnit.getLargestUnit(this.maxSizeInBytes)))};
    }
}

