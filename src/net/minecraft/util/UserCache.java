/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.util.NameToIdCache;
import net.minecraft.util.StringHelper;
import org.slf4j.Logger;

public class UserCache
implements NameToIdCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SAVED_ENTRIES = 1000;
    private static final int field_29789 = 1;
    private boolean offlineMode = true;
    private final Map<String, Entry> byName = new ConcurrentHashMap<String, Entry>();
    private final Map<UUID, Entry> byUuid = new ConcurrentHashMap<UUID, Entry>();
    private final GameProfileRepository profileRepository;
    private final Gson gson = new GsonBuilder().create();
    private final File cacheFile;
    private final AtomicLong accessCount = new AtomicLong();

    public UserCache(GameProfileRepository profileRepository, File cacheFile) {
        this.profileRepository = profileRepository;
        this.cacheFile = cacheFile;
        Lists.reverse(this.load()).forEach(this::add);
    }

    private void add(Entry entry) {
        PlayerConfigEntry lv = entry.getPlayer();
        entry.setLastAccessed(this.incrementAndGetAccessCount());
        this.byName.put(lv.name().toLowerCase(Locale.ROOT), entry);
        this.byUuid.put(lv.id(), entry);
    }

    private Optional<PlayerConfigEntry> findProfileByName(GameProfileRepository repository, String string) {
        if (!StringHelper.isValidPlayerName(string)) {
            return this.getOfflinePlayerProfile(string);
        }
        Optional<PlayerConfigEntry> optional = repository.findProfileByName(string).map(PlayerConfigEntry::new);
        if (optional.isEmpty()) {
            return this.getOfflinePlayerProfile(string);
        }
        return optional;
    }

    private Optional<PlayerConfigEntry> getOfflinePlayerProfile(String string) {
        if (this.offlineMode) {
            return Optional.of(PlayerConfigEntry.fromNickname(string));
        }
        return Optional.empty();
    }

    @Override
    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    @Override
    public void add(PlayerConfigEntry player) {
        this.addToCache(player);
    }

    private Entry addToCache(PlayerConfigEntry player) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(2, 1);
        Date date = calendar.getTime();
        Entry lv = new Entry(player, date);
        this.add(lv);
        this.save();
        return lv;
    }

    private long incrementAndGetAccessCount() {
        return this.accessCount.incrementAndGet();
    }

    @Override
    public Optional<PlayerConfigEntry> findByName(String name) {
        Optional<PlayerConfigEntry> optional;
        String string2 = name.toLowerCase(Locale.ROOT);
        Entry lv = this.byName.get(string2);
        boolean bl = false;
        if (lv != null && new Date().getTime() >= lv.expirationDate.getTime()) {
            this.byUuid.remove(lv.getPlayer().id());
            this.byName.remove(lv.getPlayer().name().toLowerCase(Locale.ROOT));
            bl = true;
            lv = null;
        }
        if (lv != null) {
            lv.setLastAccessed(this.incrementAndGetAccessCount());
            optional = Optional.of(lv.getPlayer());
        } else {
            Optional<PlayerConfigEntry> optional2 = this.findProfileByName(this.profileRepository, string2);
            if (optional2.isPresent()) {
                optional = Optional.of(this.addToCache(optional2.get()).getPlayer());
                bl = false;
            } else {
                optional = Optional.empty();
            }
        }
        if (bl) {
            this.save();
        }
        return optional;
    }

    @Override
    public Optional<PlayerConfigEntry> getByUuid(UUID uuid) {
        Entry lv = this.byUuid.get(uuid);
        if (lv == null) {
            return Optional.empty();
        }
        lv.setLastAccessed(this.incrementAndGetAccessCount());
        return Optional.of(lv.getPlayer());
    }

    private static DateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private List<Entry> load() {
        ArrayList<Entry> list = Lists.newArrayList();
        try (BufferedReader reader2222 = Files.newReader(this.cacheFile, StandardCharsets.UTF_8);){
            JsonArray jsonArray = this.gson.fromJson((Reader)reader2222, JsonArray.class);
            if (jsonArray == null) {
                ArrayList<Entry> arrayList = list;
                return arrayList;
            }
            DateFormat dateFormat = UserCache.getDateFormat();
            jsonArray.forEach(json -> UserCache.entryFromJson(json, dateFormat).ifPresent(list::add));
            return list;
        } catch (FileNotFoundException reader2222) {
            return list;
        } catch (JsonParseException | IOException exception) {
            LOGGER.warn("Failed to load profile cache {}", (Object)this.cacheFile, (Object)exception);
        }
        return list;
    }

    @Override
    public void save() {
        JsonArray jsonArray = new JsonArray();
        DateFormat dateFormat = UserCache.getDateFormat();
        this.getLastAccessedEntries(1000).forEach(entry -> jsonArray.add(UserCache.entryToJson(entry, dateFormat)));
        String string = this.gson.toJson(jsonArray);
        try (BufferedWriter writer = Files.newWriter(this.cacheFile, StandardCharsets.UTF_8);){
            writer.write(string);
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    private Stream<Entry> getLastAccessedEntries(int limit) {
        return ImmutableList.copyOf(this.byUuid.values()).stream().sorted(Comparator.comparing(Entry::getLastAccessed).reversed()).limit(limit);
    }

    private static JsonElement entryToJson(Entry entry, DateFormat dateFormat) {
        JsonObject jsonObject = new JsonObject();
        entry.getPlayer().write(jsonObject);
        jsonObject.addProperty("expiresOn", dateFormat.format(entry.getExpirationDate()));
        return jsonObject;
    }

    private static Optional<Entry> entryFromJson(JsonElement json, DateFormat dateFormat) {
        JsonElement jsonElement2;
        JsonObject jsonObject;
        PlayerConfigEntry lv;
        if (json.isJsonObject() && (lv = PlayerConfigEntry.read(jsonObject = json.getAsJsonObject())) != null && (jsonElement2 = jsonObject.get("expiresOn")) != null) {
            String string = jsonElement2.getAsString();
            try {
                Date date = dateFormat.parse(string);
                return Optional.of(new Entry(lv, date));
            } catch (ParseException parseException) {
                LOGGER.warn("Failed to parse date {}", (Object)string, (Object)parseException);
            }
        }
        return Optional.empty();
    }

    static class Entry {
        private final PlayerConfigEntry player;
        final Date expirationDate;
        private volatile long lastAccessed;

        Entry(PlayerConfigEntry player, Date expirationDate) {
            this.player = player;
            this.expirationDate = expirationDate;
        }

        public PlayerConfigEntry getPlayer() {
            return this.player;
        }

        public Date getExpirationDate() {
            return this.expirationDate;
        }

        public void setLastAccessed(long lastAccessed) {
            this.lastAccessed = lastAccessed;
        }

        public long getLastAccessed() {
            return this.lastAccessed;
        }
    }
}

