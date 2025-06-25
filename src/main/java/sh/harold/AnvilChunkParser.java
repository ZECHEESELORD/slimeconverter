package sh.harold;

import sh.harold.nbt.NBTCompound;
import sh.harold.nbt.NBTInput;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public final class AnvilChunkParser {
    private AnvilChunkParser() {
        throw new AssertionError("Utility class");
    }

    public static List<ChunkData> readRegion(File mcaFile) {
        List<ChunkData> chunks = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(mcaFile, "r")) {
            for (int i = 0; i < 1024; i++) {
                raf.seek(i * 4);
                int offset = (raf.readUnsignedByte() << 16) | (raf.readUnsignedByte() << 8) | raf.readUnsignedByte();
                int sectorCount = raf.readUnsignedByte();
                if (offset == 0 || sectorCount == 0) continue;
                long chunkStart = offset * 4096L;
                long chunkEnd = chunkStart + sectorCount * 4096L;
                raf.seek(chunkStart);
                if (chunkEnd > raf.length()) {
                    System.err.println("[WARN] Chunk at index " + i + " in region " + mcaFile.getName() + " exceeds file length, skipping.");
                    continue;
                }
                if (chunkEnd - chunkStart < 5) {
                    System.err.println("[WARN] Chunk at index " + i + " in region " + mcaFile.getName() + " is too small, skipping.");
                    continue;
                }
                int length = raf.readInt();
                if (length < 1) {
                    System.err.println("[WARN] Chunk at index " + i + " in region " + mcaFile.getName() + " has invalid length, skipping.");
                    continue;
                }
                if (length + 4 > sectorCount * 4096) {
                    throw new IOException("Declared chunk size exceeds allocated sector space at index " + i + " in region " + mcaFile.getName());
                }
                if (length < 2) {
                    throw new IOException("Chunk data is less than minimum size (length + compression + 1 byte payload) at index " + i + " in region " + mcaFile.getName());
                }
                byte[] rawChunkData = new byte[length];
                int read = raf.read(rawChunkData);
                if (read != length) {
                    throw new IOException("Failed to read full chunk data at index " + i + " in region " + mcaFile.getName());
                }
                try {
                    ChunkData data = parseChunk(rawChunkData);
                    if (data != null) {
                        chunks.add(data);
                    }
                } catch (Exception chunkEx) {
                    System.err.println("[WARN] Failed to parse chunk at index " + i + " in region " + mcaFile.getName());
                    chunkEx.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse region " + mcaFile.getName());
            e.printStackTrace();
        }
        return chunks;
    }

    /**
     * Parses a single chunk from raw chunk data (length = N, first byte = compression, rest = payload).
     * Performs all boundary and format checks. Throws on error.
     */
    public static ChunkData parseChunk(byte[] rawChunkData) throws IOException {
        if (rawChunkData == null || rawChunkData.length < 2) {
            throw new IOException("Chunk data is less than minimum size (compression + 1 byte payload)");
        }
        int compression = rawChunkData[0] & 0xFF;
        byte[] compressed = new byte[rawChunkData.length - 1];
        System.arraycopy(rawChunkData, 1, compressed, 0, compressed.length);
        InputStream is;
        switch (compression) {
            case 1 -> is = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(compressed)));
            case 2 -> is = new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(compressed)));
            default -> throw new IOException("Unknown compression type: " + compression);
        }
        NBTCompound root = (NBTCompound) new NBTInput(is).readTag();
        NBTCompound level = root.get("Level").map(NBTCompound.class::cast).orElse(root);
        if (!level.containsKey("sections")) return null;
        return ChunkData.fromNBT(level);
    }
}