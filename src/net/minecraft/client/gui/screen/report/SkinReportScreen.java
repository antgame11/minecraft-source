/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.report;

import java.util.UUID;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.report.AbuseReportReasonScreen;
import net.minecraft.client.gui.screen.report.ReportScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.LayoutWidgets;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.AbuseReportReason;
import net.minecraft.client.session.report.AbuseReportType;
import net.minecraft.client.session.report.SkinAbuseReport;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class SkinReportScreen
extends ReportScreen<SkinAbuseReport.Builder> {
    private static final int SKIN_WIDGET_WIDTH = 85;
    private static final int REASON_BUTTON_AND_COMMENTS_BOX_WIDTH = 178;
    private static final Text TITLE_TEXT = Text.translatable("gui.abuseReport.skin.title");
    private EditBoxWidget commentsBox;
    private ButtonWidget selectReasonButton;

    private SkinReportScreen(Screen parent, AbuseReportContext context, SkinAbuseReport.Builder reportBuilder) {
        super(TITLE_TEXT, parent, context, reportBuilder);
    }

    public SkinReportScreen(Screen parent, AbuseReportContext context, UUID reportedPlayerUuid, Supplier<SkinTextures> skinSupplier) {
        this(parent, context, new SkinAbuseReport.Builder(reportedPlayerUuid, skinSupplier, context.getSender().getLimits()));
    }

    public SkinReportScreen(Screen parent, AbuseReportContext context, SkinAbuseReport report) {
        this(parent, context, new SkinAbuseReport.Builder(report, context.getSender().getLimits()));
    }

    @Override
    protected void addContent() {
        DirectionalLayoutWidget lv = this.layout.add(DirectionalLayoutWidget.horizontal().spacing(8));
        lv.getMainPositioner().alignVerticalCenter();
        lv.add(new PlayerSkinWidget(85, 120, this.client.getLoadedEntityModels(), ((SkinAbuseReport)((SkinAbuseReport.Builder)this.reportBuilder).getReport()).getSkinSupplier()));
        DirectionalLayoutWidget lv2 = lv.add(DirectionalLayoutWidget.vertical().spacing(8));
        this.selectReasonButton = ButtonWidget.builder(SELECT_REASON_TEXT, button -> this.client.setScreen(new AbuseReportReasonScreen(this, ((SkinAbuseReport.Builder)this.reportBuilder).getReason(), AbuseReportType.SKIN, reason -> {
            ((SkinAbuseReport.Builder)this.reportBuilder).setReason((AbuseReportReason)((Object)((Object)reason)));
            this.onChange();
        }))).width(178).build();
        lv2.add(LayoutWidgets.createLabeledWidget(this.textRenderer, this.selectReasonButton, OBSERVED_WHAT_TEXT));
        this.commentsBox = this.createCommentsBox(178, this.textRenderer.fontHeight * 8, comments -> {
            ((SkinAbuseReport.Builder)this.reportBuilder).setOpinionComments((String)comments);
            this.onChange();
        });
        lv2.add(LayoutWidgets.createLabeledWidget(this.textRenderer, this.commentsBox, MORE_COMMENTS_TEXT, positioner -> positioner.marginBottom(12)));
    }

    @Override
    protected void onChange() {
        AbuseReportReason lv = ((SkinAbuseReport.Builder)this.reportBuilder).getReason();
        if (lv != null) {
            this.selectReasonButton.setMessage(lv.getText());
        } else {
            this.selectReasonButton.setMessage(SELECT_REASON_TEXT);
        }
        super.onChange();
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (super.mouseReleased(click)) {
            return true;
        }
        return this.commentsBox.mouseReleased(click);
    }
}

