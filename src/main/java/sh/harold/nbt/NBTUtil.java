package sh.harold.nbt;

import net.querz.nbt.tag.*;
import java.util.*;

public final class NBTUtil {
    public static NBTTag convertQuerzToInternal(Tag<?> tag) {
        if (tag instanceof CompoundTag ct) {
            Map<String, NBTTag> map = new HashMap<>();
            for (String key : ct.keySet()) {
                map.put(key, convertQuerzToInternal(ct.get(key)));
            }
            return new NBTCompound(null, map);
        }
        if (tag instanceof ListTag<?> lt) {
            List<NBTTag> list = new ArrayList<>();
            for (Object o : lt) list.add(convertQuerzToInternal((Tag<?>) o));
            return new NBTList(null, list);
        }
        if (tag instanceof LongArrayTag lat) {
            return new NBTLongArray(null, lat.getValue());
        }
        if (tag instanceof IntArrayTag iat) {
            return new NBTIntArray(null, iat.getValue());
        }
        if (tag instanceof ByteArrayTag bat) {
            return new NBTByteArray(null, bat.getValue());
        }
        if (tag instanceof StringTag st) {
            return new NBTString(null, st.getValue());
        }
        if (tag instanceof IntTag it) {
            return new NBTInt(null, it.asInt());
        }
        if (tag instanceof ByteTag bt) {
            return new NBTByte(null, bt.asByte());
        }
        if (tag instanceof ShortTag st) {
            return new NBTShort(null, st.asShort());
        }
        if (tag instanceof LongTag lt) {
            return new NBTLong(null, lt.asLong());
        }
        return null;
    }
}
