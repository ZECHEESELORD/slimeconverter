package sh.harold.nbt;

public final class NBTByteArray implements NBTTag {
    public final String name;
    public final byte[] value;
    public NBTByteArray(String name, byte[] value) {
        this.name = name;
        this.value = value;
    }
    public String name() { return name; }
}
