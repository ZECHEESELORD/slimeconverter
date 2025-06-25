package sh.harold.nbt;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class NBTOutput {
    private final DataOutput out;

    public NBTOutput(OutputStream out) {
        this.out = new DataOutputStream(out);
    }

    public void writeTag(NBTTag tag) throws IOException {
        if (tag == null) {
            out.writeByte(0); // TAG_End
            return;
        }
        byte type = getType(tag);
        out.writeByte(type);
        writeUTF(tag.name());
        writePayload(type, tag);
    }

    private void writePayload(byte type, NBTTag tag) throws IOException {
        switch (type) {
            case 0x0A -> writeCompound((NBTCompound) tag);
            case 0x09 -> writeList((NBTList) tag);
            case 0x0B -> writeLongArray((NBTLongArray) tag);
            case 0x08 -> writeUTF(((NBTString) tag).value);
            case 0x03 -> out.writeInt(((NBTInt) tag).value);
            case 0x01 -> out.writeByte(((NBTByte) tag).value);
            case 0x02 -> out.writeShort(((NBTShort) tag).value);
            case 0x04 -> out.writeLong(((NBTLong) tag).value);
            case 0x0C -> writeIntArray((NBTIntArray) tag);
            case 0x06 -> out.writeDouble(((NBTDouble) tag).value);
            case 0x05 -> out.writeFloat(((NBTFloat) tag).value);
            default -> throw new IOException("Unsupported NBT tag type: " + type);
        }
    }

    private void writeCompound(NBTCompound tag) throws IOException {
        for (NBTTag v : tag.value.values()) {
            writeTag(v);
        }
        out.writeByte(0); // TAG_End
    }

    private void writeList(NBTList tag) throws IOException {
        byte elemType = tag.value.isEmpty() ? 0 : getType(tag.value.get(0));
        out.writeByte(elemType);
        out.writeInt(tag.value.size());
        for (NBTTag v : tag.value) {
            writePayload(elemType, v);
        }
    }

    private void writeLongArray(NBTLongArray tag) throws IOException {
        out.writeInt(tag.value.length);
        for (long l : tag.value) out.writeLong(l);
    }

    private void writeIntArray(NBTIntArray tag) throws IOException {
        out.writeInt(tag.value.length);
        for (int i : tag.value) out.writeInt(i);
    }

    private void writeUTF(String s) throws IOException {
        byte[] bytes = s.getBytes("UTF-8");
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    private byte getType(NBTTag tag) {
        return switch (tag) {
            case NBTCompound ignored -> 0x0A;
            case NBTList ignored -> 0x09;
            case NBTLongArray ignored -> 0x0B;
            case NBTString ignored -> 0x08;
            case NBTInt ignored -> 0x03;
            case NBTByte ignored -> 0x01;
            case NBTShort ignored -> 0x02;
            case NBTLong ignored -> 0x04;
            case NBTIntArray ignored -> 0x0C;
            case NBTDouble ignored -> 0x06;
            case NBTFloat ignored -> 0x05;
            default -> throw new IllegalArgumentException("Unknown tag: " + tag.getClass());
        };
    }
}
