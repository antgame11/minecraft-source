/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.util.UploadProgress;

@Environment(value=EnvType.CLIENT)
public interface UploadProgressTracker {
    public UploadProgress getUploadProgress();

    public void updateProgressDisplay();

    public static UploadProgressTracker create() {
        return new UploadProgressTracker(){
            private final UploadProgress progress = new UploadProgress();

            @Override
            public UploadProgress getUploadProgress() {
                return this.progress;
            }

            @Override
            public void updateProgressDisplay() {
            }
        };
    }
}

