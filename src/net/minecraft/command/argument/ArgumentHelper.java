/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import net.minecraft.command.PermissionLevelPredicate;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;

public class ArgumentHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte MIN_FLAG = 1;
    private static final byte MAX_FLAG = 2;

    public static int getMinMaxFlag(boolean hasMin, boolean hasMax) {
        int i = 0;
        if (hasMin) {
            i |= 1;
        }
        if (hasMax) {
            i |= 2;
        }
        return i;
    }

    public static boolean hasMinFlag(byte flags) {
        return (flags & 1) != 0;
    }

    public static boolean hasMaxFlag(byte flags) {
        return (flags & 2) != 0;
    }

    private static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void writeArgumentTypeProperties(JsonObject json, ArgumentSerializer<A, T> serializer, ArgumentSerializer.ArgumentTypeProperties<A> properties) {
        serializer.writeJson(properties, json);
    }

    private static <T extends ArgumentType<?>> void writeArgument(JsonObject json, T argumentType) {
        ArgumentSerializer.ArgumentTypeProperties<T> lv = ArgumentTypes.getArgumentTypeProperties(argumentType);
        json.addProperty("type", "argument");
        json.addProperty("parser", String.valueOf(Registries.COMMAND_ARGUMENT_TYPE.getId(lv.getSerializer())));
        JsonObject jsonObject2 = new JsonObject();
        ArgumentHelper.writeArgumentTypeProperties(jsonObject2, lv.getSerializer(), lv);
        if (!jsonObject2.isEmpty()) {
            json.add("properties", jsonObject2);
        }
    }

    public static <S> JsonObject toJson(CommandDispatcher<S> dispatcher, CommandNode<S> node) {
        Collection<String> collection2;
        Iterator<CommandNode<S>> rootCommandNode;
        JsonObject jsonObject = new JsonObject();
        CommandNode<S> commandNode = node;
        Objects.requireNonNull(commandNode);
        CommandNode<S> commandNode2 = commandNode;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RootCommandNode.class, LiteralCommandNode.class, ArgumentCommandNode.class}, commandNode2, n)) {
            case 0: {
                rootCommandNode = (RootCommandNode)commandNode2;
                jsonObject.addProperty("type", "root");
                break;
            }
            case 1: {
                LiteralCommandNode literalCommandNode = (LiteralCommandNode)commandNode2;
                jsonObject.addProperty("type", "literal");
                break;
            }
            case 2: {
                ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)commandNode2;
                ArgumentHelper.writeArgument(jsonObject, argumentCommandNode.getType());
                break;
            }
            default: {
                LOGGER.error("Could not serialize node {} ({})!", (Object)node, (Object)node.getClass());
                jsonObject.addProperty("type", "unknown");
            }
        }
        Collection<CommandNode<S>> collection = node.getChildren();
        if (!collection.isEmpty()) {
            JsonObject jsonObject2 = new JsonObject();
            rootCommandNode = collection.iterator();
            while (rootCommandNode.hasNext()) {
                CommandNode<S> commandNode22 = rootCommandNode.next();
                jsonObject2.add(commandNode22.getName(), ArgumentHelper.toJson(dispatcher, commandNode22));
            }
            jsonObject.add("children", jsonObject2);
        }
        if (node.getCommand() != null) {
            jsonObject.addProperty("executable", true);
        }
        if ((rootCommandNode = node.getRequirement()) instanceof PermissionLevelPredicate) {
            PermissionLevelPredicate lv = (PermissionLevelPredicate)((Object)rootCommandNode);
            jsonObject.addProperty("required_level", lv.requiredLevel());
        }
        if (node.getRedirect() != null && !(collection2 = dispatcher.getPath(node.getRedirect())).isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            for (String string : collection2) {
                jsonArray.add(string);
            }
            jsonObject.add("redirect", jsonArray);
        }
        return jsonObject;
    }

    public static <T> Set<ArgumentType<?>> collectUsedArgumentTypes(CommandNode<T> rootNode) {
        ReferenceOpenHashSet<CommandNode<T>> set = new ReferenceOpenHashSet<CommandNode<T>>();
        HashSet set2 = new HashSet();
        ArgumentHelper.collectUsedArgumentTypes(rootNode, set2, set);
        return set2;
    }

    private static <T> void collectUsedArgumentTypes(CommandNode<T> node, Set<ArgumentType<?>> usedArgumentTypes, Set<CommandNode<T>> visitedNodes) {
        if (!visitedNodes.add(node)) {
            return;
        }
        if (node instanceof ArgumentCommandNode) {
            ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)node;
            usedArgumentTypes.add(argumentCommandNode.getType());
        }
        node.getChildren().forEach(child -> ArgumentHelper.collectUsedArgumentTypes(child, usedArgumentTypes, visitedNodes));
        CommandNode<T> commandNode2 = node.getRedirect();
        if (commandNode2 != null) {
            ArgumentHelper.collectUsedArgumentTypes(commandNode2, usedArgumentTypes, visitedNodes);
        }
    }
}

