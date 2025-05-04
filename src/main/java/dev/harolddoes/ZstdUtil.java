package dev.harolddoes;

import com.github.luben.zstd.Zstd;

public final class ZstdUtil {

    private ZstdUtil() {
        throw new AssertionError("Utility class");
    }

    public static byte[] compress(byte[] data) {
        return Zstd.compress(data);
    }

}