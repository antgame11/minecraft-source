/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringHelper;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class CommandBlockExecutor {
    private static final Text DEFAULT_NAME = Text.literal("@");
    private static final int DEFAULT_LAST_EXECUTION = -1;
    private long lastExecution = -1L;
    private boolean updateLastExecution = true;
    private int successCount;
    private boolean trackOutput = true;
    @Nullable
    Text lastOutput;
    private String command = "";
    @Nullable
    private Text customName;

    public int getSuccessCount() {
        return this.successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public Text getLastOutput() {
        return this.lastOutput == null ? ScreenTexts.EMPTY : this.lastOutput;
    }

    public void writeData(WriteView view) {
        view.putString("Command", this.command);
        view.putInt("SuccessCount", this.successCount);
        view.putNullable("CustomName", TextCodecs.CODEC, this.customName);
        view.putBoolean("TrackOutput", this.trackOutput);
        if (this.trackOutput) {
            view.putNullable("LastOutput", TextCodecs.CODEC, this.lastOutput);
        }
        view.putBoolean("UpdateLastExecution", this.updateLastExecution);
        if (this.updateLastExecution && this.lastExecution != -1L) {
            view.putLong("LastExecution", this.lastExecution);
        }
    }

    public void readData(ReadView view) {
        this.command = view.getString("Command", "");
        this.successCount = view.getInt("SuccessCount", 0);
        this.setCustomName(BlockEntity.tryParseCustomName(view, "CustomName"));
        this.trackOutput = view.getBoolean("TrackOutput", true);
        this.lastOutput = this.trackOutput ? BlockEntity.tryParseCustomName(view, "LastOutput") : null;
        this.updateLastExecution = view.getBoolean("UpdateLastExecution", true);
        this.lastExecution = this.updateLastExecution ? view.getLong("LastExecution", -1L) : -1L;
    }

    public void setCommand(String command) {
        this.command = command;
        this.successCount = 0;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean execute(World world) {
        if (world.isClient() || world.getTime() == this.lastExecution) {
            return false;
        }
        if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = Text.literal("#itzlipofutzli");
            this.successCount = 1;
            return true;
        }
        this.successCount = 0;
        MinecraftServer minecraftServer = this.getWorld().getServer();
        if (minecraftServer.areCommandBlocksEnabled() && !StringHelper.isEmpty(this.command)) {
            try {
                this.lastOutput = null;
                try (CommandBlockOutput lv = this.createOutput();){
                    CommandOutput lv2 = Objects.requireNonNullElse(lv, CommandOutput.DUMMY);
                    ServerCommandSource lv3 = this.getSource(lv2).withReturnValueConsumer((successful, returnValue) -> {
                        if (successful) {
                            ++this.successCount;
                        }
                    });
                    minecraftServer.getCommandManager().parseAndExecute(lv3, this.command);
                }
            } catch (Throwable throwable) {
                CrashReport lv4 = CrashReport.create(throwable, "Executing command block");
                CrashReportSection lv5 = lv4.addElement("Command to be executed");
                lv5.add("Command", this::getCommand);
                lv5.add("Name", () -> this.getName().getString());
                throw new CrashException(lv4);
            }
        }
        this.lastExecution = this.updateLastExecution ? world.getTime() : -1L;
        return true;
    }

    @Nullable
    private CommandBlockOutput createOutput() {
        return this.trackOutput ? new CommandBlockOutput() : null;
    }

    public Text getName() {
        return this.customName != null ? this.customName : DEFAULT_NAME;
    }

    @Nullable
    public Text getCustomName() {
        return this.customName;
    }

    public void setCustomName(@Nullable Text customName) {
        this.customName = customName;
    }

    public abstract ServerWorld getWorld();

    public abstract void markDirty();

    public void setLastOutput(@Nullable Text lastOutput) {
        this.lastOutput = lastOutput;
    }

    public void setTrackOutput(boolean trackOutput) {
        this.trackOutput = trackOutput;
    }

    public boolean isTrackingOutput() {
        return this.trackOutput;
    }

    public ActionResult interact(PlayerEntity player) {
        if (!player.isCreativeLevelTwoOp()) {
            return ActionResult.PASS;
        }
        if (player.getEntityWorld().isClient()) {
            player.openCommandBlockMinecartScreen(this);
        }
        return ActionResult.SUCCESS;
    }

    public abstract Vec3d getPos();

    public abstract ServerCommandSource getSource(CommandOutput var1);

    public abstract boolean isEditable();

    protected class CommandBlockOutput
    implements CommandOutput,
    AutoCloseable {
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
        private boolean closed;

        protected CommandBlockOutput() {
        }

        @Override
        public boolean shouldReceiveFeedback() {
            return !this.closed && CommandBlockExecutor.this.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK);
        }

        @Override
        public boolean shouldTrackOutput() {
            return !this.closed;
        }

        @Override
        public boolean shouldBroadcastConsoleToOps() {
            return !this.closed && CommandBlockExecutor.this.getWorld().getGameRules().getBoolean(GameRules.COMMAND_BLOCK_OUTPUT);
        }

        @Override
        public void sendMessage(Text message) {
            if (!this.closed) {
                CommandBlockExecutor.this.lastOutput = Text.literal("[" + TIME_FORMATTER.format(ZonedDateTime.now()) + "] ").append(message);
                CommandBlockExecutor.this.markDirty();
            }
        }

        @Override
        public void close() throws Exception {
            this.closed = true;
        }
    }
}

