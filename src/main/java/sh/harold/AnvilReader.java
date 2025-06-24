package sh.harold;

import java.io.File;
import java.util.*;

public final class AnvilReader {

    private AnvilReader() {
        throw new AssertionError("Utility class");
    }

    public static List<ChunkData> loadChunks(File worldDirectory, List<String> globalPalette, Map<String, Integer> paletteIndexMap) {
        List<ChunkData> allChunks = new ArrayList<>();

        File[] regionFiles = new File(worldDirectory, "region").listFiles((dir, name) -> name.endsWith(".mca"));
        if (regionFiles == null) {
            System.err.println("No region files found.");
            return allChunks;
        }

        for (File regionFile : regionFiles) {
            System.out.println("Reading region file: " + regionFile.getName());
            List<ChunkData> chunks = AnvilChunkParser.readRegion(regionFile, globalPalette, paletteIndexMap);
            allChunks.addAll(chunks);
        }

        return allChunks;
    }
}
