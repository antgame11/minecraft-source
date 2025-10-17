/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.DebugLabelManager;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.gl.GlGpuBuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.ARBVertexAttribBinding;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

@Environment(value=EnvType.CLIENT)
public abstract class VertexBufferManager {
    public static VertexBufferManager create(GLCapabilities capabilities, DebugLabelManager labeler, Set<String> usedCapabilities) {
        if (capabilities.GL_ARB_vertex_attrib_binding && GlBackend.allowGlArbVABinding) {
            usedCapabilities.add("GL_ARB_vertex_attrib_binding");
            return new ARBVertexBufferManager(labeler);
        }
        return new DefaultVertexBufferManager(labeler);
    }

    public abstract void setupBuffer(VertexFormat var1, @Nullable GlGpuBuffer var2);

    @Environment(value=EnvType.CLIENT)
    static class ARBVertexBufferManager
    extends VertexBufferManager {
        private final Map<VertexFormat, AllocatedBuffer> cache = new HashMap<VertexFormat, AllocatedBuffer>();
        private final DebugLabelManager labeler;
        private final boolean applyMesaWorkaround;

        public ARBVertexBufferManager(DebugLabelManager labeler) {
            String string;
            this.labeler = labeler;
            this.applyMesaWorkaround = "Mesa".equals(GlStateManager._getString(GL11.GL_VENDOR)) ? (string = GlStateManager._getString(GL11.GL_VERSION)).contains("25.0.0") || string.contains("25.0.1") || string.contains("25.0.2") : false;
        }

        @Override
        public void setupBuffer(VertexFormat format, @Nullable GlGpuBuffer into) {
            AllocatedBuffer lv = this.cache.get(format);
            if (lv == null) {
                int i = GlStateManager._glGenVertexArrays();
                GlStateManager._glBindVertexArray(i);
                if (into != null) {
                    List<VertexFormatElement> list = format.getElements();
                    for (int j = 0; j < list.size(); ++j) {
                        VertexFormatElement vertexFormatElement = list.get(j);
                        GlStateManager._enableVertexAttribArray(j);
                        switch (vertexFormatElement.usage()) {
                            case POSITION: 
                            case GENERIC: 
                            case UV: {
                                if (vertexFormatElement.type() == VertexFormatElement.Type.FLOAT) {
                                    ARBVertexAttribBinding.glVertexAttribFormat(j, vertexFormatElement.count(), GlConst.toGl(vertexFormatElement.type()), false, format.getOffset(vertexFormatElement));
                                    break;
                                }
                                ARBVertexAttribBinding.glVertexAttribIFormat(j, vertexFormatElement.count(), GlConst.toGl(vertexFormatElement.type()), format.getOffset(vertexFormatElement));
                                break;
                            }
                            case NORMAL: 
                            case COLOR: {
                                ARBVertexAttribBinding.glVertexAttribFormat(j, vertexFormatElement.count(), GlConst.toGl(vertexFormatElement.type()), true, format.getOffset(vertexFormatElement));
                            }
                        }
                        ARBVertexAttribBinding.glVertexAttribBinding(j, 0);
                    }
                }
                if (into != null) {
                    ARBVertexAttribBinding.glBindVertexBuffer(0, into.id, 0L, format.getVertexSize());
                }
                AllocatedBuffer lv2 = new AllocatedBuffer(i, format, into);
                this.labeler.labelAllocatedBuffer(lv2);
                this.cache.put(format, lv2);
                return;
            }
            GlStateManager._glBindVertexArray(lv.glId);
            if (into != null && lv.buffer != into) {
                if (this.applyMesaWorkaround && lv.buffer != null && lv.buffer.id == into.id) {
                    ARBVertexAttribBinding.glBindVertexBuffer(0, 0, 0L, 0);
                }
                ARBVertexAttribBinding.glBindVertexBuffer(0, into.id, 0L, format.getVertexSize());
                lv.buffer = into;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DefaultVertexBufferManager
    extends VertexBufferManager {
        private final Map<VertexFormat, AllocatedBuffer> cache = new HashMap<VertexFormat, AllocatedBuffer>();
        private final DebugLabelManager labeler;

        public DefaultVertexBufferManager(DebugLabelManager labeler) {
            this.labeler = labeler;
        }

        @Override
        public void setupBuffer(VertexFormat format, @Nullable GlGpuBuffer into) {
            AllocatedBuffer lv = this.cache.get(format);
            if (lv == null) {
                int i = GlStateManager._glGenVertexArrays();
                GlStateManager._glBindVertexArray(i);
                if (into != null) {
                    GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, into.id);
                    DefaultVertexBufferManager.setupBuffer(format, true);
                }
                AllocatedBuffer lv2 = new AllocatedBuffer(i, format, into);
                this.labeler.labelAllocatedBuffer(lv2);
                this.cache.put(format, lv2);
                return;
            }
            GlStateManager._glBindVertexArray(lv.glId);
            if (into != null && lv.buffer != into) {
                GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, into.id);
                lv.buffer = into;
                DefaultVertexBufferManager.setupBuffer(format, false);
            }
        }

        private static void setupBuffer(VertexFormat format, boolean vbaIsNew) {
            int i = format.getVertexSize();
            List<VertexFormatElement> list = format.getElements();
            block4: for (int j = 0; j < list.size(); ++j) {
                VertexFormatElement vertexFormatElement = list.get(j);
                if (vbaIsNew) {
                    GlStateManager._enableVertexAttribArray(j);
                }
                switch (vertexFormatElement.usage()) {
                    case POSITION: 
                    case GENERIC: 
                    case UV: {
                        if (vertexFormatElement.type() == VertexFormatElement.Type.FLOAT) {
                            GlStateManager._vertexAttribPointer(j, vertexFormatElement.count(), GlConst.toGl(vertexFormatElement.type()), false, i, format.getOffset(vertexFormatElement));
                            continue block4;
                        }
                        GlStateManager._vertexAttribIPointer(j, vertexFormatElement.count(), GlConst.toGl(vertexFormatElement.type()), i, format.getOffset(vertexFormatElement));
                        continue block4;
                    }
                    case NORMAL: 
                    case COLOR: {
                        GlStateManager._vertexAttribPointer(j, vertexFormatElement.count(), GlConst.toGl(vertexFormatElement.type()), true, i, format.getOffset(vertexFormatElement));
                    }
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class AllocatedBuffer {
        final int glId;
        final VertexFormat vertexFormat;
        @Nullable
        GlGpuBuffer buffer;

        AllocatedBuffer(int glId, VertexFormat vertexFormat, @Nullable GlGpuBuffer buffer) {
            this.glId = glId;
            this.vertexFormat = vertexFormat;
            this.buffer = buffer;
        }
    }
}

