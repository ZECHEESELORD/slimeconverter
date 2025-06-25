package sh.harold;

import sh.harold.nbt.NBTCompound;
import sh.harold.nbt.NBTList;
import sh.harold.nbt.NBTOutput;
import sh.harold.nbt.NBTTag;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class ChunkSerializer {
    private static final Logger LOGGER = Logger.getLogger(ChunkSerializer.class.getName());
    private ChunkSerializer() {
        throw new AssertionError("Utility class");
    }

    /**
     * Write a chunk in .slime format (see spec).
     */
    public static void writeChunk(DataOutputStream out, ChunkData chunk) throws IOException {
        LOGGER.info("[DEBUG] Writing chunk at x=" + chunk.x() + ", z=" + chunk.z());
        out.writeInt(chunk.x());
        out.writeInt(chunk.z());
        List<SectionData> sections = chunk.sections();
        out.writeInt(sections.size());
        LOGGER.info("[DEBUG] Section count: " + sections.size());
        for (int i = 0; i < sections.size(); i++) {
            SectionData section = sections.get(i);
            LOGGER.info("[DEBUG]  Section " + i + ": y=" + section.y() + ", hasSkyLight=" + section.hasSkyLight() + ", hasBlockLight=" + section.hasBlockLight());
            out.writeBoolean(section.hasSkyLight());
            if (section.hasSkyLight()) {
                if (section.skyLight() == null || section.skyLight().length != 2048) {
                    throw new IOException("Section " + i + " skyLight missing or wrong size");
                }
                out.write(section.skyLight());
            }
            out.writeBoolean(section.hasBlockLight());
            if (section.hasBlockLight()) {
                if (section.blockLight() == null || section.blockLight().length != 2048) {
                    throw new IOException("Section " + i + " blockLight missing or wrong size");
                }
                out.write(section.blockLight());
            }
            // Serialize blockStates
            byte[] blockStatesNbt = section.blockStates() != null ? nbtToBytes(section.blockStates()) : new byte[0];
            out.writeInt(blockStatesNbt.length);
            LOGGER.info("[DEBUG]   blockStatesNbt length: " + blockStatesNbt.length);
            if (blockStatesNbt.length > 0) out.write(blockStatesNbt);
            // Serialize biomes
            byte[] biomesNbt = section.biomes() != null ? nbtToBytes(section.biomes()) : new byte[0];
            out.writeInt(biomesNbt.length);
            LOGGER.info("[DEBUG]   biomesNbt length: " + biomesNbt.length);
            if (biomesNbt.length > 0) out.write(biomesNbt);
        }
        byte[] heightmapNbt = nbtToBytes(chunk.heightmaps());
        out.writeInt(heightmapNbt.length);
        LOGGER.info("[DEBUG] heightmapNbt length: " + heightmapNbt.length);
        out.write(heightmapNbt);
        byte[] tileEntitiesNbt = nbtListToBytes(chunk.tileEntities(), "tileEntities");
        out.writeInt(tileEntitiesNbt.length);
        LOGGER.info("[DEBUG] tileEntitiesNbt length: " + tileEntitiesNbt.length);
        out.write(tileEntitiesNbt);
        byte[] entitiesNbt = nbtListToBytes(chunk.entities(), "entities");
        out.writeInt(entitiesNbt.length);
        LOGGER.info("[DEBUG] entitiesNbt length: " + entitiesNbt.length);
        out.write(entitiesNbt);
        byte[] extraNbt = chunk.extra() != null ? nbtToBytes(chunk.extra()) : new byte[0];
        out.writeInt(extraNbt.length);
        LOGGER.info("[DEBUG] extraNbt length: " + extraNbt.length);
        if (extraNbt.length > 0) out.write(extraNbt);
    }

    private static byte[] nbtToBytes(NBTCompound tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new NBTOutput(baos).writeTag(tag);
        return baos.toByteArray();
    }

    private static byte[] nbtListToBytes(List<NBTCompound> list, String name) throws IOException {
        Map<String, NBTTag> root = new HashMap<>();
        List<NBTTag> nbtList = new ArrayList<>(list);
        root.put(name, new NBTList(name, nbtList));
        NBTCompound compound = new NBTCompound(null, root);
        return nbtToBytes(compound);
    }
}