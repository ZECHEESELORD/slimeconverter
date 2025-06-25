package sh.harold.nbt;

public sealed interface NBTTag permits NBTCompound, NBTList, NBTLongArray, NBTString, NBTInt, NBTByte, NBTShort, NBTLong, NBTIntArray, NBTByteArray, NBTDouble, NBTFloat {
    String name();
}
