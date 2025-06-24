package sh.harold.nbt;

public final class NBTString implements NBTTag {
    public final String name;
    public final String value;

    public NBTString(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }
}
