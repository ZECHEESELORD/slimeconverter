package sh.harold;

import sh.harold.nbt.*;
import java.io.*;

public final class DataVersionUtil {
    public static int readDataVersion(File levelDat) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(levelDat)))) {
            // level.dat is GZIP compressed NBT
            try (java.util.zip.GZIPInputStream gzip = new java.util.zip.GZIPInputStream(in)) {
                NBTCompound root = (NBTCompound) new NBTInput(gzip).readTag();
                NBTCompound data = root.get("Data").map(NBTCompound.class::cast).orElse(root);
                return ((NBTInt) data.get("DataVersion").orElseThrow()).value;
            }
        }
    }
    private DataVersionUtil() { throw new AssertionError("Utility class"); }
}
