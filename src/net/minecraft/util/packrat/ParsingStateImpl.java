/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.packrat;

import net.minecraft.util.Util;
import net.minecraft.util.packrat.Cut;
import net.minecraft.util.packrat.ParseErrorList;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.Symbol;
import org.jetbrains.annotations.Nullable;

public abstract class ParsingStateImpl<S>
implements ParsingState<S> {
    private MemoizedData[] memoStack = new MemoizedData[256];
    private final ParseErrorList<S> errors;
    private final ParseResults results = new ParseResults();
    private Cutter[] cutters = new Cutter[16];
    private int topCutterIndex;
    private final ErrorSuppressing errorSuppressingState = new ErrorSuppressing();

    protected ParsingStateImpl(ParseErrorList<S> errors) {
        this.errors = errors;
    }

    @Override
    public ParseResults getResults() {
        return this.results;
    }

    @Override
    public ParseErrorList<S> getErrors() {
        return this.errors;
    }

    @Override
    @Nullable
    public <T> T parse(ParsingRuleEntry<S, T> rule) {
        MemoizedValue lv3;
        T object;
        int i = this.getCursor();
        MemoizedData lv = this.pushMemoizedData(i);
        int j = lv.get(rule.getSymbol());
        if (j != -1) {
            MemoizedValue lv2 = lv.get(j);
            if (lv2 != null) {
                if (lv2 == MemoizedValue.EMPTY) {
                    return null;
                }
                this.setCursor(lv2.markAfterParse);
                return lv2.value;
            }
        } else {
            j = lv.push(rule.getSymbol());
        }
        if ((object = rule.getRule().parse(this)) == null) {
            lv3 = MemoizedValue.empty();
        } else {
            int k = this.getCursor();
            lv3 = new MemoizedValue<T>(object, k);
        }
        lv.put(j, lv3);
        return object;
    }

    private MemoizedData pushMemoizedData(int cursor) {
        MemoizedData lv;
        int j = this.memoStack.length;
        if (cursor >= j) {
            int k = Util.nextCapacity(j, cursor + 1);
            MemoizedData[] lvs = new MemoizedData[k];
            System.arraycopy(this.memoStack, 0, lvs, 0, j);
            this.memoStack = lvs;
        }
        if ((lv = this.memoStack[cursor]) == null) {
            this.memoStack[cursor] = lv = new MemoizedData();
        }
        return lv;
    }

    @Override
    public Cut pushCutter() {
        Cutter lv;
        int j;
        int i = this.cutters.length;
        if (this.topCutterIndex >= i) {
            j = Util.nextCapacity(i, this.topCutterIndex + 1);
            Cutter[] lvs = new Cutter[j];
            System.arraycopy(this.cutters, 0, lvs, 0, i);
            this.cutters = lvs;
        }
        if ((lv = this.cutters[j = this.topCutterIndex++]) == null) {
            this.cutters[j] = lv = new Cutter();
        } else {
            lv.reset();
        }
        return lv;
    }

    @Override
    public void popCutter() {
        --this.topCutterIndex;
    }

    @Override
    public ParsingState<S> getErrorSuppressingState() {
        return this.errorSuppressingState;
    }

    static class MemoizedData {
        public static final int SIZE_PER_SYMBOL = 2;
        private static final int MISSING = -1;
        private Object[] values = new Object[16];
        private int top;

        MemoizedData() {
        }

        public int get(Symbol<?> symbol) {
            for (int i = 0; i < this.top; i += 2) {
                if (this.values[i] != symbol) continue;
                return i;
            }
            return -1;
        }

        public int push(Symbol<?> symbol) {
            int i = this.top;
            this.top += 2;
            int j = i + 1;
            int k = this.values.length;
            if (j >= k) {
                int l = Util.nextCapacity(k, j + 1);
                Object[] objects = new Object[l];
                System.arraycopy(this.values, 0, objects, 0, k);
                this.values = objects;
            }
            this.values[i] = symbol;
            return i;
        }

        @Nullable
        public <T> MemoizedValue<T> get(int index) {
            return (MemoizedValue)this.values[index + 1];
        }

        public void put(int index, MemoizedValue<?> value) {
            this.values[index + 1] = value;
        }
    }

    static class Cutter
    implements Cut {
        private boolean cut;

        Cutter() {
        }

        @Override
        public void cut() {
            this.cut = true;
        }

        @Override
        public boolean isCut() {
            return this.cut;
        }

        public void reset() {
            this.cut = false;
        }
    }

    class ErrorSuppressing
    implements ParsingState<S> {
        private final ParseErrorList<S> errors = new ParseErrorList.Noop();

        ErrorSuppressing() {
        }

        @Override
        public ParseErrorList<S> getErrors() {
            return this.errors;
        }

        @Override
        public ParseResults getResults() {
            return ParsingStateImpl.this.getResults();
        }

        @Override
        @Nullable
        public <T> T parse(ParsingRuleEntry<S, T> rule) {
            return ParsingStateImpl.this.parse(rule);
        }

        @Override
        public S getReader() {
            return ParsingStateImpl.this.getReader();
        }

        @Override
        public int getCursor() {
            return ParsingStateImpl.this.getCursor();
        }

        @Override
        public void setCursor(int cursor) {
            ParsingStateImpl.this.setCursor(cursor);
        }

        @Override
        public Cut pushCutter() {
            return ParsingStateImpl.this.pushCutter();
        }

        @Override
        public void popCutter() {
            ParsingStateImpl.this.popCutter();
        }

        @Override
        public ParsingState<S> getErrorSuppressingState() {
            return this;
        }
    }

    record MemoizedValue<T>(@Nullable T value, int markAfterParse) {
        public static final MemoizedValue<?> EMPTY = new MemoizedValue<Object>(null, -1);

        public static <T> MemoizedValue<T> empty() {
            return EMPTY;
        }

        @Nullable
        public T value() {
            return this.value;
        }
    }
}

