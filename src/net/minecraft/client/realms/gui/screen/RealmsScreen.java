/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.client.realms.RealmsLabel;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public abstract class RealmsScreen
extends Screen {
    protected static final int field_33055 = 17;
    protected static final int field_33057 = 7;
    protected static final long MAX_FILE_SIZE = 0x140000000L;
    protected static final int DARK_GRAY = -11776948;
    protected static final int GRAY = -9671572;
    protected static final int GREEN = -8388737;
    protected static final int BLUE = -13408581;
    protected static final int PURPLE = -9670204;
    protected static final int field_39676 = 32;
    protected static final int field_54866 = 8;
    protected static final Identifier REALMS_LOGO_TEXTURE = Identifier.ofVanilla("textures/gui/title/realms.png");
    protected static final int REALMS_LOGO_WIDGET_WIDTH = 128;
    protected static final int REALMS_LOGO_WIDGET_HEIGHT = 34;
    protected static final int REALMS_LOGO_TEXTURE_WIDTH = 128;
    protected static final int REALMS_LOGO_TEXTURE_HEIGHT = 64;
    private final List<RealmsLabel> labels = Lists.newArrayList();

    public RealmsScreen(Text arg) {
        super(arg);
    }

    protected static int row(int index) {
        return 40 + index * 13;
    }

    protected RealmsLabel addLabel(RealmsLabel label) {
        this.labels.add(label);
        return this.addDrawable(label);
    }

    public Text narrateLabels() {
        return ScreenTexts.joinLines(this.labels.stream().map(RealmsLabel::getText).collect(Collectors.toList()));
    }

    protected static IconWidget createRealmsLogoIconWidget() {
        return IconWidget.create(128, 34, REALMS_LOGO_TEXTURE, 128, 64);
    }
}

