package com.velocitypowered.proxy.protocol.data.chat;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@UtilityClass
public class ChatTagManager {

    public final CompoundBinaryTag CHAT_TYPE_119;
    public final CompoundBinaryTag CHAT_TYPE_1191;
    public final CompoundBinaryTag DAMAGE_TYPE_1194;
    public final CompoundBinaryTag DAMAGE_TYPE_120;

    static {
        try {
            try (InputStream stream = ChatTagManager.class.getClassLoader().getResourceAsStream("mapping/chat_type_1_19.nbt")) {
                CHAT_TYPE_119 = BinaryTagIO.unlimitedReader().read(Objects.requireNonNull(stream), BinaryTagIO.Compression.GZIP);
            }
            try (InputStream stream = ChatTagManager.class.getClassLoader().getResourceAsStream("mapping/chat_type_1_19_1.nbt")) {
                CHAT_TYPE_1191 = BinaryTagIO.unlimitedReader().read(Objects.requireNonNull(stream), BinaryTagIO.Compression.GZIP);
            }
            try (InputStream stream = ChatTagManager.class.getClassLoader().getResourceAsStream("mapping/damage_type_1_19_4.nbt")) {
                DAMAGE_TYPE_1194 = BinaryTagIO.unlimitedReader().read(Objects.requireNonNull(stream), BinaryTagIO.Compression.GZIP);
            }
            try (InputStream stream = ChatTagManager.class.getClassLoader().getResourceAsStream("mapping/damage_type_1_20.nbt")) {
                DAMAGE_TYPE_120 = BinaryTagIO.unlimitedReader().read(Objects.requireNonNull(stream), BinaryTagIO.Compression.GZIP);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
