package sh.harold.nbt;

public final class NBTIntArray implements NBTTag {
    public final String name;
    public final int[] value;

    public NBTIntArray(String name, int[] value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }
}
