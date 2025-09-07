package gameblock.util.rendering;

import net.minecraft.util.Mth;

public class ColorF {
    private float red, green, blue, alpha;

    public ColorF(int whiteness) {
        this((float)whiteness / 255);
    }

    public ColorF(float whiteness) {
        this(whiteness, whiteness, whiteness);
    }

    public ColorF(float red, float green, float blue) {
        this(red, green, blue, 1.0f);
    }

    public ColorF(float red, float green, float blue, float alpha) {
        this.red = Mth.clamp(red, 0.0f, 1.0f);
        this.green = Mth.clamp(green, 0.0f, 1.0f);
        this.blue = Mth.clamp(blue, 0.0f, 1.0f);
        this.alpha = Mth.clamp(alpha, 0.0f, 1.0f);
    }

    public ColorF(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public ColorF(int red, int green, int blue, int alpha) {
        this((float)red / 255, (float)green / 255, (float)blue / 255, (float)alpha / 255);
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public ColorF withRed(int col) {
        return new ColorF((float)col / 255, green, blue, alpha);
    }

    public ColorF withGreen(int col) {
        return new ColorF(red, (float)col / 255, blue, alpha);
    }

    public ColorF withBlue(int col) {
        return new ColorF(red, green, (float)col / 255, alpha);
    }

    public ColorF withAlpha(int col) {
        return new ColorF(red, green, blue, (float)col / 255);
    }

    public ColorF withRed(float f) {
        return new ColorF(f, green, blue, alpha);
    }

    public ColorF withGreen(float f) {
        return new ColorF(red, f, blue, alpha);
    }

    public ColorF withBlue(float f) {
        return new ColorF(red, green, f, alpha);
    }

    public ColorF withAlpha(float f) {
        return new ColorF(red, green, blue, f);
    }

    public ColorF multiply(ColorF other) {
        return new ColorF(this.red * other.red, this.green * other.green, this.blue * other.blue, this.alpha * other.alpha);
    }

    public ColorF fadeTo(ColorF other, float lerp) {
        return new ColorF(
                (other.red - this.red) * lerp + this.red,
                (other.green - this.green) * lerp + this.green,
                (other.blue - this.blue) * lerp + this.blue,
                (other.alpha - this.alpha) * lerp + this.alpha
        );
    }
}
