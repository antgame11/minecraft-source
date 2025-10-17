/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.PersistentState;

public class ScoreboardState
extends PersistentState {
    public static final String SCOREBOARD_KEY = "scoreboard";
    private final Scoreboard scoreboard;

    public ScoreboardState(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public void unpack(Packed packed) {
        packed.objectives().forEach(this.scoreboard::addObjective);
        packed.scores().forEach(this.scoreboard::addEntry);
        packed.displaySlots().forEach((slot, objectiveName) -> {
            ScoreboardObjective lv = this.scoreboard.getNullableObjective((String)objectiveName);
            this.scoreboard.setObjectiveSlot((ScoreboardDisplaySlot)slot, lv);
        });
        packed.teams().forEach(this.scoreboard::addTeam);
    }

    public Packed pack() {
        EnumMap<ScoreboardDisplaySlot, String> map = new EnumMap<ScoreboardDisplaySlot, String>(ScoreboardDisplaySlot.class);
        for (ScoreboardDisplaySlot lv : ScoreboardDisplaySlot.values()) {
            ScoreboardObjective lv2 = this.scoreboard.getObjectiveForSlot(lv);
            if (lv2 == null) continue;
            map.put(lv, lv2.getName());
        }
        return new Packed(this.scoreboard.getObjectives().stream().map(ScoreboardObjective::pack).toList(), this.scoreboard.pack(), map, this.scoreboard.getTeams().stream().map(Team::pack).toList());
    }

    public record Packed(List<ScoreboardObjective.Packed> objectives, List<Scoreboard.PackedEntry> scores, Map<ScoreboardDisplaySlot, String> displaySlots, List<Team.Packed> teams) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group(ScoreboardObjective.Packed.CODEC.listOf().optionalFieldOf("Objectives", List.of()).forGetter(Packed::objectives), Scoreboard.PackedEntry.CODEC.listOf().optionalFieldOf("PlayerScores", List.of()).forGetter(Packed::scores), Codec.unboundedMap(ScoreboardDisplaySlot.CODEC, Codec.STRING).optionalFieldOf("DisplaySlots", Map.of()).forGetter(Packed::displaySlots), Team.Packed.CODEC.listOf().optionalFieldOf("Teams", List.of()).forGetter(Packed::teams)).apply((Applicative<Packed, ?>)instance, Packed::new));
    }
}

