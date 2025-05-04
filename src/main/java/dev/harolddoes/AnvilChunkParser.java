package dev.harolddoes;

import net.querz.mca.Chunk;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class AnvilChunkParser {

    private AnvilChunkParser() {
        throw new AssertionError("Utility class");
    }

    public static List<ChunkData> readRegion(File mcaFile) {
        List<ChunkData> chunks = new ArrayList<>();
        try {
            MCAFile mca = MCAUtil.read(mcaFile);
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    Chunk chunk = mca.getChunk(x, z);
                    if (chunk == null) continue;

                    CompoundTag level = chunk.getHandle().getCompoundTag("Level");
                    if (level == null) continue;

                    ChunkData parsed = ChunkData.fromNBT(level);
                    if (parsed != null) {
                        chunks.add(parsed);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AnvilChunkParser] Failed to parse " + mcaFile.getName());
            e.printStackTrace(); // surely this is fine...
        }
        return chunks;
    }
}