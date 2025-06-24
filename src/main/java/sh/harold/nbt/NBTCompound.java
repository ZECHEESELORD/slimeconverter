package sh.harold.nbt;

import java.util.Map;

public final class NBTCompound implements NBTTag {
    public final String name;
    public final Map<String, NBTTag> value;
    public NBTCompound(String name, Map<String, NBTTag> value) {
        this.name = name;
        this.value = value;
    }
    public String name() { return name; }
}
