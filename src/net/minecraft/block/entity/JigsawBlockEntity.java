/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

public class JigsawBlockEntity
extends BlockEntity {
    public static final Codec<RegistryKey<StructurePool>> STRUCTURE_POOL_KEY_CODEC = RegistryKey.createCodec(RegistryKeys.TEMPLATE_POOL);
    public static final Identifier DEFAULT_NAME = Identifier.ofVanilla("empty");
    private static final int DEFAULT_PLACEMENT_PRIORITY = 0;
    private static final int DEFAULT_SELECTION_PRIORITY = 0;
    public static final String TARGET_KEY = "target";
    public static final String POOL_KEY = "pool";
    public static final String JOINT_KEY = "joint";
    public static final String PLACEMENT_PRIORITY_KEY = "placement_priority";
    public static final String SELECTION_PRIORITY_KEY = "selection_priority";
    public static final String NAME_KEY = "name";
    public static final String FINAL_STATE_KEY = "final_state";
    public static final String DEFAULT_FINAL_STATE = "minecraft:air";
    private Identifier name = DEFAULT_NAME;
    private Identifier target = DEFAULT_NAME;
    private RegistryKey<StructurePool> pool = StructurePools.EMPTY;
    private Joint joint = Joint.ROLLABLE;
    private String finalState = "minecraft:air";
    private int placementPriority = 0;
    private int selectionPriority = 0;

    public JigsawBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.JIGSAW, pos, state);
    }

    public Identifier getName() {
        return this.name;
    }

    public Identifier getTarget() {
        return this.target;
    }

    public RegistryKey<StructurePool> getPool() {
        return this.pool;
    }

    public String getFinalState() {
        return this.finalState;
    }

    public Joint getJoint() {
        return this.joint;
    }

    public int getPlacementPriority() {
        return this.placementPriority;
    }

    public int getSelectionPriority() {
        return this.selectionPriority;
    }

    public void setName(Identifier name) {
        this.name = name;
    }

    public void setTarget(Identifier target) {
        this.target = target;
    }

    public void setPool(RegistryKey<StructurePool> pool) {
        this.pool = pool;
    }

    public void setFinalState(String finalState) {
        this.finalState = finalState;
    }

    public void setJoint(Joint joint) {
        this.joint = joint;
    }

    public void setPlacementPriority(int placementPriority) {
        this.placementPriority = placementPriority;
    }

    public void setSelectionPriority(int selectionPriority) {
        this.selectionPriority = selectionPriority;
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put(NAME_KEY, Identifier.CODEC, this.name);
        view.put(TARGET_KEY, Identifier.CODEC, this.target);
        view.put(POOL_KEY, STRUCTURE_POOL_KEY_CODEC, this.pool);
        view.putString(FINAL_STATE_KEY, this.finalState);
        view.put(JOINT_KEY, Joint.CODEC, this.joint);
        view.putInt(PLACEMENT_PRIORITY_KEY, this.placementPriority);
        view.putInt(SELECTION_PRIORITY_KEY, this.selectionPriority);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.name = view.read(NAME_KEY, Identifier.CODEC).orElse(DEFAULT_NAME);
        this.target = view.read(TARGET_KEY, Identifier.CODEC).orElse(DEFAULT_NAME);
        this.pool = view.read(POOL_KEY, STRUCTURE_POOL_KEY_CODEC).orElse(StructurePools.EMPTY);
        this.finalState = view.getString(FINAL_STATE_KEY, DEFAULT_FINAL_STATE);
        this.joint = view.read(JOINT_KEY, Joint.CODEC).orElseGet(() -> StructureTemplate.getJointFromFacing(this.getCachedState()));
        this.placementPriority = view.getInt(PLACEMENT_PRIORITY_KEY, 0);
        this.selectionPriority = view.getInt(SELECTION_PRIORITY_KEY, 0);
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    public void generate(ServerWorld world, int maxDepth, boolean keepJigsaws) {
        BlockPos lv = this.getPos().offset(this.getCachedState().get(JigsawBlock.ORIENTATION).getFacing());
        RegistryWrapper.Impl lv2 = world.getRegistryManager().getOrThrow(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv3 = lv2.getOrThrow(this.pool);
        StructurePoolBasedGenerator.generate(world, lv3, this.target, maxDepth, lv, keepJigsaws);
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }

    public static enum Joint implements StringIdentifiable
    {
        ROLLABLE("rollable"),
        ALIGNED("aligned");

        public static final StringIdentifiable.EnumCodec<Joint> CODEC;
        private final String name;

        private Joint(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public Text asText() {
            return Text.translatable("jigsaw_block.joint." + this.name);
        }

        static {
            CODEC = StringIdentifiable.createCodec(Joint::values);
        }
    }
}

