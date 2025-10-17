/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.datafixers.util.Either;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.NameToIdCache;
import net.minecraft.util.StringHelper;

public interface GameProfileResolver {
    public Optional<GameProfile> getProfileByName(String var1);

    public Optional<GameProfile> getProfileById(UUID var1);

    default public Optional<GameProfile> getProfile(Either<String, UUID> either) {
        return either.map(this::getProfileByName, this::getProfileById);
    }

    public static class CachedSessionProfileResolver
    implements GameProfileResolver {
        private final LoadingCache<String, Optional<GameProfile>> nameCache;
        final LoadingCache<UUID, Optional<GameProfile>> idCache;

        public CachedSessionProfileResolver(final MinecraftSessionService sessionService, final NameToIdCache cache) {
            this.idCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build(new CacheLoader<UUID, Optional<GameProfile>>(this){

                @Override
                public Optional<GameProfile> load(UUID uUID) {
                    ProfileResult profileResult = sessionService.fetchProfile(uUID, true);
                    return Optional.ofNullable(profileResult).map(ProfileResult::profile);
                }

                @Override
                public /* synthetic */ Object load(Object id) throws Exception {
                    return this.load((UUID)id);
                }
            });
            this.nameCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build(new CacheLoader<String, Optional<GameProfile>>(){

                @Override
                public Optional<GameProfile> load(String string) {
                    return cache.findByName(string).flatMap(entry -> idCache.getUnchecked(entry.id()));
                }

                @Override
                public /* synthetic */ Object load(Object name) throws Exception {
                    return this.load((String)name);
                }
            });
        }

        @Override
        public Optional<GameProfile> getProfileByName(String name) {
            if (StringHelper.isValidPlayerName(name)) {
                return this.nameCache.getUnchecked(name);
            }
            return Optional.empty();
        }

        @Override
        public Optional<GameProfile> getProfileById(UUID id) {
            return this.idCache.getUnchecked(id);
        }
    }
}

