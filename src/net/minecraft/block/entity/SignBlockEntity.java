/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SignBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TEXT_WIDTH = 90;
    private static final int TEXT_LINE_HEIGHT = 10;
    private static final boolean DEFAULT_WAXED = false;
    @Nullable
    private UUID editor;
    private SignText frontText = this.createText();
    private SignText backText = this.createText();
    private boolean waxed = false;

    public SignBlockEntity(BlockPos pos, BlockState state) {
        this((BlockEntityType)BlockEntityType.SIGN, pos, state);
    }

    public SignBlockEntity(BlockEntityType arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    protected SignText createText() {
        return new SignText();
    }

    public boolean isPlayerFacingFront(PlayerEntity player) {
        Block block = this.getCachedState().getBlock();
        if (block instanceof AbstractSignBlock) {
            float g;
            AbstractSignBlock lv = (AbstractSignBlock)block;
            Vec3d lv2 = lv.getCenter(this.getCachedState());
            double d = player.getX() - ((double)this.getPos().getX() + lv2.x);
            double e = player.getZ() - ((double)this.getPos().getZ() + lv2.z);
            float f = lv.getRotationDegrees(this.getCachedState());
            return MathHelper.angleBetween(f, g = (float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0f) <= 90.0f;
        }
        return false;
    }

    public SignText getText(boolean front) {
        return front ? this.frontText : this.backText;
    }

    public SignText getFrontText() {
        return this.frontText;
    }

    public SignText getBackText() {
        return this.backText;
    }

    public int getTextLineHeight() {
        return 10;
    }

    public int getMaxTextWidth() {
        return 90;
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put("front_text", SignText.CODEC, this.frontText);
        view.put("back_text", SignText.CODEC, this.backText);
        view.putBoolean("is_waxed", this.waxed);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.frontText = view.read("front_text", SignText.CODEC).map(this::parseLines).orElseGet(SignText::new);
        this.backText = view.read("back_text", SignText.CODEC).map(this::parseLines).orElseGet(SignText::new);
        this.waxed = view.getBoolean("is_waxed", false);
    }

    private SignText parseLines(SignText signText) {
        for (int i = 0; i < 4; ++i) {
            Text lv = this.parseLine(signText.getMessage(i, false));
            Text lv2 = this.parseLine(signText.getMessage(i, true));
            signText = signText.withMessage(i, lv, lv2);
        }
        return signText;
    }

    private Text parseLine(Text text) {
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            try {
                return Texts.parse(SignBlockEntity.createCommandSource(null, lv, this.pos), text, null, 0);
            } catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }
        return text;
    }

    public void tryChangeText(PlayerEntity player, boolean front, List<FilteredMessage> messages) {
        if (this.isWaxed() || !player.getUuid().equals(this.getEditor()) || this.world == null) {
            LOGGER.warn("Player {} just tried to change non-editable sign", (Object)player.getStringifiedName());
            return;
        }
        this.changeText(text -> this.getTextWithMessages(player, messages, (SignText)text), front);
        this.setEditor(null);
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    public boolean changeText(UnaryOperator<SignText> textChanger, boolean front) {
        SignText lv = this.getText(front);
        return this.setText((SignText)textChanger.apply(lv), front);
    }

    private SignText getTextWithMessages(PlayerEntity player, List<FilteredMessage> messages, SignText text) {
        for (int i = 0; i < messages.size(); ++i) {
            FilteredMessage lv = messages.get(i);
            Style lv2 = text.getMessage(i, player.shouldFilterText()).getStyle();
            text = player.shouldFilterText() ? text.withMessage(i, Text.literal(lv.getString()).setStyle(lv2)) : text.withMessage(i, Text.literal(lv.raw()).setStyle(lv2), Text.literal(lv.getString()).setStyle(lv2));
        }
        return text;
    }

    public boolean setText(SignText text, boolean front) {
        return front ? this.setFrontText(text) : this.setBackText(text);
    }

    private boolean setBackText(SignText backText) {
        if (backText != this.backText) {
            this.backText = backText;
            this.updateListeners();
            return true;
        }
        return false;
    }

    private boolean setFrontText(SignText frontText) {
        if (frontText != this.frontText) {
            this.frontText = frontText;
            this.updateListeners();
            return true;
        }
        return false;
    }

    public boolean canRunCommandClickEvent(boolean front, PlayerEntity player) {
        return this.isWaxed() && this.getText(front).hasRunCommandClickEvent(player);
    }

    public boolean runCommandClickEvent(ServerWorld world, PlayerEntity player, BlockPos pos, boolean front) {
        boolean bl2 = false;
        block5: for (Text lv : this.getText(front).getMessages(player.shouldFilterText())) {
            ClickEvent lv3;
            Style lv2 = lv.getStyle();
            ClickEvent clickEvent = lv3 = lv2.getClickEvent();
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.RunCommand.class, ClickEvent.ShowDialog.class, ClickEvent.Custom.class}, (Object)clickEvent, n)) {
                case 0: {
                    ClickEvent.RunCommand lv4 = (ClickEvent.RunCommand)clickEvent;
                    world.getServer().getCommandManager().parseAndExecute(SignBlockEntity.createCommandSource(player, world, pos), lv4.command());
                    bl2 = true;
                    continue block5;
                }
                case 1: {
                    ClickEvent.ShowDialog lv5 = (ClickEvent.ShowDialog)clickEvent;
                    player.openDialog(lv5.dialog());
                    bl2 = true;
                    continue block5;
                }
                case 2: {
                    ClickEvent.Custom lv6 = (ClickEvent.Custom)clickEvent;
                    world.getServer().handleCustomClickAction(lv6.id(), lv6.payload());
                    bl2 = true;
                    continue block5;
                }
            }
        }
        return bl2;
    }

    private static ServerCommandSource createCommandSource(@Nullable PlayerEntity player, ServerWorld world, BlockPos pos) {
        String string = player == null ? "Sign" : player.getStringifiedName();
        Text lv = player == null ? Text.literal("Sign") : player.getDisplayName();
        return new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ofCenter(pos), Vec2f.ZERO, world, 2, string, lv, world.getServer(), player);
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    public void setEditor(@Nullable UUID editor) {
        this.editor = editor;
    }

    @Nullable
    public UUID getEditor() {
        return this.editor;
    }

    private void updateListeners() {
        this.markDirty();
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    public boolean isWaxed() {
        return this.waxed;
    }

    public boolean setWaxed(boolean waxed) {
        if (this.waxed != waxed) {
            this.waxed = waxed;
            this.updateListeners();
            return true;
        }
        return false;
    }

    public boolean isPlayerTooFarToEdit(UUID uuid) {
        PlayerEntity lv = this.world.getPlayerByUuid(uuid);
        return lv == null || !lv.canInteractWithBlockAt(this.getPos(), 4.0);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SignBlockEntity blockEntity) {
        UUID uUID = blockEntity.getEditor();
        if (uUID != null) {
            blockEntity.tryClearInvalidEditor(blockEntity, world, uUID);
        }
    }

    private void tryClearInvalidEditor(SignBlockEntity blockEntity, World world, UUID uuid) {
        if (blockEntity.isPlayerTooFarToEdit(uuid)) {
            blockEntity.setEditor(null);
        }
    }

    public SoundEvent getInteractionFailSound() {
        return SoundEvents.BLOCK_SIGN_WAXED_INTERACT_FAIL;
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

