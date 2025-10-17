/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.NumberFormatTypes;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import org.jetbrains.annotations.Nullable;

public class ScoreboardObjective {
    private final Scoreboard scoreboard;
    private final String name;
    private final ScoreboardCriterion criterion;
    private Text displayName;
    private Text bracketedDisplayName;
    private ScoreboardCriterion.RenderType renderType;
    private boolean displayAutoUpdate;
    @Nullable
    private NumberFormat numberFormat;

    public ScoreboardObjective(Scoreboard scoreboard, String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType, boolean displayAutoUpdate, @Nullable NumberFormat numberFormat) {
        this.scoreboard = scoreboard;
        this.name = name;
        this.criterion = criterion;
        this.displayName = displayName;
        this.bracketedDisplayName = this.generateBracketedDisplayName();
        this.renderType = renderType;
        this.displayAutoUpdate = displayAutoUpdate;
        this.numberFormat = numberFormat;
    }

    public Packed pack() {
        return new Packed(this.name, this.criterion, this.displayName, this.renderType, this.displayAutoUpdate, Optional.ofNullable(this.numberFormat));
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public String getName() {
        return this.name;
    }

    public ScoreboardCriterion getCriterion() {
        return this.criterion;
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public boolean shouldDisplayAutoUpdate() {
        return this.displayAutoUpdate;
    }

    @Nullable
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    public NumberFormat getNumberFormatOr(NumberFormat format) {
        return Objects.requireNonNullElse(this.numberFormat, format);
    }

    private Text generateBracketedDisplayName() {
        return Texts.bracketed(this.displayName.copy().styled(style -> style.withHoverEvent(new HoverEvent.ShowText(Text.literal(this.name)))));
    }

    public Text toHoverableText() {
        return this.bracketedDisplayName;
    }

    public void setDisplayName(Text name) {
        this.displayName = name;
        this.bracketedDisplayName = this.generateBracketedDisplayName();
        this.scoreboard.updateExistingObjective(this);
    }

    public ScoreboardCriterion.RenderType getRenderType() {
        return this.renderType;
    }

    public void setRenderType(ScoreboardCriterion.RenderType renderType) {
        this.renderType = renderType;
        this.scoreboard.updateExistingObjective(this);
    }

    public void setDisplayAutoUpdate(boolean displayAutoUpdate) {
        this.displayAutoUpdate = displayAutoUpdate;
        this.scoreboard.updateExistingObjective(this);
    }

    public void setNumberFormat(@Nullable NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        this.scoreboard.updateExistingObjective(this);
    }

    public record Packed(String name, ScoreboardCriterion criteria, Text displayName, ScoreboardCriterion.RenderType renderType, boolean displayAutoUpdate, Optional<NumberFormat> numberFormat) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("Name")).forGetter(Packed::name), ScoreboardCriterion.CODEC.optionalFieldOf("CriteriaName", ScoreboardCriterion.DUMMY).forGetter(Packed::criteria), ((MapCodec)TextCodecs.CODEC.fieldOf("DisplayName")).forGetter(Packed::displayName), ScoreboardCriterion.RenderType.CODEC.optionalFieldOf("RenderType", ScoreboardCriterion.RenderType.INTEGER).forGetter(Packed::renderType), Codec.BOOL.optionalFieldOf("display_auto_update", false).forGetter(Packed::displayAutoUpdate), NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter(Packed::numberFormat)).apply((Applicative<Packed, ?>)instance, Packed::new));
    }
}

