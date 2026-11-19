package gameblock.game;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameUpdate {
    public final int messageType;
    final List<Object> data;

    GameUpdate(int messageType, List<Object> data) {
        this.messageType = messageType;
        this.data = data;
    }

    public List<Object> getData() {
        return Collections.unmodifiableList(data);
    }


    public static final class Incoming extends GameUpdate {
        private int readIndex = 0;

        public Incoming(int messageType, List<Object> data) {
            super(messageType, data);
        }

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

        public <T> Outgoing add(T data) {
            this.data.add(data);
            return this;
        }
    }
}
