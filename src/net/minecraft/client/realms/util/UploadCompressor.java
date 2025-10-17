/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.exception.upload.CancelledRealmsUploadException;
import net.minecraft.client.realms.exception.upload.TooBigRealmsUploadException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

@Environment(value=EnvType.CLIENT)
public class UploadCompressor {
    private static final long MAX_SIZE = 0x140000000L;
    private static final String field_54369 = "world";
    private final BooleanSupplier cancellationSupplier;
    private final Path directory;

    public static File compress(Path directory, BooleanSupplier cancellationSupplier) throws IOException {
        return new UploadCompressor(directory, cancellationSupplier).run();
    }

    private UploadCompressor(Path directory, BooleanSupplier cancellationSupplier) {
        this.cancellationSupplier = cancellationSupplier;
        this.directory = directory;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private File run() throws IOException {
        try (TarArchiveOutputStream tarArchiveOutputStream = null;){
            File file = File.createTempFile("realms-upload-file", ".tar.gz");
            tarArchiveOutputStream = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
            tarArchiveOutputStream.setLongFileMode(3);
            this.compress(tarArchiveOutputStream, this.directory, field_54369, true);
            if (this.cancellationSupplier.getAsBoolean()) {
                throw new CancelledRealmsUploadException();
            }
            tarArchiveOutputStream.finish();
            this.validateSize(file.length());
            File file2 = file;
            return file2;
        }
    }

    private void compress(TarArchiveOutputStream stream, Path directory, String prefix, boolean root) throws IOException {
        if (this.cancellationSupplier.getAsBoolean()) {
            throw new CancelledRealmsUploadException();
        }
        this.validateSize(stream.getBytesWritten());
        File file = directory.toFile();
        String string2 = root ? prefix : prefix + file.getName();
        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, string2);
        stream.putArchiveEntry(tarArchiveEntry);
        if (file.isFile()) {
            try (FileInputStream inputStream = new FileInputStream(file);){
                ((InputStream)inputStream).transferTo(stream);
            }
            stream.closeArchiveEntry();
        } else {
            stream.closeArchiveEntry();
            File[] files = file.listFiles();
            if (files != null) {
                for (File file2 : files) {
                    this.compress(stream, file2.toPath(), string2 + "/", false);
                }
            }
        }
    }

    private void validateSize(long size) {
        if (size > 0x140000000L) {
            throw new TooBigRealmsUploadException(0x140000000L);
        }
    }
}

