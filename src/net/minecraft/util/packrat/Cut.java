/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.packrat;

public interface Cut {
    public static final Cut NOOP = new Cut(){

        @Override
        public void cut() {
        }

        @Override
        public boolean isCut() {
            return false;
        }
    };

    public void cut();

    public boolean isCut();
}

