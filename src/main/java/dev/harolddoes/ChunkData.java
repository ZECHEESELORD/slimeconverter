package dev.harolddoes;

import net.querz.nbt.tag.ByteArrayTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;

public class ChunkData {

    private final int x;
    private final int z;
    private final byte[] blockIds;
    private final byte[] blockData;
    private final byte[] lighting;

    public ChunkData(int x, int z, byte[] blockIds, byte[] blockData, byte[] lighting) {
        this.x = x;
        this.z = z;
        this.blockIds = blockIds;
        this.blockData = blockData;
        this.lighting = lighting;
    }

    public static ChunkData fromNBT(CompoundTag root) {
        if (!root.containsKey("Level")) return null;

        CompoundTag level = root.getCompoundTag("Level");
        int x = level.getInt("xPos");
        int z = level.getInt("zPos");

        ListTag<?> sections = level.getListTag("Sections");
        if (sections == null || sections.size() == 0) return null;

        // Flattened 16x384x16 arrays
        byte[] blockIds = new byte[16 * 384 * 16];
        byte[] blockData = new byte[blockIds.length];
        byte[] lighting = new byte[blockIds.length];

        for (Object o : sections) {
            if (!(o instanceof CompoundTag section)) continue;
            int y = section.getByte("Y");
            int yOffset = y * 16;

            ByteArrayTag blockTag = section.getByteArrayTag("Blocks");
            ByteArrayTag dataTag = section.getByteArrayTag("Data");
            ByteArrayTag lightTag = section.getByteArrayTag("BlockLight");

            if (blockTag == null || dataTag == null) continue;

            byte[] blocks = blockTag.getValue();
            byte[] data = dataTag.getValue();
            byte[] light = lightTag != null ? lightTag.getValue() : new byte[blocks.length];

            for (int i = 0; i < blocks.length; i++) {
                int cy = yOffset + (i / (16 * 16));
                int cz = (i / 16) % 16;
                int cx = i % 16;
                int index = cx + (cy * 16 + cz * 16 * 384);
                blockIds[index] = blocks[i];
                blockData[index] = data[i];
                lighting[index] = light[i];
            }
        }

        return new ChunkData(x, z, blockIds, blockData, lighting);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public byte[] getBlockIds() {
        return blockIds;
    }

    public byte[] getBlockData() {
        return blockData;
    }

    public byte[] getLighting() {
        return lighting;
    }

    @Override
    public String toString() {
        return "ChunkData[x=" + x + ", z=" + z + "]";
    }
}
