package gameblock.util;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TileGrid2D<T> {
    private final Object[][] array;
    private final Object defaultValue;
    private final Vec2i origin;
    public final int minX, maxX, minY, maxY;

    public TileGrid2D(int minX, int maxX, int minY, int maxY) {
        this(minX, maxX, minY, maxY, null);
    }

    public TileGrid2D(int minX, int maxX, int minY, int maxY, T defaultValue) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        int xRange = maxX - minX;
        int yRange = maxY - minY;
        array = new Object[xRange + 1][yRange + 1];
        this.defaultValue = defaultValue;
        origin = new Vec2i(-minX, -minY);
    }

    private Vec2i transformCoordinates(int x, int y) {
        Vec2i index = origin.offset(x, y);
        if (index.getX() < 0 || index.getY() < 0) return null;
        if (index.getX() >= array.length) return null;
        if (index.getY() >= array[0].length) return null;
        return index;
    }

    public void set(int x, int y, T value) {
        Vec2i index = transformCoordinates(x, y);
        if (index != null) array[index.getX()][index.getY()] = value;
    }

    /**
     * Update all values in this grid
     * @param func A function that takes the current value at these indices and returns the updated value.
     *             Return the provided value if you want to leave it unchanged.
     */
    public void setAll(Function<T, T> func) {
        for (int x = 0; x < array.length; x++) {
            for (int y = 0; y < array[0].length; y++) {
                array[x][y] = func.apply((T) array[x][y]);
            }
        }
    }

    public T get(int x, int y) {
        Vec2i index = transformCoordinates(x, y);
        return index != null ? (T) array[index.getX()][index.getY()] : (T) defaultValue;
    }

    public void forEach(BiConsumer<Vec2i, T> func) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                func.accept(new Vec2i(x, y), get(x, y));
            }
        }
    }
}
