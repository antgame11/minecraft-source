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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

@Environment(value=EnvType.CLIENT)
public class AddServerScreen
extends Screen {
    private static final Text ENTER_NAME_TEXT = Text.translatable("manageServer.enterName");
    private static final Text ENTER_IP_TEXT = Text.translatable("manageServer.enterIp");
    private static final Text field_62476 = Text.translatable("selectServer.defaultName");
    private ButtonWidget addButton;
    private final BooleanConsumer callback;
    private final ServerInfo server;
    private TextFieldWidget addressField;
    private TextFieldWidget serverNameField;
    private final Screen parent;

    public AddServerScreen(Screen parent, Text arg2, BooleanConsumer booleanConsumer, ServerInfo arg3) {
        super(arg2);
        this.parent = parent;
        this.callback = booleanConsumer;
        this.server = arg3;
    }

    @Override
    protected void init() {
        this.serverNameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 66, 200, 20, ENTER_NAME_TEXT);
        this.serverNameField.setText(this.server.name);
        this.serverNameField.setPlaceholder(field_62476);
        this.serverNameField.setChangedListener(serverName -> this.updateAddButton());
        this.addSelectableChild(this.serverNameField);
        this.addressField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 106, 200, 20, ENTER_IP_TEXT);
        this.addressField.setMaxLength(128);
        this.addressField.setText(this.server.address);
        this.addressField.setChangedListener(address -> this.updateAddButton());
        this.addSelectableChild(this.addressField);
        this.addDrawableChild(CyclingButtonWidget.builder(ServerInfo.ResourcePackPolicy::getName).values((ServerInfo.ResourcePackPolicy[])ServerInfo.ResourcePackPolicy.values()).initially(this.server.getResourcePackPolicy()).build(this.width / 2 - 100, this.height / 4 + 72, 200, 20, Text.translatable("manageServer.resourcePack"), (button, resourcePackPolicy) -> this.server.setResourcePackPolicy((ServerInfo.ResourcePackPolicy)((Object)resourcePackPolicy))));
        this.addButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.addAndClose()).dimensions(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.callback.accept(false)).dimensions(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20).build());
        this.updateAddButton();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.serverNameField);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.addressField.getText();
        String string2 = this.serverNameField.getText();
        this.init(client, width, height);
        this.addressField.setText(string);
        this.serverNameField.setText(string2);
    }

    private void addAndClose() {
        String string = this.serverNameField.getText();
        this.server.name = string.isEmpty() ? field_62476.getString() : string;
        this.server.address = this.addressField.getText();
        this.callback.accept(true);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void updateAddButton() {
        this.addButton.active = ServerAddress.isValid(this.addressField.getText());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 17, Colors.WHITE);
        context.drawTextWithShadow(this.textRenderer, ENTER_NAME_TEXT, this.width / 2 - 100 + 1, 53, Colors.LIGHT_GRAY);
        context.drawTextWithShadow(this.textRenderer, ENTER_IP_TEXT, this.width / 2 - 100 + 1, 94, Colors.LIGHT_GRAY);
        this.serverNameField.render(context, mouseX, mouseY, deltaTicks);
        this.addressField.render(context, mouseX, mouseY, deltaTicks);
    }
}

