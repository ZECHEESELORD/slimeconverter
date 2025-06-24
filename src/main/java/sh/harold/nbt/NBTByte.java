package sh.harold.nbt;

public final class NBTByte implements NBTTag {
    public final String name;
    public final byte value;
    public NBTByte(String name, byte value) {
        this.name = name;
        this.value = value;
    }
    public String name() { return name; }
}
