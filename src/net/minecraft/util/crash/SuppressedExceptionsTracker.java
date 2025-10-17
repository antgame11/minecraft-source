/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.crash;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Queue;
import net.minecraft.util.collection.ArrayListDeque;

public class SuppressedExceptionsTracker {
    private static final int MAX_QUEUE_SIZE = 8;
    private final Queue<Entry> queue = new ArrayListDeque<Entry>();
    private final Object2IntLinkedOpenHashMap<Key> keyToCount = new Object2IntLinkedOpenHashMap();

    private static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public synchronized void onSuppressedException(String location, Throwable exception) {
        long l = SuppressedExceptionsTracker.currentTimeMillis();
        String string2 = exception.getMessage();
        this.queue.add(new Entry(l, location, exception.getClass(), string2));
        while (this.queue.size() > 8) {
            this.queue.remove();
        }
        Key lv = new Key(location, exception.getClass());
        int i = this.keyToCount.getInt(lv);
        this.keyToCount.putAndMoveToFirst(lv, i + 1);
    }

    public synchronized String collect() {
        long l = SuppressedExceptionsTracker.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        if (!this.queue.isEmpty()) {
            stringBuilder.append("\n\t\tLatest entries:\n");
            for (Entry entry : this.queue) {
                stringBuilder.append("\t\t\t").append(entry.location).append(":").append(entry.cls).append(": ").append(entry.message).append(" (").append(l - entry.timestampMs).append("ms ago)").append("\n");
            }
        }
        if (!this.keyToCount.isEmpty()) {
            if (stringBuilder.isEmpty()) {
                stringBuilder.append("\n");
            }
            stringBuilder.append("\t\tEntry counts:\n");
            for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(this.keyToCount)) {
                stringBuilder.append("\t\t\t").append(((Key)entry.getKey()).location).append(":").append(((Key)entry.getKey()).cls).append(" x ").append(entry.getIntValue()).append("\n");
            }
        }
        if (stringBuilder.isEmpty()) {
            return "~~NONE~~";
        }
        return stringBuilder.toString();
    }

    record Entry(long timestampMs, String location, Class<? extends Throwable> cls, String message) {
    }

    record Key(String location, Class<? extends Throwable> cls) {
    }
}

