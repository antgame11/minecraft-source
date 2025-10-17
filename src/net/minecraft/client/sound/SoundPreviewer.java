/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public final class SoundPreviewer {
    @Nullable
    private static SoundInstance currentSoundPreview;
    @Nullable
    private static SoundCategory category;

    public static void preview(SoundManager manager, SoundCategory category, float pitch) {
        SoundPreviewer.stopPreviewOfOtherCategory(manager, category);
        if (SoundPreviewer.canPlaySound(manager)) {
            SoundEvent lv;
            switch (category) {
                case RECORDS: {
                    SoundEvent soundEvent = SoundEvents.BLOCK_NOTE_BLOCK_GUITAR.value();
                    break;
                }
                case WEATHER: {
                    SoundEvent soundEvent = SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER;
                    break;
                }
                case BLOCKS: {
                    SoundEvent soundEvent = SoundEvents.BLOCK_GRASS_PLACE;
                    break;
                }
                case HOSTILE: {
                    SoundEvent soundEvent = SoundEvents.ENTITY_ZOMBIE_AMBIENT;
                    break;
                }
                case NEUTRAL: {
                    SoundEvent soundEvent = SoundEvents.ENTITY_COW_AMBIENT;
                    break;
                }
                case PLAYERS: {
                    SoundEvent soundEvent = SoundEvents.ENTITY_GENERIC_EAT.value();
                    break;
                }
                case AMBIENT: {
                    SoundEvent soundEvent = SoundEvents.AMBIENT_CAVE.value();
                    break;
                }
                case UI: {
                    SoundEvent soundEvent = SoundEvents.UI_BUTTON_CLICK.value();
                    break;
                }
                default: {
                    SoundEvent soundEvent = lv = SoundEvents.INTENTIONALLY_EMPTY;
                }
            }
            if (lv != SoundEvents.INTENTIONALLY_EMPTY) {
                currentSoundPreview = PositionedSoundInstance.master(lv, 1.0f, pitch);
                manager.play(currentSoundPreview);
            }
        }
    }

    private static void stopPreviewOfOtherCategory(SoundManager manager, SoundCategory category) {
        if (SoundPreviewer.category != category) {
            SoundPreviewer.category = category;
            if (currentSoundPreview != null) {
                manager.stop(currentSoundPreview);
            }
        }
    }

    private static boolean canPlaySound(SoundManager manager) {
        return currentSoundPreview == null || !manager.isPlaying(currentSoundPreview);
    }
}

