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
import java.nio.ByteBuffer;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.gl.GpuDeviceInfo;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;

@Environment(value=EnvType.CLIENT)
public abstract class BufferManager {
    public static BufferManager create(GLCapabilities capabilities, Set<String> usedCapabilities, GpuDeviceInfo deviceInfo) {
        if (capabilities.GL_ARB_direct_state_access && GlBackend.allowGlArbDirectAccess && !deviceInfo.shouldDisableArbDirectAccess()) {
            usedCapabilities.add("GL_ARB_direct_state_access");
            return new ARBBufferManager();
        }
        return new DefaultBufferManager();
    }

    abstract int createBuffer();

    abstract void setBufferData(int var1, long var2, int var4);

    abstract void setBufferData(int var1, ByteBuffer var2, int var3);

    abstract void setBufferSubData(int var1, int var2, ByteBuffer var3, int var4);

    abstract void setBufferStorage(int var1, long var2, int var4);

    abstract void setBufferStorage(int var1, ByteBuffer var2, int var3);

    @Nullable
    abstract ByteBuffer mapBufferRange(int var1, int var2, int var3, int var4, int var5);

    abstract void unmapBuffer(int var1, int var2);

    abstract int createFramebuffer();

    abstract void setupFramebuffer(int var1, int var2, int var3, int var4, int var5);

    abstract void setupBlitFramebuffer(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12);

    abstract void flushMappedBufferRange(int var1, int var2, int var3, int var4);

    abstract void copyBufferSubData(int var1, int var2, int var3, int var4, int var5);

