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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.Nullable;

public interface HoverEvent {
    public static final Codec<HoverEvent> CODEC = Action.CODEC.dispatch("action", HoverEvent::getAction, action -> action.codec);

    public Action getAction();

    public static enum Action implements StringIdentifiable
    {
        SHOW_TEXT("show_text", true, ShowText.CODEC),
        SHOW_ITEM("show_item", true, ShowItem.CODEC),
        SHOW_ENTITY("show_entity", true, ShowEntity.CODEC);

        public static final Codec<Action> UNVALIDATED_CODEC;
        public static final Codec<Action> CODEC;
        private final String name;
        private final boolean parsable;
        final MapCodec<? extends HoverEvent> codec;

        private Action(String name, boolean parsable, MapCodec<? extends HoverEvent> codec) {
            this.name = name;
            this.parsable = parsable;
            this.codec = codec;
        }

        public boolean isParsable() {
            return this.parsable;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<Action> validate(Action action) {
            if (!action.isParsable()) {
                return DataResult.error(() -> "Action not allowed: " + String.valueOf(action));
            }
            return DataResult.success(action, Lifecycle.stable());
        }

        static {
            UNVALIDATED_CODEC = StringIdentifiable.createBasicCodec(Action::values);
            CODEC = UNVALIDATED_CODEC.validate(Action::validate);
        }
    }

    public static class EntityContent {
        public static final MapCodec<EntityContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Registries.ENTITY_TYPE.getCodec().fieldOf("id")).forGetter(content -> content.entityType), ((MapCodec)Uuids.STRICT_CODEC.fieldOf("uuid")).forGetter(content -> content.uuid), TextCodecs.CODEC.optionalFieldOf("name").forGetter(content -> content.name)).apply((Applicative<EntityContent, ?>)instance, EntityContent::new));
        public final EntityType<?> entityType;
        public final UUID uuid;
        public final Optional<Text> name;
        @Nullable
        private List<Text> tooltip;

        public EntityContent(EntityType<?> entityType, UUID uuid, @Nullable Text name) {
            this(entityType, uuid, Optional.ofNullable(name));
        }

        public EntityContent(EntityType<?> entityType, UUID uuid, Optional<Text> name) {
            this.entityType = entityType;
            this.uuid = uuid;
            this.name = name;
        }

        public List<Text> asTooltip() {
            if (this.tooltip == null) {
                this.tooltip = new ArrayList<Text>();
                this.name.ifPresent(this.tooltip::add);
                this.tooltip.add(Text.translatable("gui.entity_tooltip.type", this.entityType.getName()));
                this.tooltip.add(Text.literal(this.uuid.toString()));
            }
            return this.tooltip;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            EntityContent lv = (EntityContent)o;
            return this.entityType.equals(lv.entityType) && this.uuid.equals(lv.uuid) && this.name.equals(lv.name);
        }

        public int hashCode() {
            int i = this.entityType.hashCode();
            i = 31 * i + this.uuid.hashCode();
            i = 31 * i + this.name.hashCode();
            return i;
        }
    }

    public record ShowEntity(EntityContent entity) implements HoverEvent
    {
        public static final MapCodec<ShowEntity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(EntityContent.CODEC.forGetter(ShowEntity::entity)).apply((Applicative<ShowEntity, ?>)instance, ShowEntity::new));

        @Override
        public Action getAction() {
            return Action.SHOW_ENTITY;
        }
    }

    public record ShowItem(ItemStack item) implements HoverEvent
    {
        public static final MapCodec<ShowItem> CODEC = ItemStack.MAP_CODEC.xmap(ShowItem::new, ShowItem::item);

        public ShowItem(ItemStack stack) {
            this.item = stack = stack.copy();
        }

        @Override
        public Action getAction() {
            return Action.SHOW_ITEM;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ShowItem)) return false;
            ShowItem lv = (ShowItem)o;
            if (!ItemStack.areEqual(this.item, lv.item)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return ItemStack.hashCode(this.item);
        }
    }

    public record ShowText(Text value) implements HoverEvent
    {
        public static final MapCodec<ShowText> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)TextCodecs.CODEC.fieldOf("value")).forGetter(ShowText::value)).apply((Applicative<ShowText, ?>)instance, ShowText::new));

        @Override
        public Action getAction() {
            return Action.SHOW_TEXT;
        }
    }
}

