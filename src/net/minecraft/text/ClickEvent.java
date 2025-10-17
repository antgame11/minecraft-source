/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;

public interface ClickEvent {
    public static final Codec<ClickEvent> CODEC = Action.CODEC.dispatch("action", ClickEvent::getAction, action -> action.codec);

    public Action getAction();

    public static enum Action implements StringIdentifiable
    {
        OPEN_URL("open_url", true, OpenUrl.CODEC),
        OPEN_FILE("open_file", false, OpenFile.CODEC),
        RUN_COMMAND("run_command", true, RunCommand.CODEC),
        SUGGEST_COMMAND("suggest_command", true, SuggestCommand.CODEC),
        SHOW_DIALOG("show_dialog", true, ShowDialog.CODEC),
        CHANGE_PAGE("change_page", true, ChangePage.CODEC),
        COPY_TO_CLIPBOARD("copy_to_clipboard", true, CopyToClipboard.CODEC),
        CUSTOM("custom", true, Custom.CODEC);

        public static final Codec<Action> UNVALIDATED_CODEC;
        public static final Codec<Action> CODEC;
        private final boolean userDefinable;
        private final String name;
        final MapCodec<? extends ClickEvent> codec;

        private Action(String name, boolean userDefinable, MapCodec<? extends ClickEvent> codec) {
            this.name = name;
            this.userDefinable = userDefinable;
            this.codec = codec;
        }

        public boolean isUserDefinable() {
            return this.userDefinable;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public MapCodec<? extends ClickEvent> getCodec() {
            return this.codec;
        }

        public static DataResult<Action> validate(Action action) {
            if (!action.isUserDefinable()) {
                return DataResult.error(() -> "Click event type not allowed: " + String.valueOf(action));
            }
            return DataResult.success(action, Lifecycle.stable());
        }

        static {
            UNVALIDATED_CODEC = StringIdentifiable.createCodec(Action::values);
            CODEC = UNVALIDATED_CODEC.validate(Action::validate);
        }
    }

    public record Custom(Identifier id, Optional<NbtElement> payload) implements ClickEvent
    {
        public static final MapCodec<Custom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("id")).forGetter(Custom::id), Codecs.NBT_ELEMENT.optionalFieldOf("payload").forGetter(Custom::payload)).apply((Applicative<Custom, ?>)instance, Custom::new));

        @Override
        public Action getAction() {
            return Action.CUSTOM;
        }
    }

    public record CopyToClipboard(String value) implements ClickEvent
    {
        public static final MapCodec<CopyToClipboard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("value")).forGetter(CopyToClipboard::value)).apply((Applicative<CopyToClipboard, ?>)instance, CopyToClipboard::new));

        @Override
        public Action getAction() {
            return Action.COPY_TO_CLIPBOARD;
        }
    }

    public record ChangePage(int page) implements ClickEvent
    {
        public static final MapCodec<ChangePage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.POSITIVE_INT.fieldOf("page")).forGetter(ChangePage::page)).apply((Applicative<ChangePage, ?>)instance, ChangePage::new));

        @Override
        public Action getAction() {
            return Action.CHANGE_PAGE;
        }
    }

    public record ShowDialog(RegistryEntry<Dialog> dialog) implements ClickEvent
    {
        public static final MapCodec<ShowDialog> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Dialog.ENTRY_CODEC.fieldOf("dialog")).forGetter(ShowDialog::dialog)).apply((Applicative<ShowDialog, ?>)instance, ShowDialog::new));

        @Override
        public Action getAction() {
            return Action.SHOW_DIALOG;
        }
    }

    public record SuggestCommand(String command) implements ClickEvent
    {
        public static final MapCodec<SuggestCommand> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.CHAT_TEXT.fieldOf("command")).forGetter(SuggestCommand::command)).apply((Applicative<SuggestCommand, ?>)instance, SuggestCommand::new));

        @Override
        public Action getAction() {
            return Action.SUGGEST_COMMAND;
        }
    }

    public record RunCommand(String command) implements ClickEvent
    {
        public static final MapCodec<RunCommand> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.CHAT_TEXT.fieldOf("command")).forGetter(RunCommand::command)).apply((Applicative<RunCommand, ?>)instance, RunCommand::new));

        @Override
        public Action getAction() {
            return Action.RUN_COMMAND;
        }
    }

    public record OpenFile(String path) implements ClickEvent
    {
        public static final MapCodec<OpenFile> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("path")).forGetter(OpenFile::path)).apply((Applicative<OpenFile, ?>)instance, OpenFile::new));

        public OpenFile(File file) {
            this(file.toString());
        }

        public OpenFile(Path path) {
            this(path.toFile());
        }

        public File file() {
            return new File(this.path);
        }

        @Override
        public Action getAction() {
            return Action.OPEN_FILE;
        }
    }

    public record OpenUrl(URI uri) implements ClickEvent
    {
        public static final MapCodec<OpenUrl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.URI.fieldOf("url")).forGetter(OpenUrl::uri)).apply((Applicative<OpenUrl, ?>)instance, OpenUrl::new));

        @Override
        public Action getAction() {
            return Action.OPEN_URL;
        }
    }
}

