package sh.harold.nbt;

public final class NBTLongArray implements NBTTag {
    public final String name;
    public final long[] value;
    public NBTLongArray(String name, long[] value) {
        this.name = name;
        this.value = value;
    }
    public String name() { return name; }
}
