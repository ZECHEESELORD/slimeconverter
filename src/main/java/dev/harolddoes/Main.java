package dev.harolddoes;

import java.io.File;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        File inputWorld = new File("world"); // Path to anvil world folder
        File outputSlime = new File("output.slime");

        System.out.println("[HAROLDDOESDEV] Reading chunks from Anvil world: " + inputWorld.getAbsolutePath());

        List<String> globalPalette = new ArrayList<>();
        Map<String, Integer> paletteIndexMap = new HashMap<>();

        List<ChunkData> chunks = AnvilReader.loadChunks(inputWorld, globalPalette, paletteIndexMap);

        System.out.println("[HAROLDDOESDEV] Writing Slime world: " + outputSlime.getAbsolutePath());

        try {
            SlimeWorldWriter.write(chunks, globalPalette, outputSlime);
            System.out.println("[HAROLDDOESDEV] Finished writing Slime world: " + outputSlime.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[HAROLDDOESDEV] Failed to write Slime world");
            e.printStackTrace();
        }
    }
}
