package sh.harold.nbt;

import java.util.List;

public final class NBTList implements NBTTag {
    public final String name;
    public final List<NBTTag> value;

    public NBTList(String name, List<NBTTag> value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }
}
