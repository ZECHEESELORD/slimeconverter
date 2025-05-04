package dev.harolddoes;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.LongArrayTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkData {

    private final int x;
    private final int z;
    private final int[] paletteIndices;

    public ChunkData(int x, int z, int[] paletteIndices) {
        this.x = x;
        this.z = z;
        this.paletteIndices = paletteIndices;
    }

    public static ChunkData fromNBT(CompoundTag root, List<String> globalPalette, Map<String, Integer> paletteIndexMap) {
        int x = root.getInt("xPos");
        int z = root.getInt("zPos");

        ListTag<?> sections = root.getListTag("sections");
        if (sections == null || sections.size() == 0) return null;

        int[] paletteIndices = new int[16 * 384 * 16]; // XZY

        for (Object o : sections) {
            if (!(o instanceof CompoundTag section)) continue;
            int y = section.getByte("Y");
            if (!section.containsKey("block_states")) continue;

            CompoundTag blockStatesTag = section.getCompoundTag("block_states");
            ListTag<?> rawPalette = blockStatesTag.getListTag("palette");
            if (rawPalette == null || rawPalette.size() == 0) continue;

            LongArrayTag dataTag = blockStatesTag.getLongArrayTag("data");
            if (dataTag == null) continue;

            List<String> localPalette = new ArrayList<>();
            for (Object entry : rawPalette) {
                if (entry instanceof CompoundTag compound) {
                    String id = compound.getString("Name");
                    if (!paletteIndexMap.containsKey(id)) {
                        paletteIndexMap.put(id, globalPalette.size());
                        globalPalette.add(id);
                    }
                    localPalette.add(id);
                } else {
                    localPalette.add("minecraft:air");
                }
            }

            long[] rawData = dataTag.getValue();
            int bitsPerBlock = Math.max(4, (int) Math.ceil(Math.log(localPalette.size()) / Math.log(2)));
            int blocksPerLong = 64 / bitsPerBlock;
            int mask = (1 << bitsPerBlock) - 1;

            for (int i = 0; i < 4096; i++) {
                int wordIndex = i / blocksPerLong;
                int bitIndex = (i % blocksPerLong) * bitsPerBlock;
                if (wordIndex >= rawData.length) break;

                long word = rawData[wordIndex];
                int localIndex = (int) ((word >> bitIndex) & mask);
                String state = localIndex < localPalette.size() ? localPalette.get(localIndex) : "minecraft:air";
                int globalIndex = paletteIndexMap.getOrDefault(state, 0);

                int sx = i % 16;
                int sy = i / 256;
                int sz = (i / 16) % 16;
                int globalY = y * 16 + sy;
                int index = sx + sz * 16 + globalY * 256;

                if (index >= 0 && index < paletteIndices.length) {
                    paletteIndices[index] = globalIndex;
                }
            }
        }

        return new ChunkData(x, z, paletteIndices);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int[] getPaletteIndices() {
        return paletteIndices;
    }

    @Override
    public String toString() {
        return "ChunkData[x=" + x + ", z=" + z + "]";
    }
}
