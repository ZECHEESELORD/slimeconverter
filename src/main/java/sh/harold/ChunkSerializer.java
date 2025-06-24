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

public final class ChunkSerializer {
    private ChunkSerializer() {
        throw new AssertionError("Utility class");
    }

    /**
     * Write a chunk in .slime format (see spec).
     */
    public static void writeChunk(DataOutputStream out, ChunkData chunk) throws IOException {
        out.writeInt(chunk.x());
        out.writeInt(chunk.z());
        List<SectionData> sections = chunk.sections();
        out.writeInt(sections.size());
        for (SectionData section : sections) {
            out.writeBoolean(section.hasSkyLight());
            if (section.hasSkyLight()) out.write(section.skyLight());
            out.writeBoolean(section.hasBlockLight());
            if (section.hasBlockLight()) out.write(section.blockLight());
            // TODO: Implement blockStates/biomes serialization from NBTCompound to byte[]
            // For now, skip or write empty arrays
            out.writeInt(0); // blockStates size
            out.writeInt(0); // biomes size
        }
        byte[] heightmapNbt = nbtToBytes(chunk.heightmaps());
        out.writeInt(heightmapNbt.length);
        out.write(heightmapNbt);
        byte[] tileEntitiesNbt = nbtListToBytes(chunk.tileEntities(), "tileEntities");
        out.writeInt(tileEntitiesNbt.length);
        out.write(tileEntitiesNbt);
        byte[] entitiesNbt = nbtListToBytes(chunk.entities(), "entities");
        out.writeInt(entitiesNbt.length);
        out.write(entitiesNbt);
        if (chunk.extra() != null) {
            byte[] extraNbt = nbtToBytes(chunk.extra());
            out.write(extraNbt);
        }
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