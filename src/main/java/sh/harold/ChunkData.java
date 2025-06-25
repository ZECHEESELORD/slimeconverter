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
        int x = root.get("xPos").filter(NBTInt.class::isInstance).map(NBTInt.class::cast).map(n -> n.value).orElseThrow();
        int z = root.get("zPos").filter(NBTInt.class::isInstance).map(NBTInt.class::cast).map(n -> n.value).orElseThrow();
        List<SectionData> sections = new ArrayList<>();
        Optional<NBTList> nbtSectionsOpt = root.get("sections").filter(NBTList.class::isInstance).map(NBTList.class::cast);
        if (nbtSectionsOpt.isPresent()) {
            for (NBTTag tag : nbtSectionsOpt.get().value) {
                if (!(tag instanceof NBTCompound section)) continue;
                int y = section.get("Y").filter(NBTByte.class::isInstance).map(NBTByte.class::cast).map(n -> n.value).orElse((byte) 0);
                boolean hasSky = section.containsKey("SkyLight");
                byte[] sky = hasSky ? section.get("SkyLight").filter(NBTByteArray.class::isInstance).map(NBTByteArray.class::cast).map(n -> n.value).orElse(null) : null;
                boolean hasBlock = section.containsKey("BlockLight");
                byte[] block = hasBlock ? section.get("BlockLight").filter(NBTByteArray.class::isInstance).map(NBTByteArray.class::cast).map(n -> n.value).orElse(null) : null;
                NBTCompound blockStates = section.get("block_states").filter(NBTCompound.class::isInstance).map(NBTCompound.class::cast).orElse(null);
                NBTCompound biomes = section.get("biomes").filter(NBTCompound.class::isInstance).map(NBTCompound.class::cast).orElse(null);
                sections.add(new SectionData(y, hasSky, sky, hasBlock, block, blockStates, biomes));
            }
        }
        NBTCompound heightmaps = root.get("Heightmaps").filter(NBTCompound.class::isInstance).map(NBTCompound.class::cast).orElse(null);
        List<NBTCompound> tileEntities = extractCompoundList(root, "tileEntities");
        List<NBTCompound> entities = extractCompoundList(root, "entities");
        NBTCompound extra = root.get("extra").filter(NBTCompound.class::isInstance).map(NBTCompound.class::cast).orElse(null);
        return new ChunkData(x, z, sections, heightmaps, tileEntities, entities, extra);
    }

    private static List<NBTCompound> extractCompoundList(NBTCompound root, String key) {
        NBTList list = root.get(key).map(NBTList.class::cast).orElse(null);
        if (list == null) return List.of();
        List<NBTCompound> out = new ArrayList<>();
        for (NBTTag tag : list.value) {
            if (tag instanceof NBTCompound compound) out.add(compound);
        }
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