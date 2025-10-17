/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.handler;

import java.lang.runtime.SwitchBootstraps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.RpcException;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandler;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.world.GameRules;

public class GameRuleManagementHandlerImpl
implements GameRuleManagementHandler {
    private final MinecraftDedicatedServer server;
    private final ManagementLogger logger;

    public GameRuleManagementHandlerImpl(MinecraftDedicatedServer server, ManagementLogger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public GameRuleRpcDispatcher.TypedRule updateRule(GameRuleRpcDispatcher.UntypedRule untypedRule, ManagementConnectionId remote) {
        GameRules.Rule<?> lv = this.getRule(untypedRule.key());
        String string = lv.serialize();
        if (lv instanceof GameRules.BooleanRule) {
            GameRules.BooleanRule lv2 = (GameRules.BooleanRule)lv;
            lv2.set(Boolean.parseBoolean(untypedRule.value()), this.server);
        } else if (lv instanceof GameRules.IntRule) {
            GameRules.IntRule lv3 = (GameRules.IntRule)lv;
            lv3.set(Integer.parseInt(untypedRule.value()), this.server);
        } else {
            throw new RpcException("Unknown rule type for key: " + untypedRule.key());
        }
        GameRuleRpcDispatcher.TypedRule lv4 = this.toTypedRule(untypedRule.key(), lv);
        this.logger.logAction(remote, "Game rule '{}' updated from '{}' to '{}'", lv4.key(), string, lv4.value());
        this.server.onGameRuleUpdated(untypedRule.key(), lv);
        return lv4;
    }

    @Override
    public <T extends GameRules.Rule<T>> T getRule(GameRules.Key<T> key) {
        return this.server.getGameRules().get(key);
    }

    @Override
    public GameRuleRpcDispatcher.TypedRule toTypedRule(String name, GameRules.Rule<?> gameRule) {
        GameRules.Rule<?> rule = gameRule;
        Objects.requireNonNull(rule);
        GameRules.Rule<?> rule2 = rule;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{GameRules.BooleanRule.class, GameRules.IntRule.class}, rule2, n)) {
            case 0 -> {
                GameRules.BooleanRule lv = (GameRules.BooleanRule)rule2;
                yield new GameRuleRpcDispatcher.TypedRule(name, String.valueOf(lv.get()), GameRuleRpcDispatcher.GameRuleType.BOOL);
            }
            case 1 -> {
                GameRules.IntRule lv2 = (GameRules.IntRule)rule2;
                yield new GameRuleRpcDispatcher.TypedRule(name, String.valueOf(lv2.get()), GameRuleRpcDispatcher.GameRuleType.INT);
            }
            default -> throw new RpcException("Unknown rule type");
        };
    }

    @Override
    public Stream<Map.Entry<GameRules.Key<?>, GameRules.Type<?>>> getRules() {
        FeatureSet lv = this.server.getSaveProperties().getLevelInfo().getDataConfiguration().enabledFeatures();
        return GameRules.streamAllRules(lv);
    }

    private Optional<GameRules.Key<?>> getRuleKey(String name) {
        Stream<Map.Entry<GameRules.Key<?>, GameRules.Type<?>>> stream = this.getRules();
        return stream.filter(entry -> ((GameRules.Key)entry.getKey()).getName().equals(name)).findFirst().map(Map.Entry::getKey);
    }

    private GameRules.Rule<?> getRule(String name) {
        GameRules.Key<?> lv = this.getRuleKey(name).orElseThrow(() -> new RpcException("Game rule '" + name + "' does not exist"));
        return this.server.getGameRules().get(lv);
    }
}

