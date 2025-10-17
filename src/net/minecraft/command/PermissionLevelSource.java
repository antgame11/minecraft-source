/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import net.minecraft.command.PermissionLevelPredicate;

public interface PermissionLevelSource {
    public boolean hasPermissionLevel(int var1);

    default public boolean hasElevatedPermissions() {
        return this.hasPermissionLevel(2);
    }

    public record PermissionLevelSourcePredicate<T extends PermissionLevelSource>(int requiredLevel) implements PermissionLevelPredicate<T>
    {
        @Override
        public boolean test(T arg) {
            return arg.hasPermissionLevel(this.requiredLevel);
        }

        @Override
        public /* synthetic */ boolean test(Object permissionLevelSource) {
            return this.test((T)((PermissionLevelSource)permissionLevelSource));
        }
    }
}

