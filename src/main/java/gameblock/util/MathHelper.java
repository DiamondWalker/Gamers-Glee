package gameblock.util;

public class MathHelper {
    public static boolean hasSameSign(byte i1, byte i2) {
        return i1 / Math.abs(i1) == i2 / Math.abs(i2);
    }

    public static boolean hasSameSign(short i1, short i2) {
        return i1 / Math.abs(i1) == i2 / Math.abs(i2);
    }
    public static boolean hasSameSign(int i1, int i2) {
        return i1 / Math.abs(i1) == i2 / Math.abs(i2);
    }

    public static boolean hasSameSign(long i1, long i2) {
        return i1 / Math.abs(i1) == i2 / Math.abs(i2);
    }

    public static boolean hasSameSign(float f1, float f2) {
        return Math.round(Math.signum(f1)) == Math.round(Math.signum(f2));
    }

    public static boolean hasSameSign(double f1, double f2) {
        return Math.round(Math.signum(f1)) == Math.round(Math.signum(f2));
    }
}
