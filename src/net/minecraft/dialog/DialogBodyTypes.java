/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog;

import com.mojang.serialization.MapCodec;
import net.minecraft.dialog.body.DialogBody;
import net.minecraft.dialog.body.ItemDialogBody;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DialogBodyTypes {
    public static MapCodec<? extends DialogBody> registerAndGetDefault(Registry<MapCodec<? extends DialogBody>> registry) {
        Registry.register(registry, Identifier.ofVanilla("item"), ItemDialogBody.CODEC);
        return Registry.register(registry, Identifier.ofVanilla("plain_message"), PlainMessageDialogBody.CODEC);
    }
}

