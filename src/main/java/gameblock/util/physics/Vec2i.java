package gameblock.util.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;

import java.util.stream.IntStream;

public class Vec2i implements Comparable<Vec2i> {
    public static final Codec<Vec2i> CODEC = Codec.INT_STREAM.comapFlatMap((p_123318_) -> {
        return Util.fixedSize(p_123318_, 2).map((p_175586_) -> {
            return new Vec2i(p_175586_[0], p_175586_[1]);
        });
    }, (p_123313_) -> {
        return IntStream.of(p_123313_.getX(), p_123313_.getY());
    });

    public static final Vec2i ZERO = new Vec2i(0, 0);

    private int x;
    private int y;

    public static Codec<Vec2i> offsetCodec(int pMaxOffset) {
        return ExtraCodecs.validate(CODEC, (p_274739_) -> {
            return Math.abs(p_274739_.getX()) < pMaxOffset && Math.abs(p_274739_.getY()) < pMaxOffset ? DataResult.success(p_274739_) : DataResult.error(() -> {
                return "Position out of range, expected at most " + pMaxOffset + ": " + p_274739_;
            });
        });
    }

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else if (!(pOther instanceof Vec3i)) {
            return false;
        } else {
            Vec3i vec3i = (Vec3i)pOther;
            if (this.getX() != vec3i.getX()) {
                return false;
            } else {
                return this.getY() != vec3i.getY();
            }
        }
    }

    public int hashCode() {
        return this.getY() * 31 + this.getX();
    }

    @Override
    public int compareTo(Vec2i pOther) {
        return this.getY() == pOther.getY() ? this.getX() - pOther.getX() : this.getY() - pOther.getY();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    protected Vec2i setX(int pX) {
        this.x = pX;
        return this;
    }

    protected Vec2i setY(int pY) {
        this.y = pY;
        return this;
    }

    public Vec2i offset(int x, int y) {
        return new Vec2i(this.x + x, this.y + y);
    }

    public Vec2i offset(Vec2i offset) {
        return offset(offset.x, offset.y);
    }

    public Vec2i scale(int scale) {
        return new Vec2i(x * scale, y * scale);
    }
}
