package sh.harold.nbt;

import java.io.*;
import java.util.*;

public final class NBTInput {
    private final DataInput in;
    public NBTInput(InputStream in) { this.in = new DataInputStream(in); }

    public NBTTag readTag() throws IOException {
        byte type = in.readByte();
        if (type == 0) return null;
        String name = readUTF();
        return readPayload(type, name);
    }

    private NBTTag readPayload(byte type, String name) throws IOException {
        switch (type) {
            case 0x0A: return new NBTCompound(name, readCompound());
            case 0x09: return new NBTList(name, readList());
            case 0x0B: return new NBTLongArray(name, readLongArray());
            case 0x08: return new NBTString(name, readUTF());
            case 0x03: return new NBTInt(name, in.readInt());
            case 0x01: return new NBTByte(name, in.readByte());
            case 0x02: return new NBTShort(name, in.readShort());
            case 0x04: return new NBTLong(name, in.readLong());
            case 0x0C: return new NBTIntArray(name, readIntArray());
            default: throw new IOException("Unsupported NBT tag type: " + type);
        }
    }

    private Map<String, NBTTag> readCompound() throws IOException {
        Map<String, NBTTag> map = new HashMap<>();
        while (true) {
            byte type = in.readByte();
            if (type == 0) break;
            String name = readUTF();
            map.put(name, readPayload(type, name));
        }
        return map;
    }

    private List<NBTTag> readList() throws IOException {
        byte elemType = in.readByte();
        int len = in.readInt();
        List<NBTTag> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(readPayload(elemType, null));
        }
        return list;
    }

    private long[] readLongArray() throws IOException {
        int len = in.readInt();
        long[] arr = new long[len];
        for (int i = 0; i < len; i++) arr[i] = in.readLong();
        return arr;
    }

    private int[] readIntArray() throws IOException {
        int len = in.readInt();
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) arr[i] = in.readInt();
        return arr;
    }

    private String readUTF() throws IOException {
        int len = in.readUnsignedShort();
        byte[] buf = new byte[len];
        in.readFully(buf);
        return new String(buf, "UTF-8");
    }
}
