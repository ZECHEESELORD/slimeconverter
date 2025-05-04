package dev.harolddoes;

import java.io.*;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

public final class SlimeWorldWriter {

    private SlimeWorldWriter() {
        throw new AssertionError("Utility class");
    }

    public static void write(List<ChunkData> chunks, List<String> globalPalette, File outputFile) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            // Header
            out.writeShort(0xB10B); // Magic
            out.writeByte(0x09);    // Format version
            out.writeByte(0x0B);    // World version (e.g., 1.21.4)

            int minX = chunks.stream().mapToInt(ChunkData::getX).min().orElse(0);
            int minZ = chunks.stream().mapToInt(ChunkData::getZ).min().orElse(0);
            int maxX = chunks.stream().mapToInt(ChunkData::getX).max().orElse(0);
            int maxZ = chunks.stream().mapToInt(ChunkData::getZ).max().orElse(0);

            int width = (maxX - minX + 1);
            int depth = (maxZ - minZ + 1);

            out.writeShort(minX);
            out.writeShort(minZ);
            out.writeShort(width);
            out.writeShort(depth);

            // Write global palette
            ByteArrayOutputStream paletteBuffer = new ByteArrayOutputStream();
            try (DataOutputStream paletteOut = new DataOutputStream(new DeflaterOutputStream(paletteBuffer))) {
                paletteOut.writeShort(globalPalette.size());
                for (String id : globalPalette) {
                    byte[] bytes = id.getBytes();
                    paletteOut.writeByte(bytes.length);
                    paletteOut.write(bytes);
                }
            }
            byte[] paletteBytes = paletteBuffer.toByteArray();
            out.writeInt(paletteBytes.length);
            out.write(paletteBytes);

            // Write chunk data (compressed)
            ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
            try (DataOutputStream chunkOut = new DataOutputStream(new DeflaterOutputStream(chunkBuffer))) {
                chunkOut.writeShort(chunks.size());
                for (ChunkData chunk : chunks) {
                    chunkOut.writeShort(chunk.getX());
                    chunkOut.writeShort(chunk.getZ());
                    int[] indices = chunk.getPaletteIndices();
                    for (int index : indices) {
                        chunkOut.writeShort(index); // 2 bytes per index
                    }
                }
            }
            byte[] chunkBytes = chunkBuffer.toByteArray();
            out.writeInt(chunkBytes.length);
            out.write(chunkBytes);

            // No extras
            out.writeInt(0); // Extra compressed size
            out.writeInt(0); // Extra uncompressed size
        }
    }
}
