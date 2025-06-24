package sh.harold;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class AnvilReader {
    private AnvilReader() {
        throw new AssertionError("Utility class");
    }

    public static List<ChunkData> loadChunks(File worldDirectory) {
        List<ChunkData> allChunks = new ArrayList<>();
        File[] regionFiles = new File(worldDirectory, "region").listFiles((dir, name) -> name.endsWith(".mca"));
        if (regionFiles == null) {
            System.err.println("No region files found.");
            return allChunks;
        }
        for (File regionFile : regionFiles) {
            System.out.println("Reading region file: " + regionFile.getName());
            List<ChunkData> chunks = AnvilChunkParser.readRegion(regionFile);
            allChunks.addAll(chunks);
        }
        return allChunks;
    }
}
