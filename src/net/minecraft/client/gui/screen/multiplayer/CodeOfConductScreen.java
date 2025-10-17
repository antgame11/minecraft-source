/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.multiplayer;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CodeOfConductScreen
extends WarningScreen {
    private static final Text TITLE_TEXT = Text.translatable("multiplayer.codeOfConduct.title").formatted(Formatting.BOLD);
    private static final Text CHECK_TEXT = Text.translatable("multiplayer.codeOfConduct.check");
    @Nullable
    private final ServerInfo serverInfo;
    private final String rawCodeOfConduct;
    private final BooleanConsumer callback;
    private final Screen field_62585;

    private CodeOfConductScreen(@Nullable ServerInfo serverInfo, Screen arg2, Text arg3, String string, BooleanConsumer booleanConsumer) {
        super(TITLE_TEXT, arg3, CHECK_TEXT, TITLE_TEXT.copy().append("\n").append(arg3));
        this.serverInfo = serverInfo;
        this.field_62585 = arg2;
        this.rawCodeOfConduct = string;
        this.callback = booleanConsumer;
    }

    public CodeOfConductScreen(@Nullable ServerInfo serverInfo, Screen arg2, String string, BooleanConsumer booleanConsumer) {
        this(serverInfo, arg2, Text.literal(string), string, booleanConsumer);
    }

    @Override
    protected LayoutWidget getLayout() {
        DirectionalLayoutWidget lv = DirectionalLayoutWidget.horizontal().spacing(8);
        lv.add(ButtonWidget.builder(ScreenTexts.ACKNOWLEDGE, button -> this.onAnswer(true)).build());
        lv.add(ButtonWidget.builder(ScreenTexts.DISCONNECT, button -> this.onAnswer(false)).build());
        return lv;
    }

    private void onAnswer(boolean acknowledged) {
        this.callback.accept(acknowledged);
        if (this.serverInfo != null) {
            if (acknowledged && this.checkbox.isChecked()) {
                this.serverInfo.setAcceptedCodeOfConduct(this.rawCodeOfConduct);
            } else {
                this.serverInfo.resetAcceptedCodeOfConduct();
            }
            ServerList.updateServerListEntry(this.serverInfo);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.field_62585 instanceof ConnectScreen || this.field_62585 instanceof ReconfiguringScreen) {
            this.field_62585.tick();
        }
    }
}

