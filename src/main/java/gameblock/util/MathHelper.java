package gameblock.util;

public class MathHelper {
    public static byte getSign(byte num) {
        return (byte) (num / Math.abs(num));
    }

    public static byte getSign(short num) {
        return (byte) (num / Math.abs(num));
    }

    public static byte getSign(int num) {
        return (byte) (num / Math.abs(num));
    }

    public static byte getSign(long num) {
        return (byte) (num / Math.abs(num));
    }

    public static byte getSign(float num) {
        return (byte) Math.round(Math.signum(num));
    }

    public static byte getSign(double num) {
        return (byte) Math.round(Math.signum(num));
    }

    public static boolean hasSameSign(byte i1, byte i2) {
        return getSign(i1) == getSign(i2);
    }

    public static boolean hasSameSign(short i1, short i2) {
        return getSign(i1) == getSign(i2);
    }
    public static boolean hasSameSign(int i1, int i2) {
        return getSign(i1) == getSign(i2);
    }

    public static boolean hasSameSign(long i1, long i2) {
        return getSign(i1) == getSign(i2);
    }

    public static boolean hasSameSign(float f1, float f2) {
        return getSign(f1) == getSign(f2);
    }

    public static boolean hasSameSign(double f1, double f2) {
        return getSign(f1) == getSign(f2);
    }
}
