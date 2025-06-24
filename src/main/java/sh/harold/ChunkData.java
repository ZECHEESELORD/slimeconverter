package sh.harold;

import sh.harold.nbt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        int x = ((NBTInt) root.get("xPos").orElseThrow()).value;
        int z = ((NBTInt) root.get("zPos").orElseThrow()).value;
        List<SectionData> sections = new ArrayList<>();
        Optional<NBTList> nbtSectionsOpt = root.get("sections").map(NBTList.class::cast);
        if (nbtSectionsOpt.isPresent()) {
            for (NBTTag tag : nbtSectionsOpt.get().value) {
                NBTCompound section = (NBTCompound) tag;
                int y = ((NBTByte) section.get("Y").orElseThrow()).value;
                boolean hasSky = section.containsKey("SkyLight");
                byte[] sky = hasSky ? ((NBTByteArray) section.get("SkyLight").orElseThrow()).value : null;
                boolean hasBlock = section.containsKey("BlockLight");
                byte[] block = hasBlock ? ((NBTByteArray) section.get("BlockLight").orElseThrow()).value : null;
                NBTCompound blockStates = section.get("block_states").map(NBTCompound.class::cast).orElse(null);
                NBTCompound biomes = section.get("biomes").map(NBTCompound.class::cast).orElse(null);
                sections.add(new SectionData(y, hasSky, sky, hasBlock, block, blockStates, biomes));
            }
        }
        NBTCompound heightmaps = root.get("Heightmaps").map(NBTCompound.class::cast).orElse(null);
        List<NBTCompound> tileEntities = extractCompoundList(root, "tileEntities");
        List<NBTCompound> entities = extractCompoundList(root, "entities");
        NBTCompound extra = root.get("extra").map(NBTCompound.class::cast).orElse(null);
        return new ChunkData(x, z, sections, heightmaps, tileEntities, entities, extra);
    }

    private static List<NBTCompound> extractCompoundList(NBTCompound root, String key) {
        NBTCompound global = root.get(key).map(NBTCompound.class::cast).orElse(null);
        if (global == null) return List.of();
        NBTList list = global.get(key).map(NBTList.class::cast).orElse(null);
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
) {
}