    @Environment(value=EnvType.CLIENT)
    static class ARBBufferManager
    extends BufferManager {
        ARBBufferManager() {
        }

        @Override
        int createBuffer() {
            GlStateManager.incrementTrackedBuffers();
            return ARBDirectStateAccess.glCreateBuffers();
        }

        @Override
        void setBufferData(int buffer, long size, int usage) {
            ARBDirectStateAccess.glNamedBufferData(buffer, size, GlConst.bufferUsageToGlEnum(usage));
        }

        @Override
        void setBufferData(int buffer, ByteBuffer data, int usage) {
            ARBDirectStateAccess.glNamedBufferData(buffer, data, GlConst.bufferUsageToGlEnum(usage));
        }

        @Override
        void setBufferSubData(int buffer, int offset, ByteBuffer data, int usage) {
            ARBDirectStateAccess.glNamedBufferSubData(buffer, (long)offset, data);
        }

        @Override
        void setBufferStorage(int buffer, long size, int usage) {
            ARBDirectStateAccess.glNamedBufferStorage(buffer, size, GlConst.bufferUsageToGlFlag(usage));
        }

        @Override
        void setBufferStorage(int buffer, ByteBuffer data, int usage) {
            ARBDirectStateAccess.glNamedBufferStorage(buffer, data, GlConst.bufferUsageToGlFlag(usage));
        }

        @Override
        @Nullable
        ByteBuffer mapBufferRange(int buffer, int offset, int length, int access, int usage) {
            return ARBDirectStateAccess.glMapNamedBufferRange(buffer, offset, length, access);
        }

        @Override
        void unmapBuffer(int buffer, int usage) {
            ARBDirectStateAccess.glUnmapNamedBuffer(buffer);
        }

        @Override
        public int createFramebuffer() {
            return ARBDirectStateAccess.glCreateFramebuffers();
        }

        @Override
        public void setupFramebuffer(int framebuffer, int colorAttachment, int depthAttachment, int mipLevel, int bindTarget) {
            ARBDirectStateAccess.glNamedFramebufferTexture(framebuffer, GlConst.GL_COLOR_ATTACHMENT0, colorAttachment, mipLevel);
            ARBDirectStateAccess.glNamedFramebufferTexture(framebuffer, GlConst.GL_DEPTH_ATTACHMENT, depthAttachment, mipLevel);
            if (bindTarget != 0) {
                GlStateManager._glBindFramebuffer(bindTarget, framebuffer);
            }
        }

        @Override
        public void setupBlitFramebuffer(int readFramebuffer, int writeFramebuffer, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
            ARBDirectStateAccess.glBlitNamedFramebuffer(readFramebuffer, writeFramebuffer, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
        }

        @Override
        void flushMappedBufferRange(int buffer, int offset, int length, int usage) {
            ARBDirectStateAccess.glFlushMappedNamedBufferRange(buffer, offset, length);
        }

        @Override
        void copyBufferSubData(int fromBuffer, int toBuffer, int readOffset, int writeOffset, int size) {
            ARBDirectStateAccess.glCopyNamedBufferSubData(fromBuffer, toBuffer, readOffset, writeOffset, size);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DefaultBufferManager
    extends BufferManager {
        DefaultBufferManager() {
        }

        private int getTarget(int i) {
            if ((i & 0x20) != 0) {
                return 34962;
            }
            if ((i & 0x40) != 0) {
                return 34963;
            }
            if ((i & 0x80) != 0) {
                return 35345;
            }
            return 36663;
        }

        @Override
        int createBuffer() {
            return GlStateManager._glGenBuffers();
        }

        @Override
        void setBufferData(int buffer, long size, int usage) {
            int k = this.getTarget(usage);
            GlStateManager._glBindBuffer(k, buffer);
            GlStateManager._glBufferData(k, size, GlConst.bufferUsageToGlEnum(usage));
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void setBufferData(int buffer, ByteBuffer data, int usage) {
            int k = this.getTarget(usage);
            GlStateManager._glBindBuffer(k, buffer);
            GlStateManager._glBufferData(k, data, GlConst.bufferUsageToGlEnum(usage));
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void setBufferSubData(int buffer, int offset, ByteBuffer data, int usage) {
            int l = this.getTarget(usage);
            GlStateManager._glBindBuffer(l, buffer);
            GlStateManager._glBufferSubData(l, offset, data);
            GlStateManager._glBindBuffer(l, 0);
        }

        @Override
        void setBufferStorage(int buffer, long size, int usage) {
            int k = this.getTarget(usage);
            GlStateManager._glBindBuffer(k, buffer);
            ARBBufferStorage.glBufferStorage(k, size, GlConst.bufferUsageToGlFlag(usage));
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void setBufferStorage(int buffer, ByteBuffer data, int usage) {
            int k = this.getTarget(usage);
            GlStateManager._glBindBuffer(k, buffer);
            ARBBufferStorage.glBufferStorage(k, data, GlConst.bufferUsageToGlFlag(usage));
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        @Nullable
        ByteBuffer mapBufferRange(int buffer, int offset, int length, int access, int usage) {
            int n = this.getTarget(usage);
            GlStateManager._glBindBuffer(n, buffer);
            ByteBuffer byteBuffer = GlStateManager._glMapBufferRange(n, offset, length, access);
            GlStateManager._glBindBuffer(n, 0);
            return byteBuffer;
        }

        @Override
        void unmapBuffer(int buffer, int usage) {
            int k = this.getTarget(usage);
            GlStateManager._glBindBuffer(k, buffer);
            GlStateManager._glUnmapBuffer(k);
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void flushMappedBufferRange(int buffer, int offset, int length, int usage) {
            int m = this.getTarget(usage);
            GlStateManager._glBindBuffer(m, buffer);
            GL30.glFlushMappedBufferRange(m, offset, length);
            GlStateManager._glBindBuffer(m, 0);
        }

        @Override
        void copyBufferSubData(int fromBuffer, int toBuffer, int readOffset, int writeOffset, int size) {
            GlStateManager._glBindBuffer(GlConst.GL_COPY_READ_BUFFER, fromBuffer);
            GlStateManager._glBindBuffer(GlConst.GL_COPY_WRITE_BUFFER, toBuffer);
            GL31.glCopyBufferSubData(36662, 36663, readOffset, writeOffset, size);
            GlStateManager._glBindBuffer(GlConst.GL_COPY_READ_BUFFER, 0);
            GlStateManager._glBindBuffer(GlConst.GL_COPY_WRITE_BUFFER, 0);
        }

        @Override
        public int createFramebuffer() {
            return GlStateManager.glGenFramebuffers();
        }

        @Override
        public void setupFramebuffer(int framebuffer, int colorAttachment, int depthAttachment, int mipLevel, int bindTarget) {
            int n = bindTarget == 0 ? GlConst.GL_DRAW_FRAMEBUFFER : bindTarget;
            int o = GlStateManager.getFrameBuffer(n);
            GlStateManager._glBindFramebuffer(n, framebuffer);
            GlStateManager._glFramebufferTexture2D(n, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, colorAttachment, mipLevel);
            GlStateManager._glFramebufferTexture2D(n, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_TEXTURE_2D, depthAttachment, mipLevel);
            if (bindTarget == 0) {
                GlStateManager._glBindFramebuffer(n, o);
            }
        }

        @Override
        public void setupBlitFramebuffer(int readFramebuffer, int writeFramebuffer, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
            int u = GlStateManager.getFrameBuffer(GlConst.GL_READ_FRAMEBUFFER);
            int v = GlStateManager.getFrameBuffer(GlConst.GL_DRAW_FRAMEBUFFER);
            GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, readFramebuffer);
            GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, writeFramebuffer);
            GlStateManager._glBlitFrameBuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
            GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, u);
            GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, v);
        }
    }
}

