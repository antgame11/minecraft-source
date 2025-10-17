/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class Team
extends AbstractTeam {
    private static final int field_31884 = 0;
    private static final int field_31885 = 1;
    private final Scoreboard scoreboard;
    private final String name;
    private final Set<String> playerList = Sets.newHashSet();
    private Text displayName;
    private Text prefix = ScreenTexts.EMPTY;
    private Text suffix = ScreenTexts.EMPTY;
    private boolean friendlyFire = true;
    private boolean showFriendlyInvisibles = true;
    private AbstractTeam.VisibilityRule nameTagVisibilityRule = AbstractTeam.VisibilityRule.ALWAYS;
    private AbstractTeam.VisibilityRule deathMessageVisibilityRule = AbstractTeam.VisibilityRule.ALWAYS;
    private Formatting color = Formatting.RESET;
    private AbstractTeam.CollisionRule collisionRule = AbstractTeam.CollisionRule.ALWAYS;
    private final Style nameStyle;

    public Team(Scoreboard scoreboard, String name) {
        this.scoreboard = scoreboard;
        this.name = name;
        this.displayName = Text.literal(name);
        this.nameStyle = Style.EMPTY.withInsertion(name).withHoverEvent(new HoverEvent.ShowText(Text.literal(name)));
    }

    public Packed pack() {
        return new Packed(this.name, Optional.of(this.displayName), this.color != Formatting.RESET ? Optional.of(this.color) : Optional.empty(), this.friendlyFire, this.showFriendlyInvisibles, this.prefix, this.suffix, this.nameTagVisibilityRule, this.deathMessageVisibilityRule, this.collisionRule, List.copyOf(this.playerList));
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public MutableText getFormattedName() {
        MutableText lv = Texts.bracketed(this.displayName.copy().fillStyle(this.nameStyle));
        Formatting lv2 = this.getColor();
        if (lv2 != Formatting.RESET) {
            lv.formatted(lv2);
        }
        return lv;
    }

    public void setDisplayName(Text displayName) {
        if (displayName == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.displayName = displayName;
        this.scoreboard.updateScoreboardTeam(this);
    }

    public void setPrefix(@Nullable Text prefix) {
        this.prefix = prefix == null ? ScreenTexts.EMPTY : prefix;
        this.scoreboard.updateScoreboardTeam(this);
    }

    public Text getPrefix() {
        return this.prefix;
    }

    public void setSuffix(@Nullable Text suffix) {
        this.suffix = suffix == null ? ScreenTexts.EMPTY : suffix;
        this.scoreboard.updateScoreboardTeam(this);
    }

    public Text getSuffix() {
        return this.suffix;
    }

    @Override
    public Collection<String> getPlayerList() {
        return this.playerList;
    }

    @Override
    public MutableText decorateName(Text name) {
        MutableText lv = Text.empty().append(this.prefix).append(name).append(this.suffix);
        Formatting lv2 = this.getColor();
        if (lv2 != Formatting.RESET) {
            lv.formatted(lv2);
        }
        return lv;
    }

    public static MutableText decorateName(@Nullable AbstractTeam team, Text name) {
        if (team == null) {
            return name.copy();
        }
        return team.decorateName(name);
    }

    @Override
    public boolean isFriendlyFireAllowed() {
        return this.friendlyFire;
    }

    public void setFriendlyFireAllowed(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
        this.scoreboard.updateScoreboardTeam(this);
    }

    @Override
    public boolean shouldShowFriendlyInvisibles() {
        return this.showFriendlyInvisibles;
    }

    public void setShowFriendlyInvisibles(boolean showFriendlyInvisible) {
        this.showFriendlyInvisibles = showFriendlyInvisible;
        this.scoreboard.updateScoreboardTeam(this);
    }

    @Override
    public AbstractTeam.VisibilityRule getNameTagVisibilityRule() {
        return this.nameTagVisibilityRule;
    }

    @Override
    public AbstractTeam.VisibilityRule getDeathMessageVisibilityRule() {
        return this.deathMessageVisibilityRule;
    }

    public void setNameTagVisibilityRule(AbstractTeam.VisibilityRule nameTagVisibilityRule) {
        this.nameTagVisibilityRule = nameTagVisibilityRule;
        this.scoreboard.updateScoreboardTeam(this);
    }

    public void setDeathMessageVisibilityRule(AbstractTeam.VisibilityRule deathMessageVisibilityRule) {
        this.deathMessageVisibilityRule = deathMessageVisibilityRule;
        this.scoreboard.updateScoreboardTeam(this);
    }

    @Override
    public AbstractTeam.CollisionRule getCollisionRule() {
        return this.collisionRule;
    }

    public void setCollisionRule(AbstractTeam.CollisionRule collisionRule) {
        this.collisionRule = collisionRule;
        this.scoreboard.updateScoreboardTeam(this);
    }

    public int getFriendlyFlagsBitwise() {
        int i = 0;
        if (this.isFriendlyFireAllowed()) {
            i |= 1;
        }
        if (this.shouldShowFriendlyInvisibles()) {
            i |= 2;
        }
        return i;
    }

    public void setFriendlyFlagsBitwise(int flags) {
        this.setFriendlyFireAllowed((flags & 1) > 0);
        this.setShowFriendlyInvisibles((flags & 2) > 0);
    }

    public void setColor(Formatting color) {
        this.color = color;
        this.scoreboard.updateScoreboardTeam(this);
    }

    @Override
    public Formatting getColor() {
        return this.color;
    }

    public record Packed(String name, Optional<Text> displayName, Optional<Formatting> color, boolean allowFriendlyFire, boolean seeFriendlyInvisibles, Text memberNamePrefix, Text memberNameSuffix, AbstractTeam.VisibilityRule nameTagVisibility, AbstractTeam.VisibilityRule deathMessageVisibility, AbstractTeam.CollisionRule collisionRule, List<String> players) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("Name")).forGetter(Packed::name), TextCodecs.CODEC.optionalFieldOf("DisplayName").forGetter(Packed::displayName), Formatting.COLOR_CODEC.optionalFieldOf("TeamColor").forGetter(Packed::color), Codec.BOOL.optionalFieldOf("AllowFriendlyFire", true).forGetter(Packed::allowFriendlyFire), Codec.BOOL.optionalFieldOf("SeeFriendlyInvisibles", true).forGetter(Packed::seeFriendlyInvisibles), TextCodecs.CODEC.optionalFieldOf("MemberNamePrefix", ScreenTexts.EMPTY).forGetter(Packed::memberNamePrefix), TextCodecs.CODEC.optionalFieldOf("MemberNameSuffix", ScreenTexts.EMPTY).forGetter(Packed::memberNameSuffix), AbstractTeam.VisibilityRule.CODEC.optionalFieldOf("NameTagVisibility", AbstractTeam.VisibilityRule.ALWAYS).forGetter(Packed::nameTagVisibility), AbstractTeam.VisibilityRule.CODEC.optionalFieldOf("DeathMessageVisibility", AbstractTeam.VisibilityRule.ALWAYS).forGetter(Packed::deathMessageVisibility), AbstractTeam.CollisionRule.CODEC.optionalFieldOf("CollisionRule", AbstractTeam.CollisionRule.ALWAYS).forGetter(Packed::collisionRule), Codec.STRING.listOf().optionalFieldOf("Players", List.of()).forGetter(Packed::players)).apply((Applicative<Packed, ?>)instance, Packed::new));
    }
}

