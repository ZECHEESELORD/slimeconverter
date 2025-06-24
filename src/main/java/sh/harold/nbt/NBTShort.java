package sh.harold.nbt;

public final class NBTShort implements NBTTag {
    public final String name;
    public final short value;

    public NBTShort(String name, short value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }
}
