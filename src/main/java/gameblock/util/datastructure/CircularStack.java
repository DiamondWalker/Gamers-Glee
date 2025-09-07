package gameblock.util.datastructure;

import java.util.function.Consumer;

public class CircularStack<T> {
    private T[] data;
    private int currentIndex = 0;
    private int count = 0;

    public CircularStack(int size) {
        data = (T[]) new Object[size];
    }

    public void enqueue(T value) {
        data[currentIndex] = value;
        currentIndex = (currentIndex + 1) % data.length;
        if (count < data.length) count++;
    }

    public T dequeue() {
        if (isEmpty()) return null;
        currentIndex = getPreviousIndex();
        count--;
        T val = data[currentIndex];
        return val;
    }

    public T peek() {
        if (isEmpty()) return null;
        return data[getPreviousIndex()];
    }

    public void forEach(Consumer<T> func) {
        int c = 0;
        int index = getPreviousIndex();

        for (int i = index; i >= 0; i--) {
            if (c++ == count) return;
            func.accept(data[i]);
        }
        for (int i = data.length - 1; i > index; i--) {
            if (c++ == count) return;
            func.accept(data[i]);
        }
    }

    public void clear() {
        /*
        although this doesn't actually remove the elements internally, they will be gradually overwritten and garbage collected
        the only alternative would be to iterate through the elements setting everything null, or create a new array altogether
        both of these could cause slowdown for very large queues
         */
        count = 0;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    private int getPreviousIndex() {
        return currentIndex == 0 ? data.length - 1 : currentIndex - 1;
    }
}
