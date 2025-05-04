package dev.harolddoes;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnvilReader {

    private AnvilReader() {
        throw new AssertionError("Utility class");
    }

    /**
     * Loads all region files (.mca) in the input Anvil world and returns a list of chunk data.
     */
    public static List<ChunkData> loadChunks(Path worldPath) throws IOException {
        Path regionFolder = worldPath.resolve("region");
        if (!Files.exists(regionFolder)) {
            throw new IOException("Missing 'region' folder in world path: " + regionFolder);
        }

        List<ChunkData> result = new ArrayList<>();

        try (Stream<Path> stream = Files.list(regionFolder)) {
            List<Path> regionFiles = stream
                    .filter(path -> path.toString().endsWith(".mca"))
                    .toList();

            for (Path regionPath : regionFiles) {
                System.out.println("[AnvilReader] Reading region file: " + regionPath.getFileName());
                result.addAll(AnvilChunkParser.readRegion(new File(regionPath.toUri())));
            }
        }

        return result;
    }
}