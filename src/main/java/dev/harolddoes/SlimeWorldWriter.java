package dev.harolddoes;

import java.io.*;
import java.util.List;

public final class SlimeWorldWriter {

    private SlimeWorldWriter() {
        throw new AssertionError("Utility class");
    }

    public static void write(List<ChunkData> chunks, File outputFile) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            // Header
            out.writeShort(0xB10B); // Magic
            out.writeByte(0x09);    // Format version
            out.writeByte(0x01);    // World version (custom, 1 = 1.21.4 vanilla mapping)

            int minX = chunks.stream().mapToInt(ChunkData::getX).min().orElse(0);
            int minZ = chunks.stream().mapToInt(ChunkData::getZ).min().orElse(0);
            int maxX = chunks.stream().mapToInt(ChunkData::getX).max().orElse(0);
            int maxZ = chunks.stream().mapToInt(ChunkData::getZ).max().orElse(0);

            int width = (maxX - minX) + 1;
            int depth = (maxZ - minZ) + 1;

            out.writeShort(minX);
            out.writeShort(minZ);
            out.writeShort(width);
            out.writeShort(depth);

            // Prepare chunk buffer
            ByteArrayOutputStream rawChunkData = new ByteArrayOutputStream();
            DataOutputStream rawOut = new DataOutputStream(rawChunkData);

            for (ChunkData chunk : chunks) {
                rawOut.writeShort(chunk.getX());
                rawOut.writeShort(chunk.getZ());
                rawOut.write(chunk.getBlockIds());
                rawOut.write(chunk.getBlockData());
                rawOut.write(chunk.getLighting());
            }

            rawOut.flush();
            byte[] compressed = ZstdUtil.compress(rawChunkData.toByteArray());

            out.writeInt(compressed.length);
            out.write(compressed);

            // Skip PDC/custom tag section
            out.writeInt(0); // compressed size
            out.writeInt(0); // uncompressed size
        }
    }
}