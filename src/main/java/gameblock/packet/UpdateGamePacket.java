package gameblock.packet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gameblock.game.GameInstance;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateGamePacket {
    //private static final HashBiMap<Class<?>, Byte> DATA_TYPES = HashBiMap.create();
    private static final ArrayList<Triple<Class<?>, BiConsumer<FriendlyByteBuf, Object>, Function<FriendlyByteBuf, Object>>> DATA_TYPES = new ArrayList<>();

    private static <T> void addType(Class<T> clazz, BiConsumer<FriendlyByteBuf, Object> writer, Function<FriendlyByteBuf, Object> reader) {
        DATA_TYPES.add(Triple.of(clazz, writer, reader));
        //DATA_TYPES.put(clazz, (byte)DATA_TYPES.size());
    }

    static {
        addType(Byte.class, (buf, b) -> buf.writeByte((byte)b), ByteBuf::readByte);
        addType(Short.class, (buf, s) -> buf.writeShort((short)s), ByteBuf::readShort);
        addType(Integer.class, (buf, i) -> buf.writeInt((int)i), ByteBuf::readInt);
        addType(Long.class, (buf, l) -> buf.writeLong((long)l), ByteBuf::readLong);
        addType(Float.class, (buf, f) -> buf.writeFloat((float)f), ByteBuf::readFloat);
        addType(Double.class, (buf, d) -> buf.writeDouble((double)d), ByteBuf::readDouble);
        addType(Boolean.class, (buf, bool) -> buf.writeBoolean((boolean) bool), ByteBuf::readBoolean);
        addType(Character.class, (buf, character) -> buf.writeChar((char) character), ByteBuf::readChar);
        addType(String.class, (buf, string) -> buf.writeUtf((String) string), FriendlyByteBuf::readUtf);
        addType(UUID.class, (buf, uuid) -> buf.writeUUID((UUID) uuid), FriendlyByteBuf::readUUID);
    }

    public int messageType;
    private final ArrayList<Object> data = new ArrayList<>();

    private int readIndex = 0;

    public UpdateGamePacket(int messageType) {
        this.messageType = messageType;

    }

    public UpdateGamePacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    public <T> UpdateGamePacket add(T data) {
        this.data.add(data);
        return this;
    }

    public <T> T get(Class<T> clazz) {
        Object current = data.get(readIndex++);
        if (!current.getClass().equals(clazz)) throw new IllegalStateException("Incorrectly expected type " + clazz.getName() + ". Provided type " + current.getClass().getName());
        return clazz.cast(current);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeByte(messageType);
        buffer.writeInt(data.size());

        DATA_LOOP: for (Object var : data) {
            Class<?> clazz = var.getClass();
            for (byte b = 0; b < DATA_TYPES.size(); b++) {
                if (DATA_TYPES.get(b).getLeft().equals(clazz)) {
                    buffer.writeByte(b);
                    DATA_TYPES.get(b).getMiddle().accept(buffer, var);
                    continue DATA_LOOP;
                }
            }

            throw new IllegalStateException(clazz.getName() + " is not an accepted data type for a game update packet!");
        }
    }

    public void readFromBuffer(FriendlyByteBuf buffer) {
        messageType = buffer.readByte();
        int dataCount = buffer.readInt();

        for (int i = 0; i < dataCount; i++) {
            byte type = buffer.readByte();
            data.add(DATA_TYPES.get(type).getRight().apply(buffer));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> ClientPacketHandler.handleUpdateGamePacket(this));
        } else {
            context.get().enqueueWork(() -> ServerPacketHandler.handleUpdateGamePacket(this, context.get().getSender()));
        }
        context.get().setPacketHandled(true);
    }
}
