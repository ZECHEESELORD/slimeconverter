package sh.harold;

import java.io.*;
import java.util.zip.Inflater;

public final class SlimeVerifier {
    private SlimeVerifier() {
        throw new AssertionError("Utility class");
    }

    public static boolean verify(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            // Header
            if (in.readUnsignedShort() != 0xB10B) return false;
            if (in.readUnsignedByte() != 0x0C) return false;
            in.readInt(); // world version
            int chunkCompressed = in.readInt();
            int chunkUncompressed = in.readInt();
            if (chunkCompressed <= 0 || chunkUncompressed <= 0) return false;
            byte[] chunkData = new byte[chunkCompressed];
            in.readFully(chunkData);
            if (!verifyZstd(chunkData, chunkUncompressed)) return false;
            int extraCompressed = in.readInt();
            int extraUncompressed = in.readInt();
            if (extraCompressed < 0 || extraUncompressed < 0) return false;
            if (extraCompressed > 0) {
                byte[] extraData = new byte[extraCompressed];
                in.readFully(extraData);
                if (!verifyZstd(extraData, extraUncompressed)) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean verifyZstd(byte[] compressed, int expectedUncompressed) {
        try {
            // Use Zstd if available, else stub with Inflater for placeholder
            Inflater inflater = new Inflater();
            inflater.setInput(compressed);
            byte[] buf = new byte[expectedUncompressed];
            int len = inflater.inflate(buf);
            inflater.end();
            return len == expectedUncompressed;
        } catch (Exception e) {
            return false;
        }
    }
}
