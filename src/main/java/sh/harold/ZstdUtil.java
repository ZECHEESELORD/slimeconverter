package sh.harold;

import com.github.luben.zstd.Zstd;

public final class ZstdUtil {

    private ZstdUtil() {
        throw new AssertionError("Utility class");
    }

    public static byte[] compress(byte[] data) {
        return Zstd.compress(data);
    }

    public static byte[] decompress(byte[] data, int uncompressedSize) {
        return Zstd.decompress(data, uncompressedSize);
    }
}