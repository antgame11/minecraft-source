/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface MultilineText {
    public static final MultilineText EMPTY = new MultilineText(){

        @Override
        public int draw(DrawContext context, Alignment alignment, int x, int y, int lineHeight, boolean bl, int color) {
            return y;
        }

        @Override
        public Style getStyleAt(Alignment alignment, int baseX, int baseY, int lineHeight, double mouseX, double mouseY) {
            return null;
        }

        @Override
        public int getLineCount() {
            return 0;
        }

        @Override
        public int getMaxWidth() {
            return 0;
        }
    };

    public static MultilineText create(TextRenderer renderer, Text ... texts) {
        return MultilineText.create(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, texts);
    }

    public static MultilineText create(TextRenderer renderer, int maxWidth, Text ... texts) {
        return MultilineText.create(renderer, maxWidth, Integer.MAX_VALUE, texts);
    }

    public static MultilineText create(TextRenderer renderer, Text text, int maxWidth) {
        return MultilineText.create(renderer, maxWidth, Integer.MAX_VALUE, text);
    }

    public static MultilineText create(final TextRenderer renderer, final int maxWidth, final int maxLines, final Text ... texts) {
        if (texts.length == 0) {
            return EMPTY;
        }
        return new MultilineText(){
            @Nullable
            private List<Line> lines;
            @Nullable
            private Language language;

            @Override
            public int draw(DrawContext context, Alignment alignment, int x, int y, int lineHeight, boolean bl, int color) {
                int m = y;
                for (Line lv : this.getLines()) {
                    int n = alignment.getAdjustedX(x, lv.width);
                    context.drawTextWithShadow(renderer, lv.text, n, m, color);
                    m += lineHeight;
                }
                return m;
            }

            @Override
            @Nullable
            public Style getStyleAt(Alignment alignment, int baseX, int baseY, int lineHeight, double mouseX, double mouseY) {
                List<Line> list = this.getLines();
                int l = MathHelper.floor((mouseY - (double)baseY) / (double)lineHeight);
                if (l < 0 || l >= list.size()) {
                    return null;
                }
                Line lv = list.get(l);
                int m = alignment.getAdjustedX(baseX, lv.width);
                if (mouseX < (double)m) {
                    return null;
                }
                int n = MathHelper.floor(mouseX - (double)m);
                return renderer.getTextHandler().getStyleAt(lv.text, n);
            }

            private List<Line> getLines() {
                Language lv = Language.getInstance();
                if (this.lines != null && lv == this.language) {
                    return this.lines;
                }
                this.language = lv;
                ArrayList<StringVisitable> list = new ArrayList<StringVisitable>();
                for (Text lv2 : texts) {
                    list.addAll(renderer.wrapLinesWithoutLanguage(lv2, maxWidth));
                }
                this.lines = new ArrayList<Line>();
                int i = Math.min(list.size(), maxLines);
                List list2 = list.subList(0, i);
                for (int j = 0; j < list2.size(); ++j) {
                    StringVisitable lv3 = (StringVisitable)list2.get(j);
                    OrderedText lv4 = Language.getInstance().reorder(lv3);
                    if (j == list2.size() - 1 && i == maxLines && i != list.size()) {
                        StringVisitable lv5 = renderer.trimToWidth(lv3, renderer.getWidth(lv3) - renderer.getWidth(ScreenTexts.ELLIPSIS));
                        StringVisitable lv6 = StringVisitable.concat(lv5, ScreenTexts.ELLIPSIS);
                        this.lines.add(new Line(Language.getInstance().reorder(lv6), renderer.getWidth(lv6)));
                        continue;
                    }
                    this.lines.add(new Line(lv4, renderer.getWidth(lv4)));
                }
                return this.lines;
            }

            @Override
            public int getLineCount() {
                return this.getLines().size();
            }

            @Override
            public int getMaxWidth() {
                return Math.min(maxWidth, this.getLines().stream().mapToInt(Line::width).max().orElse(0));
            }
        };
    }

    public int draw(DrawContext var1, Alignment var2, int var3, int var4, int var5, boolean var6, int var7);

    @Nullable
    public Style getStyleAt(Alignment var1, int var2, int var3, int var4, double var5, double var7);

    public int getLineCount();

    public int getMaxWidth();

    @Environment(value=EnvType.CLIENT)
    public static enum Alignment {
        LEFT{

            @Override
            int getAdjustedX(int x, int width) {
                return x;
            }
        }
        ,
        CENTER{

            @Override
            int getAdjustedX(int x, int width) {
                return x - width / 2;
            }
        }
        ,
        RIGHT{

            @Override
            int getAdjustedX(int x, int width) {
                return x - width;
            }
        };


        abstract int getAdjustedX(int var1, int var2);
    }

    @Environment(value=EnvType.CLIENT)
    public record Line(OrderedText text, int width) {
    }
}

