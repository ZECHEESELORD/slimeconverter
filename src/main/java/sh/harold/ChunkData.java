package sh.harold;

import sh.harold.nbt.*;
import java.util.*;

public record ChunkData(
    int x,
    int z,
    List<SectionData> sections,
    NBTCompound heightmaps,
    List<NBTCompound> tileEntities,
    List<NBTCompound> entities,
    NBTCompound extra
) {
    public static ChunkData fromNBT(NBTCompound root) {
        int x = ((NBTInt) root.value.get("xPos")).value;
        int z = ((NBTInt) root.value.get("zPos")).value;
        List<SectionData> sections = new ArrayList<>();
        NBTList nbtSections = (NBTList) root.value.get("sections");
        if (nbtSections != null) {
            for (NBTTag tag : nbtSections.value) {
                NBTCompound section = (NBTCompound) tag;
                int y = ((NBTByte) section.value.get("Y")).value;
                boolean hasSky = section.value.containsKey("SkyLight");
                byte[] sky = hasSky ? ((NBTByteArray) section.value.get("SkyLight")).value : null;
                boolean hasBlock = section.value.containsKey("BlockLight");
                byte[] block = hasBlock ? ((NBTByteArray) section.value.get("BlockLight")).value : null;
                NBTCompound blockStates = (NBTCompound) section.value.get("block_states");
                NBTCompound biomes = (NBTCompound) section.value.get("biomes");
                sections.add(new SectionData(y, hasSky, sky, hasBlock, block, blockStates, biomes));
            }
        }
        NBTCompound heightmaps = (NBTCompound) root.value.get("Heightmaps");
        List<NBTCompound> tileEntities = extractCompoundList(root, "tileEntities");
        List<NBTCompound> entities = extractCompoundList(root, "entities");
        NBTCompound extra = (NBTCompound) root.value.getOrDefault("extra", null);
        return new ChunkData(x, z, sections, heightmaps, tileEntities, entities, extra);
    }
    private static List<NBTCompound> extractCompoundList(NBTCompound root, String key) {
        NBTCompound global = (NBTCompound) root.value.get(key);
        if (global == null) return List.of();
        NBTList list = (NBTList) global.value.get(key);
        if (list == null) return List.of();
        List<NBTCompound> out = new ArrayList<>();
        for (NBTTag tag : list.value) out.add((NBTCompound) tag);
        return out;
    }
}

record SectionData(
    int y,
    boolean hasSkyLight,
    byte[] skyLight,
    boolean hasBlockLight,
    byte[] blockLight,
    NBTCompound blockStates,
    NBTCompound biomes
) {}