/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.report;

import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TaskScreen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.session.report.AbuseReport;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Nullables;
import net.minecraft.util.TextifiedException;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class ReportScreen<B extends AbuseReport.Builder<?>>
extends Screen {
    private static final Text REPORT_SENT_MESSAGE_TEXT = Text.translatable("gui.abuseReport.report_sent_msg");
    private static final Text SENDING_TITLE_TEXT = Text.translatable("gui.abuseReport.sending.title").formatted(Formatting.BOLD);
    private static final Text SENT_TITLE_TEXT = Text.translatable("gui.abuseReport.sent.title").formatted(Formatting.BOLD);
    private static final Text ERROR_TITLE_TEXT = Text.translatable("gui.abuseReport.error.title").formatted(Formatting.BOLD);
    private static final Text GENERIC_ERROR_TEXT = Text.translatable("gui.abuseReport.send.generic_error");
    protected static final Text SEND_TEXT = Text.translatable("gui.abuseReport.send");
    protected static final Text OBSERVED_WHAT_TEXT = Text.translatable("gui.abuseReport.observed_what");
    protected static final Text SELECT_REASON_TEXT = Text.translatable("gui.abuseReport.select_reason");
    private static final Text DESCRIBE_TEXT = Text.translatable("gui.abuseReport.describe");
    protected static final Text MORE_COMMENTS_TEXT = Text.translatable("gui.abuseReport.more_comments");
    private static final Text COMMENTS_TEXT = Text.translatable("gui.abuseReport.comments");
    private static final Text ATTESTATION_TEXT = Text.translatable("gui.abuseReport.attestation");
    protected static final int field_52303 = 120;
    protected static final int field_46016 = 20;
    protected static final int field_46017 = 280;
    protected static final int field_46018 = 8;
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final Screen parent;
    protected final AbuseReportContext context;
    protected final DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical().spacing(8);
    protected B reportBuilder;
    private CheckboxWidget checkbox;
    protected ButtonWidget sendButton;

    protected ReportScreen(Text title, Screen parent, AbuseReportContext context, B reportBuilder) {
        super(title);
        this.parent = parent;
        this.context = context;
        this.reportBuilder = reportBuilder;
    }

    protected EditBoxWidget createCommentsBox(int width, int height, Consumer<String> changeListener) {
        AbuseReportLimits abuseReportLimits = this.context.getSender().getLimits();
        EditBoxWidget lv = EditBoxWidget.builder().placeholder(DESCRIBE_TEXT).build(this.textRenderer, width, height, COMMENTS_TEXT);
        lv.setText(((AbuseReport.Builder)this.reportBuilder).getOpinionComments());
        lv.setMaxLength(abuseReportLimits.maxOpinionCommentsLength());
        lv.setChangeListener(changeListener);
        return lv;
    }

    @Override
    protected void init() {
        this.layout.getMainPositioner().alignHorizontalCenter();
        this.addTitle();
        this.addContent();
        this.addAttestationCheckboxAndSendButton();
        this.onChange();
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.refreshWidgetPositions();
    }

    protected void addTitle() {
        this.layout.add(new TextWidget(this.title, this.textRenderer));
    }

    protected abstract void addContent();

    protected void addAttestationCheckboxAndSendButton() {
        this.checkbox = this.layout.add(CheckboxWidget.builder(ATTESTATION_TEXT, this.textRenderer).checked(((AbuseReport.Builder)this.reportBuilder).isAttested()).maxWidth(280).callback((checkbox, attested) -> {
            ((AbuseReport.Builder)this.reportBuilder).setAttested(attested);
            this.onChange();
        }).build());
        DirectionalLayoutWidget lv = this.layout.add(DirectionalLayoutWidget.horizontal().spacing(8));
        lv.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).width(120).build());
        this.sendButton = lv.add(ButtonWidget.builder(SEND_TEXT, button -> this.trySend()).width(120).build());
    }

    protected void onChange() {
        AbuseReport.ValidationError lv = ((AbuseReport.Builder)this.reportBuilder).validate();
        this.sendButton.active = lv == null && this.checkbox.isChecked();
        this.sendButton.setTooltip(Nullables.map(lv, AbuseReport.ValidationError::createTooltip));
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    protected void trySend() {
        ((AbuseReport.Builder)this.reportBuilder).build(this.context).ifLeft(reportWithId -> {
            CompletableFuture<Unit> completableFuture = this.context.getSender().send(reportWithId.id(), reportWithId.reportType(), reportWithId.report());
            this.client.setScreen(TaskScreen.createRunningScreen(SENDING_TITLE_TEXT, ScreenTexts.CANCEL, () -> {
                this.client.setScreen(this);
                completableFuture.cancel(true);
            }));
            completableFuture.handleAsync((v, throwable) -> {
                if (throwable == null) {
                    this.onSent();
                } else {
                    if (throwable instanceof CancellationException) {
                        return null;
                    }
                    this.onSendError((Throwable)throwable);
                }
                return null;
            }, (Executor)this.client);
        }).ifRight(validationError -> this.showError(validationError.message()));
    }

    private void onSent() {
        this.resetDraft();
        this.client.setScreen(TaskScreen.createResultScreen(SENT_TITLE_TEXT, REPORT_SENT_MESSAGE_TEXT, ScreenTexts.DONE, () -> this.client.setScreen(null)));
    }

    private void onSendError(Throwable error) {
        Text lv2;
        LOGGER.error("Encountered error while sending abuse report", error);
        Throwable throwable = error.getCause();
        if (throwable instanceof TextifiedException) {
            TextifiedException lv = (TextifiedException)throwable;
            lv2 = lv.getMessageText();
        } else {
            lv2 = GENERIC_ERROR_TEXT;
        }
        this.showError(lv2);
    }

    private void showError(Text errorMessage) {
        MutableText lv = errorMessage.copy().formatted(Formatting.RED);
        this.client.setScreen(TaskScreen.createResultScreen(ERROR_TITLE_TEXT, lv, ScreenTexts.BACK, () -> this.client.setScreen(this)));
    }

    void saveDraft() {
        if (((AbuseReport.Builder)this.reportBuilder).hasEnoughInfo()) {
            this.context.setDraft(((AbuseReport)((AbuseReport.Builder)this.reportBuilder).getReport()).copy());
        }
    }

    void resetDraft() {
        this.context.setDraft(null);
    }

    @Override
    public void close() {
        if (((AbuseReport.Builder)this.reportBuilder).hasEnoughInfo()) {
            this.client.setScreen(new DiscardWarningScreen());
        } else {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void removed() {
        this.saveDraft();
        super.removed();
    }

    @Environment(value=EnvType.CLIENT)
    class DiscardWarningScreen
    extends WarningScreen {
        private static final Text TITLE = Text.translatable("gui.abuseReport.discard.title").formatted(Formatting.BOLD);
        private static final Text MESSAGE = Text.translatable("gui.abuseReport.discard.content");
        private static final Text RETURN_BUTTON_TEXT = Text.translatable("gui.abuseReport.discard.return");
        private static final Text DRAFT_BUTTON_TEXT = Text.translatable("gui.abuseReport.discard.draft");
        private static final Text DISCARD_BUTTON_TEXT = Text.translatable("gui.abuseReport.discard.discard");

        protected DiscardWarningScreen() {
            super(TITLE, MESSAGE, MESSAGE);
        }

        @Override
        protected LayoutWidget getLayout() {
            DirectionalLayoutWidget lv = DirectionalLayoutWidget.vertical().spacing(8);
            lv.getMainPositioner().alignHorizontalCenter();
            DirectionalLayoutWidget lv2 = lv.add(DirectionalLayoutWidget.horizontal().spacing(8));
            lv2.add(ButtonWidget.builder(RETURN_BUTTON_TEXT, button -> this.close()).build());
            lv2.add(ButtonWidget.builder(DRAFT_BUTTON_TEXT, button -> {
                ReportScreen.this.saveDraft();
                this.client.setScreen(ReportScreen.this.parent);
            }).build());
            lv.add(ButtonWidget.builder(DISCARD_BUTTON_TEXT, button -> {
                ReportScreen.this.resetDraft();
                this.client.setScreen(ReportScreen.this.parent);
            }).build());
            return lv;
        }

        @Override
        public void close() {
            this.client.setScreen(ReportScreen.this);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }
    }
}

