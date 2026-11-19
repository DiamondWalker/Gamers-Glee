package gameblock.game;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameUpdate {
    public final int messageType;
    final List<Object> data;

    private GameUpdate(int messageType, List<Object> data) {
        this.messageType = messageType;
        this.data = data;
    }

    /**
     * Retrieves a read-only list of the data attached to this update
     * This is meant to be used only by the {@link gameblock.packet.GameUpdatePacket}. Use {@link Incoming#get(Class) for actually reading incoming data}
     * @return An unmodifiable list containing the data for this update
     */
    public List<Object> getData() {
        return Collections.unmodifiableList(data);
    }


    public static final class Incoming extends GameUpdate {
        private int readIndex = 0;

        public Incoming(int messageType, List<Object> data) {
            super(messageType, data);
        }

        /**
         * Retrieves an object from this update's data
         *
         * @param clazz The class data type to be read. Will throw an {@link IllegalStateException} if the class does not match
         * @return The object read
         * @param <T> The type to be read
         */
        public <T> T get(Class<T> clazz) {
            Object current = data.get(readIndex++);
            if (!current.getClass().equals(clazz)) throw new IllegalStateException("Incorrectly expected type " + clazz.getName() + ". Provided type " + current.getClass().getName());
            return clazz.cast(current);
        }
    }


    public static final class Outgoing extends GameUpdate {
        public Outgoing(int messageType) {
            super(messageType, new ArrayList<>());
        }

        /**
         * Adds the given object to the data to be sent for this update
         *
         * @param data The object to be sent
         * @return The update instance for method chaining
         * @param <T> The data type
         */
        public <T> Outgoing add(T data) {
            this.data.add(data);
            return this;
        }
    }
}
