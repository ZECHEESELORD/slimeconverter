package sh.harold;

import sh.harold.nbt.NBTCompound;
import sh.harold.nbt.NBTInput;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class AnvilChunkParser {
    private AnvilChunkParser() {
        throw new AssertionError("Utility class");
    }

    public static List<ChunkData> readRegion(File mcaFile) {
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
                    case 1 ->
                            new BufferedInputStream(new java.util.zip.GZIPInputStream(new FileInputStream(raf.getFD())));
                    case 2 ->
                            new BufferedInputStream(new java.util.zip.InflaterInputStream(new FileInputStream(raf.getFD())));
                    default -> throw new IOException("Unknown compression type: " + compression);
                };
                NBTCompound root = (NBTCompound) new NBTInput(is).readTag();
                NBTCompound level = root.get("Level").map(NBTCompound.class::cast).orElse(root);
                if (!level.containsKey("sections")) continue;
                ChunkData data = ChunkData.fromNBT(level);
                chunks.add(data);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse region " + mcaFile.getName());
            e.printStackTrace();
        }
        return chunks;
    }
}