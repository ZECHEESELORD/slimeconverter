package sh.harold.nbt;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NBTInput {
    private final DataInput in;
    private long bytesRead = 0;
    private final List<Byte> recentBytes = new ArrayList<>();
    private static final int DEBUG_BYTE_DUMP = 32;

    public NBTInput(InputStream in) {
        InputStream trackingIn = new java.io.FilterInputStream(in) {
            @Override
            public int read() throws IOException {
                int b = super.read();
                if (b != -1) trackByte((byte) b);
                return b;
            }
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int n = super.read(b, off, len);
                if (n > 0) for (int i = off; i < off + n; i++) trackByte(b[i]);
                return n;
            }
        };
        this.in = new DataInputStream(trackingIn);
    }

    private void trackByte(byte b) {
        bytesRead++;
        if (recentBytes.size() >= DEBUG_BYTE_DUMP) recentBytes.remove(0);
        recentBytes.add(b);
    }

    public NBTTag readTag() throws IOException {
        byte type = safeReadByte("root tag type");
        if (type == 0) return null;
        if (type != 0x0A) throw debugError("Root tag must be a TAG_Compound (type 0x0A), got: " + type);
        String name = safeReadUTF("root tag name");
        return readPayload(type, name);
    }

    private NBTTag readPayload(byte type, String name) throws IOException {
        try {
            switch (type) {
                case 0x0A:
                    return new NBTCompound(name, readCompound());
                case 0x09:
                    return new NBTList(name, readList());
                case 0x0B:
                    return new NBTLongArray(name, readLongArray());
                case 0x08:
                    return new NBTString(name, safeReadUTF(name));
                case 0x03:
                    return new NBTInt(name, safeReadInt(name));
                case 0x01:
                    return new NBTByte(name, safeReadByte(name));
                case 0x02:
                    return new NBTShort(name, safeReadShort(name));
                case 0x04:
                    return new NBTLong(name, safeReadLong(name));
                case 0x0C:
                    return new NBTIntArray(name, readIntArray());
                case 0x06:
                    return new NBTDouble(name, safeReadDouble(name));
                case 0x05:
                    return new NBTFloat(name, safeReadFloat(name));
                default:
                    throw debugError("Unsupported NBT tag type: " + type + " (name: " + name + ")");
            }
        } catch (IOException e) {
            throw debugError("Failed to read NBT payload for tag '" + name + "' (type: " + type + ")", e);
        }
    }

    private Map<String, NBTTag> readCompound() throws IOException {
        Map<String, NBTTag> map = new HashMap<>();
        while (true) {
            byte type = safeReadByte("compound entry type");
            if (type == 0) break; // TAG_End: no name or payload
            String name;
            try {
                name = safeReadUTF("compound entry name");
            } catch (IOException e) {
                throw debugError("Unexpected EOF or error while reading compound entry name (type: " + type + ") - possible truncated or corrupt NBT compound.", e);
            }
            map.put(name, readPayload(type, name));
        }
        return map;
    }

    private List<NBTTag> readList() throws IOException {
        byte elemType = safeReadByte("list element type");
        int len = safeReadInt("list length");
        if (len < 0) throw debugError("Negative NBT list length: " + len);
        List<NBTTag> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            try {
                list.add(readPayload(elemType, null));
            } catch (IOException e) {
                throw debugError("Failed to read element " + i + " of NBT list (type: " + elemType + ")", e);
            }
        }
        return list;
    }

    private long[] readLongArray() throws IOException {
        int len = safeReadInt("long array length");
        if (len < 0) throw debugError("Negative NBT long array length: " + len);
        long[] arr = new long[len];
        for (int i = 0; i < len; i++) arr[i] = safeReadLong("long array[" + i + "]");
        return arr;
    }

    private int[] readIntArray() throws IOException {
        int len = safeReadInt("int array length");
        if (len < 0) throw debugError("Negative NBT int array length: " + len);
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) arr[i] = safeReadInt("int array[" + i + "]");
        return arr;
    }

    private String readUTF() throws IOException {
        int len = in.readUnsignedShort();
        byte[] buf = new byte[len];
        in.readFully(buf);
        return new String(buf, "UTF-8");
    }

    // --- Sanity-checked wrappers and debug helpers ---

    private byte safeReadByte(String context) throws IOException {
        try {
            byte b = in.readByte();
            trackByte(b);
            return b;
        } catch (IOException e) {
            throw debugError("Failed to read byte (" + context + ")", e);
        }
    }

    private short safeReadShort(String context) throws IOException {
        try {
            short s = in.readShort();
            trackByte((byte) (s >> 8));
            trackByte((byte) s);
            return s;
        } catch (IOException e) {
            throw debugError("Failed to read short (" + context + ")", e);
        }
    }

    private int safeReadInt(String context) throws IOException {
        try {
            int i = in.readInt();
            trackByte((byte) (i >> 24));
            trackByte((byte) (i >> 16));
            trackByte((byte) (i >> 8));
            trackByte((byte) i);
            return i;
        } catch (IOException e) {
            throw debugError("Failed to read int (" + context + ")", e);
        }
    }

    private long safeReadLong(String context) throws IOException {
        try {
            long l = in.readLong();
            for (int j = 7; j >= 0; j--) trackByte((byte) (l >> (8 * j)));
            return l;
        } catch (IOException e) {
            throw debugError("Failed to read long (" + context + ")", e);
        }
    }

    private float safeReadFloat(String context) throws IOException {
        try {
            int i = in.readInt();
            trackByte((byte) (i >> 24));
            trackByte((byte) (i >> 16));
            trackByte((byte) (i >> 8));
            trackByte((byte) i);
            return Float.intBitsToFloat(i);
        } catch (IOException e) {
            throw debugError("Failed to read float (" + context + ")", e);
        }
    }

    private double safeReadDouble(String context) throws IOException {
        try {
            long l = in.readLong();
            for (int j = 7; j >= 0; j--) trackByte((byte) (l >> (8 * j)));
            return Double.longBitsToDouble(l);
        } catch (IOException e) {
            throw debugError("Failed to read double (" + context + ")", e);
        }
    }

    private String safeReadUTF(String context) throws IOException {
        try {
            int len = in.readUnsignedShort();
            if (len < 0 || len > 32767) throw debugError("Invalid UTF string length: " + len + " (" + context + ")");
            byte[] buf = new byte[len];
            in.readFully(buf);
            for (byte b : buf) trackByte(b);
            return new String(buf, "UTF-8");
        } catch (IOException e) {
            throw debugError("Failed to read UTF string (" + context + ")", e);
        }
    }

    private IOException debugError(String msg) {
        return debugError(msg, null);
    }

    private IOException debugError(String msg, Throwable cause) {
        StringBuilder sb = new StringBuilder();
        sb.append("[NBTInput ERROR] ").append(msg).append("\n");
        sb.append("Bytes read: ").append(bytesRead).append("\n");
        sb.append("Recent bytes: ").append(byteDump()).append("\n");
        IOException ex = new IOException(sb.toString(), cause);
        ex.setStackTrace(Thread.currentThread().getStackTrace());
        return ex;
    }

    private String byteDump() {
        StringBuilder sb = new StringBuilder();
        for (Byte b : recentBytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
