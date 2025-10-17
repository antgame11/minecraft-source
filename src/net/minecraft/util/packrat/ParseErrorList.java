/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.util.packrat.ParseError;
import net.minecraft.util.packrat.Suggestable;

public interface ParseErrorList<S> {
    public void add(int var1, Suggestable<S> var2, Object var3);

    default public void add(int cursor, Object reason) {
        this.add(cursor, Suggestable.empty(), reason);
    }

    public void setCursor(int var1);

    public static class Impl<S>
    implements ParseErrorList<S> {
        private Entry<S>[] errors = new Entry[16];
        private int topIndex;
        private int cursor = -1;

        private void moveCursor(int cursor) {
            if (cursor > this.cursor) {
                this.cursor = cursor;
                this.topIndex = 0;
            }
        }

        @Override
        public void setCursor(int cursor) {
            this.moveCursor(cursor);
        }

        @Override
        public void add(int cursor, Suggestable<S> suggestions, Object reason) {
            this.moveCursor(cursor);
            if (cursor == this.cursor) {
                this.add(suggestions, reason);
            }
        }

        private void add(Suggestable<S> suggestions, Object reason) {
            Entry<S> lv;
            int j;
            int i = this.errors.length;
            if (this.topIndex >= i) {
                j = Util.nextCapacity(i, this.topIndex + 1);
                Entry[] lvs = new Entry[j];
                System.arraycopy(this.errors, 0, lvs, 0, i);
                this.errors = lvs;
            }
            if ((lv = this.errors[j = this.topIndex++]) == null) {
                this.errors[j] = lv = new Entry();
            }
            lv.suggestions = suggestions;
            lv.reason = reason;
        }

        public List<ParseError<S>> getErrors() {
            int i = this.topIndex;
            if (i == 0) {
                return List.of();
            }
            ArrayList<ParseError<S>> list = new ArrayList<ParseError<S>>(i);
            for (int j = 0; j < i; ++j) {
                Entry<S> lv = this.errors[j];
                list.add(new ParseError(this.cursor, lv.suggestions, lv.reason));
            }
            return list;
        }

        public int getCursor() {
            return this.cursor;
        }

        static class Entry<S> {
            Suggestable<S> suggestions = Suggestable.empty();
            Object reason = "empty";

            Entry() {
            }
        }
    }

    public static class Noop<S>
    implements ParseErrorList<S> {
        @Override
        public void add(int cursor, Suggestable<S> suggestions, Object reason) {
        }

        @Override
        public void setCursor(int cursor) {
        }
    }
}

