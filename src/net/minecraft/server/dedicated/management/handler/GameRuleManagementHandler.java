/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.world.GameRules;

public interface GameRuleManagementHandler {
    public GameRuleRpcDispatcher.TypedRule updateRule(GameRuleRpcDispatcher.UntypedRule var1, ManagementConnectionId var2);

    public <T extends GameRules.Rule<T>> T getRule(GameRules.Key<T> var1);

    public GameRuleRpcDispatcher.TypedRule toTypedRule(String var1, GameRules.Rule<?> var2);

    public Stream<Map.Entry<GameRules.Key<?>, GameRules.Type<?>>> getRules();
}

