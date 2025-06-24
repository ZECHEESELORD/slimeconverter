package sh.harold;

import sh.harold.nbt.*;
import java.io.*;
import java.util.List;

public final class SlimeWorldWriter {
    private SlimeWorldWriter() { throw new AssertionError("Utility class"); }

    /**
     * Write a .slime file matching the official spec.
     *
     * @param chunks       List of ChunkData
     * @param worldVersion Minecraft world version (int)
     * @param outputFile   Output file
     */
    public static void write(List<ChunkData> chunks, int worldVersion, File outputFile) throws IOException {
        // === Write header ===
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            out.writeShort(0xB10B); // Magic
            out.writeByte(0x0C);    // Slime format version
            out.writeInt(worldVersion); // World version

            // --- Chunks ---
            ByteArrayOutputStream chunkRaw = new ByteArrayOutputStream();
            try (DataOutputStream chunkOut = new DataOutputStream(chunkRaw)) {
                for (ChunkData chunk : chunks) {
                    ChunkSerializer.writeChunk(chunkOut, chunk);
                }
            }
            byte[] chunkBytes = chunkRaw.toByteArray();
            byte[] chunkCompressed = ZstdUtil.compress(chunkBytes);
            out.writeInt(chunkCompressed.length);
            out.writeInt(chunkBytes.length);
            out.write(chunkCompressed);

            // --- Extra (PDC/custom data) ---
            NBTCompound extra = new NBTCompound(null, java.util.Map.of()); // TODO: Fill with real extra data if available
            byte[] extraRaw = nbtToBytes(extra);
            byte[] extraCompressed = ZstdUtil.compress(extraRaw);
            out.writeInt(extraCompressed.length);
            out.writeInt(extraRaw.length);
            out.write(extraCompressed);
        }
    }

    private static byte[] nbtToBytes(NBTCompound tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new sh.harold.nbt.NBTOutput(baos).writeTag(tag);
        return baos.toByteArray();
    }
}
