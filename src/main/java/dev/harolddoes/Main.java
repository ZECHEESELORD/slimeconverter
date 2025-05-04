/*
 * Main.java
 * CLI tool to convert an Anvil world folder (1.21.4) to SlimeLoader-compatible slime format.
 */
package dev.harolddoes;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java -jar SlimeConverter.jar <input-anvil-world-dir> <output.slime>");
            System.exit(1);
        }

        File inputWorld = new File(args[0]);
        File outputSlime = new File(args[1]);

        if (!inputWorld.exists() || !inputWorld.isDirectory()) {
            throw new IllegalArgumentException("Input world folder does not exist or is not a directory.");
        }

        System.out.println("[HAROLDDOESDEV] Reading chunks from Anvil world: " + inputWorld.getAbsolutePath());
        List<ChunkData> chunks = AnvilReader.loadChunks(inputWorld.toPath());
        System.out.println("[HAROLDDOESDEV] Loaded " + chunks.size() + " chunks.");

        System.out.println("[HAROLDDOESDEV] Writing slime file to: " + outputSlime.getAbsolutePath());
        SlimeWorldWriter.write(chunks, outputSlime);
        System.out.println("[HAROLDDOESDEV] Conversion complete!");
    }
}
