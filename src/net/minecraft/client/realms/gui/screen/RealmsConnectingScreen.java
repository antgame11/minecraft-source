/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.realms.ServiceQuality;
import net.minecraft.client.realms.dto.RealmsServerAddress;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class RealmsConnectingScreen
extends RealmsLongRunningMcoTaskScreen {
    private final LongRunningTask connectTask;
    private final RealmsServerAddress serverAddress;
    private final DirectionalLayoutWidget footerLayout = DirectionalLayoutWidget.vertical();

    public RealmsConnectingScreen(Screen parent, RealmsServerAddress serverAddress, LongRunningTask connectTask) {
        super(parent, connectTask);
        this.connectTask = connectTask;
        this.serverAddress = serverAddress;
    }

    @Override
    public void init() {
        super.init();
        if (this.serverAddress.regionData() == null || this.serverAddress.regionData().region() == null) {
            return;
        }
        DirectionalLayoutWidget lv = DirectionalLayoutWidget.horizontal().spacing(10);
        TextWidget lv2 = new TextWidget(Text.translatable("mco.connect.region", Text.translatable(this.serverAddress.regionData().region().translationKey)), this.textRenderer);
        lv.add(lv2);
        Identifier lv3 = this.serverAddress.regionData().serviceQuality() != null ? this.serverAddress.regionData().serviceQuality().getIcon() : ServiceQuality.UNKNOWN.getIcon();
        lv.add(IconWidget.create(10, 8, lv3), Positioner::alignTop);
        this.footerLayout.add(lv, positioner -> positioner.marginTop(40));
        this.footerLayout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();
        int i = this.layout.getY() + this.layout.getHeight();
        ScreenRect lv = new ScreenRect(0, i, this.width, this.height - i);
        this.footerLayout.refreshPositions();
        SimplePositioningWidget.setPos(this.footerLayout, lv, 0.5f, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.connectTask.tick();
    }

    @Override
    protected void onCancel() {
        this.connectTask.abortTask();
        super.onCancel();
    }
}

