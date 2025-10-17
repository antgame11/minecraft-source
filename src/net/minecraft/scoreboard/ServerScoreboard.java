/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreResetS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.waypoint.ServerWaypoint;
import org.jetbrains.annotations.Nullable;

public class ServerScoreboard
extends Scoreboard {
    public static final PersistentStateType<ScoreboardState> STATE_TYPE = new PersistentStateType<ScoreboardState>("scoreboard", context -> context.getWorldOrThrow().getScoreboard().createState(), context -> {
        ServerScoreboard lv = context.getWorldOrThrow().getScoreboard();
        return ScoreboardState.Packed.CODEC.xmap(lv::unpackState, ScoreboardState::pack);
    }, DataFixTypes.SAVED_DATA_SCOREBOARD);
    private final MinecraftServer server;
    private final Set<ScoreboardObjective> syncableObjectives = Sets.newHashSet();
    private final List<Runnable> updateListeners = Lists.newArrayList();

    public ServerScoreboard(MinecraftServer server) {
        this.server = server;
    }

    @Override
    protected void updateScore(ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score) {
        super.updateScore(scoreHolder, objective, score);
        if (this.syncableObjectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardScoreUpdateS2CPacket(scoreHolder.getNameForScoreboard(), objective.getName(), score.getScore(), Optional.ofNullable(score.getDisplayText()), Optional.ofNullable(score.getNumberFormat())));
        }
        this.runUpdateListeners();
    }

    @Override
    protected void resetScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        super.resetScore(scoreHolder, objective);
        this.runUpdateListeners();
    }

    @Override
    public void onScoreHolderRemoved(ScoreHolder scoreHolder) {
        super.onScoreHolderRemoved(scoreHolder);
        this.server.getPlayerManager().sendToAll(new ScoreboardScoreResetS2CPacket(scoreHolder.getNameForScoreboard(), null));
        this.runUpdateListeners();
    }

    @Override
    public void onScoreRemoved(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        super.onScoreRemoved(scoreHolder, objective);
        if (this.syncableObjectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardScoreResetS2CPacket(scoreHolder.getNameForScoreboard(), objective.getName()));
        }
        this.runUpdateListeners();
    }

    @Override
    public void setObjectiveSlot(ScoreboardDisplaySlot slot, @Nullable ScoreboardObjective objective) {
        ScoreboardObjective lv = this.getObjectiveForSlot(slot);
        super.setObjectiveSlot(slot, objective);
        if (lv != objective && lv != null) {
            if (this.countDisplaySlots(lv) > 0) {
                this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
            } else {
                this.stopSyncing(lv);
            }
        }
        if (objective != null) {
            if (this.syncableObjectives.contains(objective)) {
                this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
            } else {
                this.startSyncing(objective);
            }
        }
        this.runUpdateListeners();
    }

    @Override
    public boolean addScoreHolderToTeam(String scoreHolderName, Team team) {
        if (super.addScoreHolderToTeam(scoreHolderName, team)) {
            this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, scoreHolderName, TeamS2CPacket.Operation.ADD));
            this.refreshWaypointTrackingFor(scoreHolderName);
            this.runUpdateListeners();
            return true;
        }
        return false;
    }

    @Override
    public void removeScoreHolderFromTeam(String scoreHolderName, Team team) {
        super.removeScoreHolderFromTeam(scoreHolderName, team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, scoreHolderName, TeamS2CPacket.Operation.REMOVE));
        this.refreshWaypointTrackingFor(scoreHolderName);
        this.runUpdateListeners();
    }

    @Override
    public void updateObjective(ScoreboardObjective objective) {
        super.updateObjective(objective);
        this.runUpdateListeners();
    }

    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        super.updateExistingObjective(objective);
        if (this.syncableObjectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE));
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        super.updateRemovedObjective(objective);
        if (this.syncableObjectives.contains(objective)) {
            this.stopSyncing(objective);
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        super.updateScoreboardTeamAndPlayers(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, true));
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeam(Team team) {
        super.updateScoreboardTeam(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, false));
        this.refreshWaypointTrackingFor(team);
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedTeam(Team team) {
        super.updateRemovedTeam(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(team));
        this.refreshWaypointTrackingFor(team);
        this.runUpdateListeners();
    }

    public void addUpdateListener(Runnable listener) {
        this.updateListeners.add(listener);
    }

    protected void runUpdateListeners() {
        for (Runnable runnable : this.updateListeners) {
            runnable.run();
        }
    }

    public List<Packet<?>> createChangePackets(ScoreboardObjective objective) {
        ArrayList<Packet<?>> list = Lists.newArrayList();
        list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.ADD_MODE));
        for (ScoreboardDisplaySlot lv : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(lv) != objective) continue;
            list.add(new ScoreboardDisplayS2CPacket(lv, objective));
        }
        for (ScoreboardEntry lv2 : this.getScoreboardEntries(objective)) {
            list.add(new ScoreboardScoreUpdateS2CPacket(lv2.owner(), objective.getName(), lv2.value(), Optional.ofNullable(lv2.display()), Optional.ofNullable(lv2.numberFormatOverride())));
        }
        return list;
    }

    public void startSyncing(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createChangePackets(objective);
        for (ServerPlayerEntity lv : this.server.getPlayerManager().getPlayerList()) {
            for (Packet<?> lv2 : list) {
                lv.networkHandler.sendPacket(lv2);
            }
        }
        this.syncableObjectives.add(objective);
    }

    public List<Packet<?>> createRemovePackets(ScoreboardObjective objective) {
        ArrayList<Packet<?>> list = Lists.newArrayList();
        list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE));
        for (ScoreboardDisplaySlot lv : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(lv) != objective) continue;
            list.add(new ScoreboardDisplayS2CPacket(lv, objective));
        }
        return list;
    }

    public void stopSyncing(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createRemovePackets(objective);
        for (ServerPlayerEntity lv : this.server.getPlayerManager().getPlayerList()) {
            for (Packet<?> lv2 : list) {
                lv.networkHandler.sendPacket(lv2);
            }
        }
        this.syncableObjectives.remove(objective);
    }

    public int countDisplaySlots(ScoreboardObjective objective) {
        int i = 0;
        for (ScoreboardDisplaySlot lv : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(lv) != objective) continue;
            ++i;
        }
        return i;
    }

    private ScoreboardState createState() {
        ScoreboardState lv = new ScoreboardState(this);
        this.addUpdateListener(lv::markDirty);
        return lv;
    }

    private ScoreboardState unpackState(ScoreboardState.Packed packedState) {
        ScoreboardState lv = this.createState();
        lv.unpack(packedState);
        return lv;
    }

    private void refreshWaypointTrackingFor(String playerName) {
        ServerWorld serverWorld;
        ServerPlayerEntity lv = this.server.getPlayerManager().getPlayer(playerName);
        if (lv != null && (serverWorld = lv.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld lv2 = serverWorld;
            lv2.getWaypointHandler().refreshTracking(lv);
        }
    }

    private void refreshWaypointTrackingFor(Team team) {
        for (ServerWorld lv : this.server.getWorlds()) {
            team.getPlayerList().stream().map(playerName -> this.server.getPlayerManager().getPlayer((String)playerName)).filter(Objects::nonNull).forEach(player -> lv.getWaypointHandler().refreshTracking((ServerWaypoint)player));
        }
    }
}

