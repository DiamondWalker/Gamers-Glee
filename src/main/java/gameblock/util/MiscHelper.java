package gameblock.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class MiscHelper {
    public static <T> T getValueIfAllAreEqual(Collection<T> list) {
        if (list.isEmpty()) return null;
        Iterator<T> iter = list.iterator();
        T baseValue = iter.next();
        while (iter.hasNext()) {
            if (iter.next() != baseValue) return null;
        }
        return baseValue;
    }

    public static <T> T getValueIfAllAreEqual(T... values) {
        return getValueIfAllAreEqual(Arrays.asList(values));
    }

    public static <T> boolean areAllEqual(Collection<T> list) {
        return getValueIfAllAreEqual(list) != null;
    }

    public static <T> boolean areAllEqual(T... values) {
        return getValueIfAllAreEqual(values) != null;
    }
}
