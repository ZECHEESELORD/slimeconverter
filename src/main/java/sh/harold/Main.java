package sh.harold;

import java.io.File;
import java.util.*;

public class Main {
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String RED = "\u001B[31m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            printMenu();
            System.out.print(CYAN + "Choose an option:" + RESET + " ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> convertAnvilToSlime(scanner);
                case "2" -> verifySlime(scanner);
                case "3" -> printSpec();
                case "0" -> {
                    System.out.println(CYAN + "Exiting." + RESET);
                    return;
                }
                default -> System.out.println(RED + "Invalid option." + RESET);
            }
        }
    }

    private static void printMenu() {
        System.out.println(CYAN + "=== Minecraft World Converter ===" + RESET);
        System.out.println(YELLOW + "1." + RESET + " Convert from " + MAGENTA + "Anvil (.mca)" + RESET + " to " + GREEN + ".slime" + RESET);
        System.out.println(YELLOW + "2." + RESET + " Verify " + GREEN + ".slime" + RESET + " file structure");
        System.out.println(YELLOW + "3." + RESET + " Print out " + BLUE + ".slime format specification" + RESET);
        System.out.println(YELLOW + "0." + RESET + " Exit");
    }

    private static void convertAnvilToSlime(Scanner scanner) {
        System.out.print(YELLOW + "Enter path to Anvil world folder: " + RESET);
        String worldPath = scanner.nextLine().trim();
        System.out.print(YELLOW + "Enter output .slime file path: " + RESET);
        String slimePath = scanner.nextLine().trim();
        File inputWorld = new File(worldPath);
        File outputSlime = new File(slimePath);
        System.out.println(CYAN + "Reading chunks from Anvil world: " + inputWorld.getAbsolutePath() + RESET);
        List<String> globalPalette = new ArrayList<>();
        Map<String, Integer> paletteIndexMap = new HashMap<>();
        List<ChunkData> chunks = AnvilReader.loadChunks(inputWorld);
        System.out.println(CYAN + "Writing Slime world: " + outputSlime.getAbsolutePath() + RESET);
        File levelDat = new File(inputWorld, "level.dat");
        int worldVersion = 3216;
        try {
            if (levelDat.exists()) {
                worldVersion = DataVersionUtil.readDataVersion(levelDat);
                System.out.println(CYAN + "Detected world DataVersion: " + worldVersion + RESET);
            } else {
                System.out.println(YELLOW + "Warning: level.dat not found, using default DataVersion 3216" + RESET);
            }
            SlimeWorldWriter.write(chunks, worldVersion, outputSlime);
            System.out.println(GREEN + "Finished writing Slime world: " + outputSlime.getAbsolutePath() + RESET);
        } catch (Exception e) {
            System.err.println(RED + "Failed to write Slime world" + RESET);
            e.printStackTrace();
        }
    }

    private static void verifySlime(Scanner scanner) {
        System.out.print(YELLOW + "Enter path to .slime file: " + RESET);
        String slimePath = scanner.nextLine().trim();
        File slimeFile = new File(slimePath);
        if (!slimeFile.exists()) {
            System.out.println(RED + ".slime file not found." + RESET);
            return;
        }
        try {
            boolean valid = SlimeVerifier.verify(slimeFile);
            if (valid) {
                System.out.println(GREEN + ".slime file structure is valid." + RESET);
            } else {
                System.out.println(RED + ".slime file is invalid or corrupted." + RESET);
            }
        } catch (Exception e) {
            System.out.println(RED + "Error verifying .slime file:" + RESET);
            e.printStackTrace();
        }
    }

    private static void printSpec() {
        System.out.println(BLUE + "\n--- .slime Format Specification ---" + RESET);
        System.out.println(CYAN + "2 bytes - magic = 0xB10B\n1 byte - version = 0x0C\n4 bytes - world version (int)\n4 bytes - compressed chunks size (int)\n4 bytes - uncompressed chunks size (int)\n<chunks zstd>\n4 bytes - compressed extra size\n4 bytes - uncompressed extra size\n<extra zstd>\n\nChunk Format:\nint chunkX\nint chunkZ\nint sectionCount\n[for each section]\nbyte hasSkyLight\nif true → 2048 byte skylight\nbyte hasBlockLight\nif true → 2048 byte blocklight\nint blockStateSize\n<block states>\nint biomeSize\n<biomes>\nint heightmapSize\n<heightmap nbt compound array>\nint tileEntitiesSize\n<NBT list of tile entities in compound>\nint entitiesSize\n<NBT list of entities in compound, optional CustomId>\n[optional] NBT compound tag for PDC or custom\n" + RESET);
    }
}
