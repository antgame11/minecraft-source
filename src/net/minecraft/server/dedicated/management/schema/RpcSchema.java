/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.schema;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.server.dedicated.management.UriUtil;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.schema.RpcSchemaEntry;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public record RpcSchema(Optional<URI> reference, Optional<String> type, Optional<RpcSchema> items, Optional<Map<String, RpcSchema>> properties, Optional<List<String>> enumValues) {
    public static final Codec<RpcSchema> CODEC = Codec.recursive("Schema", codec -> RecordCodecBuilder.create(instance -> instance.group(UriUtil.URI_CODEC.optionalFieldOf("$ref").forGetter(RpcSchema::reference), Codec.STRING.optionalFieldOf("type").forGetter(RpcSchema::type), codec.optionalFieldOf("items").forGetter(RpcSchema::items), Codec.unboundedMap(Codec.STRING, codec).optionalFieldOf("properties").forGetter(RpcSchema::properties), Codec.STRING.listOf().optionalFieldOf("enum").forGetter(RpcSchema::enumValues)).apply((Applicative<RpcSchema, ?>)instance, RpcSchema::new)));
    private static final List<RpcSchemaEntry> REGISTERED_SCHEMAS = new ArrayList<RpcSchemaEntry>();
    public static final RpcSchema BOOLEAN = RpcSchema.ofLiteral("boolean");
    public static final RpcSchema INTEGER = RpcSchema.ofLiteral("integer");
    public static final RpcSchema NUMBER = RpcSchema.ofLiteral("number");
    public static final RpcSchema STRING;
    public static final RpcSchema PLAYER_ID;
    public static final RpcSchemaEntry DIFFICULTY;
    public static final RpcSchemaEntry GAME_MODE;
    public static final RpcSchemaEntry PLAYER;
    public static final RpcSchemaEntry VERSION;
    public static final RpcSchemaEntry SERVER_STATE;
    public static final RpcSchema GAME_RULE_TYPE;
    public static final RpcSchemaEntry TYPED_GAME_RULE;
    public static final RpcSchemaEntry UNTYPED_GAME_RULE;
    public static final RpcSchemaEntry MESSAGE;
    public static final RpcSchemaEntry SYSTEM_MESSAGE;
    public static final RpcSchemaEntry KICK_PLAYER;
    public static final RpcSchemaEntry OPERATOR;
    public static final RpcSchemaEntry INCOMING_IP_BAN;
    public static final RpcSchemaEntry IP_BAN;
    public static final RpcSchemaEntry USER_BAN;

    private static RpcSchemaEntry registerEntry(String reference, RpcSchema schema) {
        RpcSchemaEntry lv = new RpcSchemaEntry(reference, UriUtil.createSchemasUri(reference), schema);
        REGISTERED_SCHEMAS.add(lv);
        return lv;
    }

    public static List<RpcSchemaEntry> getRegisteredSchemas() {
        return REGISTERED_SCHEMAS;
    }

    public static RpcSchema ofReference(URI reference) {
        return new RpcSchema(Optional.of(reference), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static RpcSchema ofLiteral(String literal) {
        return new RpcSchema(Optional.empty(), Optional.of(literal), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static <E extends Enum<E>> RpcSchema ofEnum(Supplier<E[]> values) {
        List<String> list = Stream.of((Enum[])values.get()).map(value -> ((StringIdentifiable)value).asString()).toList();
        return RpcSchema.ofList(list);
    }

    public static RpcSchema ofList(List<String> values) {
        return new RpcSchema(Optional.empty(), Optional.of("string"), Optional.empty(), Optional.empty(), Optional.of(values));
    }

    public static RpcSchema ofArray(RpcSchema itemSchema) {
        return new RpcSchema(Optional.empty(), Optional.of("array"), Optional.of(itemSchema), Optional.empty(), Optional.empty());
    }

    public static RpcSchema ofObject() {
        return new RpcSchema(Optional.empty(), Optional.of("object"), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static RpcSchema ofObjectWithProperties(Map<String, RpcSchema> itemSchemaMap) {
        return new RpcSchema(Optional.empty(), Optional.of("object"), Optional.empty(), Optional.of(itemSchemaMap), Optional.empty());
    }

    public RpcSchema withProperty(String reference, RpcSchema schema) {
        HashMap<String, RpcSchema> hashMap = new HashMap<String, RpcSchema>();
        this.properties.ifPresent(hashMap::putAll);
        hashMap.put(reference, schema);
        return RpcSchema.ofObjectWithProperties(hashMap);
    }

    public RpcSchema asArray() {
        return RpcSchema.ofArray(this);
    }

    static {
        PLAYER_ID = STRING = RpcSchema.ofLiteral("string");
        DIFFICULTY = RpcSchema.registerEntry("difficulty", RpcSchema.ofEnum(Difficulty::values));
        GAME_MODE = RpcSchema.registerEntry("game_type", RpcSchema.ofEnum(GameMode::values));
        PLAYER = RpcSchema.registerEntry("player", RpcSchema.ofObject().withProperty("id", PLAYER_ID).withProperty("name", STRING));
        VERSION = RpcSchema.registerEntry("version", RpcSchema.ofObject().withProperty("name", STRING).withProperty("protocol", INTEGER));
        SERVER_STATE = RpcSchema.registerEntry("server_state", RpcSchema.ofObject().withProperty("started", BOOLEAN).withProperty("players", PLAYER.ref().asArray()).withProperty("version", VERSION.ref()));
        GAME_RULE_TYPE = RpcSchema.ofEnum(GameRuleRpcDispatcher.GameRuleType::values);
        TYPED_GAME_RULE = RpcSchema.registerEntry("typed_game_rule", RpcSchema.ofObject().withProperty("key", STRING).withProperty("value", STRING).withProperty("type", GAME_RULE_TYPE));
        UNTYPED_GAME_RULE = RpcSchema.registerEntry("untyped_game_rule", RpcSchema.ofObject().withProperty("key", STRING).withProperty("value", STRING));
        MESSAGE = RpcSchema.registerEntry("message", RpcSchema.ofObject().withProperty("literal", STRING).withProperty("translatable", STRING).withProperty("translatableParams", STRING.asArray()));
        SYSTEM_MESSAGE = RpcSchema.registerEntry("system_message", RpcSchema.ofObject().withProperty("message", MESSAGE.ref()).withProperty("overlay", BOOLEAN).withProperty("receivingPlayers", PLAYER.ref().asArray()));
        KICK_PLAYER = RpcSchema.registerEntry("kick_player", RpcSchema.ofObject().withProperty("message", MESSAGE.ref()).withProperty("player", PLAYER.ref()));
        OPERATOR = RpcSchema.registerEntry("operator", RpcSchema.ofObject().withProperty("player", PLAYER.ref()).withProperty("bypassesPlayerLimit", BOOLEAN).withProperty("permissionLevel", INTEGER));
        INCOMING_IP_BAN = RpcSchema.registerEntry("incoming_ip_ban", RpcSchema.ofObject().withProperty("player", PLAYER.ref()).withProperty("ip", STRING).withProperty("reason", STRING).withProperty("source", STRING).withProperty("expires", STRING));
        IP_BAN = RpcSchema.registerEntry("ip_ban", RpcSchema.ofObject().withProperty("ip", STRING).withProperty("reason", STRING).withProperty("source", STRING).withProperty("expires", STRING));
        USER_BAN = RpcSchema.registerEntry("user_ban", RpcSchema.ofObject().withProperty("player", PLAYER.ref()).withProperty("reason", STRING).withProperty("source", STRING).withProperty("expires", STRING));
    }
}

