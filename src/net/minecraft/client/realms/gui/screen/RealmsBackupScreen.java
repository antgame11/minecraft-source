/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.RealmsPopups;
import net.minecraft.client.realms.gui.screen.RealmsBackupInfoScreen;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.task.DownloadTask;
import net.minecraft.client.realms.task.RestoreTask;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsBackupScreen
extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Text BACKUPS_TEXT = Text.translatable("mco.configure.world.backup");
    static final Text RESTORE_TEXT = Text.translatable("mco.backup.button.restore");
    static final Text CHANGES_TOOLTIP = Text.translatable("mco.backup.changes.tooltip");
    private static final Text NO_BACKUPS_TEXT = Text.translatable("mco.backup.nobackups");
    private static final Text DOWNLOAD_TEXT = Text.translatable("mco.backup.button.download");
    private static final String UPLOADED = "uploaded";
    private static final int field_49447 = 8;
    final RealmsConfigureWorldScreen parent;
    List<Backup> backups = Collections.emptyList();
    @Nullable
    BackupObjectSelectionList selectionList;
    final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final int slotId;
    @Nullable
    ButtonWidget downloadButton;
    final RealmsServer serverData;
    boolean noBackups = false;

    public RealmsBackupScreen(RealmsConfigureWorldScreen parent, RealmsServer serverData, int slotId) {
        super(BACKUPS_TEXT);
        this.parent = parent;
        this.serverData = serverData;
        this.slotId = slotId;
    }

    @Override
    public void init() {
        this.layout.addHeader(BACKUPS_TEXT, this.textRenderer);
        this.selectionList = this.layout.addBody(new BackupObjectSelectionList(this));
        DirectionalLayoutWidget lv = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        this.downloadButton = lv.add(ButtonWidget.builder(DOWNLOAD_TEXT, button -> this.downloadClicked()).build());
        this.downloadButton.active = false;
        lv.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).build());
        this.layout.forEachChild(arg2 -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(arg2);
        });
        this.refreshWidgetPositions();
        this.startBackupFetcher();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        if (this.noBackups && this.selectionList != null) {
            context.drawTextWithShadow(this.textRenderer, NO_BACKUPS_TEXT, this.width / 2 - this.textRenderer.getWidth(NO_BACKUPS_TEXT) / 2, this.selectionList.getY() + this.selectionList.getHeight() / 2 - this.textRenderer.fontHeight / 2, Colors.WHITE);
        }
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.selectionList != null) {
            this.selectionList.position(this.width, this.layout);
        }
    }

    private void startBackupFetcher() {
        new Thread("Realms-fetch-backups"){

            @Override
            public void run() {
                RealmsClient lv = RealmsClient.create();
                try {
                    List<Backup> list = lv.backupsFor((long)RealmsBackupScreen.this.serverData.id).backups;
                    RealmsBackupScreen.this.client.execute(() -> {
                        RealmsBackupScreen.this.backups = list;
                        RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.isEmpty();
                        if (!RealmsBackupScreen.this.noBackups && RealmsBackupScreen.this.downloadButton != null) {
                            RealmsBackupScreen.this.downloadButton.active = true;
                        }
                        if (RealmsBackupScreen.this.selectionList != null) {
                            RealmsBackupScreen.this.selectionList.replaceEntries(RealmsBackupScreen.this.backups.stream().map(backup -> new BackupObjectSelectionListEntry((Backup)backup)).toList());
                        }
                    });
                } catch (RealmsServiceException lv2) {
                    LOGGER.error("Couldn't request backups", lv2);
                }
            }
        }.start();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void downloadClicked() {
        this.client.setScreen(RealmsPopups.createInfoPopup(this, Text.translatable("mco.configure.world.restore.download.question.line1"), button -> this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent.getNewScreen(), new DownloadTask(this.serverData.id, this.slotId, Objects.requireNonNullElse(this.serverData.name, "") + " (" + this.serverData.slots.get((Object)Integer.valueOf((int)this.serverData.activeSlot)).options.getSlotName(this.serverData.activeSlot) + ")", this)))));
    }

    @Environment(value=EnvType.CLIENT)
    class BackupObjectSelectionList
    extends ElementListWidget<BackupObjectSelectionListEntry> {
        private static final int field_49450 = 36;

        public BackupObjectSelectionList(RealmsBackupScreen arg) {
            super(MinecraftClient.getInstance(), arg.width, arg.layout.getContentHeight(), arg.layout.getHeaderHeight(), 36);
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BackupObjectSelectionListEntry
    extends ElementListWidget.Entry<BackupObjectSelectionListEntry> {
        private static final int field_44525 = 2;
        private final Backup mBackup;
        @Nullable
        private ButtonWidget restoreButton;
        @Nullable
        private ButtonWidget changesButton;
        private final List<ClickableWidget> buttons = new ArrayList<ClickableWidget>();

        public BackupObjectSelectionListEntry(Backup backup) {
            this.mBackup = backup;
            this.updateChangeList(backup);
            if (!backup.changeList.isEmpty()) {
                this.changesButton = ButtonWidget.builder(CHANGES_TOOLTIP, button -> RealmsBackupScreen.this.client.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, this.mBackup))).width(8 + RealmsBackupScreen.this.textRenderer.getWidth(CHANGES_TOOLTIP)).narrationSupplier(textSupplier -> ScreenTexts.joinSentences(Text.translatable("mco.backup.narration", this.getLastModifiedDate()), (Text)textSupplier.get())).build();
                this.buttons.add(this.changesButton);
            }
            if (!RealmsBackupScreen.this.serverData.expired) {
                this.restoreButton = ButtonWidget.builder(RESTORE_TEXT, button -> this.restore()).width(8 + RealmsBackupScreen.this.textRenderer.getWidth(CHANGES_TOOLTIP)).narrationSupplier(textSupplier -> ScreenTexts.joinSentences(Text.translatable("mco.backup.narration", this.getLastModifiedDate()), (Text)textSupplier.get())).build();
                this.buttons.add(this.restoreButton);
            }
        }

        private void updateChangeList(Backup backup) {
            int i = RealmsBackupScreen.this.backups.indexOf(backup);
            if (i == RealmsBackupScreen.this.backups.size() - 1) {
                return;
            }
            Backup lv = RealmsBackupScreen.this.backups.get(i + 1);
            for (String string : backup.metadata.keySet()) {
                if (!string.contains(RealmsBackupScreen.UPLOADED) && lv.metadata.containsKey(string)) {
                    if (backup.metadata.get(string).equals(lv.metadata.get(string))) continue;
                    this.addChange(string);
                    continue;
                }
                this.addChange(string);
            }
        }

        private void addChange(String metadataKey) {
            if (metadataKey.contains(RealmsBackupScreen.UPLOADED)) {
                String string2 = DateFormat.getDateTimeInstance(3, 3).format(this.mBackup.lastModifiedDate);
                this.mBackup.changeList.put(metadataKey, string2);
                this.mBackup.setUploadedVersion(true);
            } else {
                this.mBackup.changeList.put(metadataKey, this.mBackup.metadata.get(metadataKey));
            }
        }

        private String getLastModifiedDate() {
            return DateFormat.getDateTimeInstance(3, 3).format(this.mBackup.lastModifiedDate);
        }

        private void restore() {
            Text lv = RealmsUtil.convertToAgePresentation(this.mBackup.lastModifiedDate);
            MutableText lv2 = Text.translatable("mco.configure.world.restore.question.line1", this.getLastModifiedDate(), lv);
            RealmsBackupScreen.this.client.setScreen(RealmsPopups.createContinuableWarningPopup(RealmsBackupScreen.this, lv2, popup -> {
                RealmsConfigureWorldScreen lv = RealmsBackupScreen.this.parent.getNewScreen();
                RealmsBackupScreen.this.client.setScreen(new RealmsLongRunningMcoTaskScreen(lv, new RestoreTask(this.mBackup, RealmsBackupScreen.this.serverData.id, lv)));
            }));
        }

        @Override
        public List<? extends Element> children() {
            return this.buttons;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.buttons;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int k = this.getContentMiddleY();
            int l = k - ((RealmsBackupScreen)RealmsBackupScreen.this).textRenderer.fontHeight - 2;
            int m = k + 2;
            int n = this.mBackup.isUploadedVersion() ? RealmsScreen.GREEN : Colors.WHITE;
            context.drawTextWithShadow(RealmsBackupScreen.this.textRenderer, Text.translatable("mco.backup.entry", RealmsUtil.convertToAgePresentation(this.mBackup.lastModifiedDate)), this.getContentX(), l, n);
            context.drawTextWithShadow(RealmsBackupScreen.this.textRenderer, this.getMediumDatePresentation(this.mBackup.lastModifiedDate), this.getContentX(), m, RealmsScreen.DARK_GRAY);
            int o = 0;
            int p = this.getContentMiddleY() - 10;
            if (this.restoreButton != null) {
                this.restoreButton.setX(this.getContentRightEnd() - (o += this.restoreButton.getWidth() + 8));
                this.restoreButton.setY(p);
                this.restoreButton.render(context, mouseX, mouseY, deltaTicks);
            }
            if (this.changesButton != null) {
                this.changesButton.setX(this.getContentRightEnd() - (o += this.changesButton.getWidth() + 8));
                this.changesButton.setY(p);
                this.changesButton.render(context, mouseX, mouseY, deltaTicks);
            }
        }

        private String getMediumDatePresentation(Date lastModifiedDate) {
            return DateFormat.getDateTimeInstance(3, 3).format(lastModifiedDate);
        }
    }
}

