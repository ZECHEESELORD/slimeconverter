package dev.harolddoes;

import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;


public final class AnvilChunkParser {

    public static List<ChunkData> readRegion(File mcaFile, List<String> globalPalette, Map<String, Integer> paletteIndexMap) {
        List<ChunkData> chunks = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(mcaFile, "r")) {
            for (int i = 0; i < 1024; i++) {
                raf.seek(i * 4);
                int offset = raf.readUnsignedByte() << 16 | raf.readUnsignedByte() << 8 | raf.readUnsignedByte();
                int sectorCount = raf.readUnsignedByte();
                if (offset == 0 || sectorCount == 0) continue;

                raf.seek(offset * 4096L);
                int length = raf.readInt();
                int compression = raf.readUnsignedByte();

                InputStream is = switch (compression) {
                    case 1 -> new BufferedInputStream(new GZIPInputStream(new FileInputStream(raf.getFD())));
                    case 2 -> new BufferedInputStream(new InflaterInputStream(new FileInputStream(raf.getFD())));
                    default -> throw new IOException("Unknown compression type: " + compression);
                };

                NamedTag tag = new NBTDeserializer(false).fromStream(is);
                CompoundTag root = (CompoundTag) tag.getTag();

                System.out.println("[DEBUG] Chunk " + i + " root keys: " + root.keySet());

                CompoundTag level = root.containsKey("Level") ? root.getCompoundTag("Level") : root;

                System.out.println("[DEBUG] Chunk " + i + " level keys: " + level.keySet());

                if (!level.containsKey("sections")) {
                    System.out.println("[DEBUG] Chunk " + i + " skipped: no Sections");
                    continue;
                }

                ChunkData data = ChunkData.fromNBT(level, globalPalette, paletteIndexMap);
                if (data != null) {
                    chunks.add(data);
                    System.out.println("[DEBUG] Chunk " + i + " parsed successfully");
                }
            }

        } catch (Exception e) {
            System.err.println("[AnvilChunkParser] Failed to parse region " + mcaFile.getName());
            e.printStackTrace();
        }

        return chunks;
    }

    private AnvilChunkParser() {
        throw new AssertionError("Utility class");
    }
}