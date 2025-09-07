package gameblock.game.os;

import gameblock.util.rendering.ColorF;
import gameblock.util.physics.Vec2i;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class GameblockLogoRenderer {
    protected static final ArrayList<Vec2i> TITLE_BLOCKS = new ArrayList<>(List.of(
            // G
            new Vec2i(-19, 0),
            new Vec2i(-18, 0),
            new Vec2i(-17, 1),
            new Vec2i(-17, 2),
            new Vec2i(-18, 2),
            new Vec2i(-20, 1),
            new Vec2i(-20, 2),
            new Vec2i(-20, 3),
            new Vec2i(-20, 4),
            new Vec2i(-19, 5),
            new Vec2i(-18, 5),

            // A
            new Vec2i(-15, 0),
            new Vec2i(-15, 1),
            new Vec2i(-15, 2),
            new Vec2i(-15, 3),
            new Vec2i(-15, 4),

            new Vec2i(-14, 3),
            new Vec2i(-13, 3),

            new Vec2i(-14, 5),
            new Vec2i(-13, 5),

            new Vec2i(-12, 0),
            new Vec2i(-12, 1),
            new Vec2i(-12, 2),
            new Vec2i(-12, 3),
            new Vec2i(-12, 4),

            // M
            new Vec2i(-10, 0),
            new Vec2i(-10, 1),
            new Vec2i(-10, 2),
            new Vec2i(-10, 3),
            new Vec2i(-10, 4),

            new Vec2i(-9, 5),

            new Vec2i(-8, 0),
            new Vec2i(-8, 1),
            new Vec2i(-8, 2),
            new Vec2i(-8, 3),
            new Vec2i(-8, 4),

            new Vec2i(-7, 5),

            new Vec2i(-6, 0),
            new Vec2i(-6, 1),
            new Vec2i(-6, 2),
            new Vec2i(-6, 3),
            new Vec2i(-6, 4),

            // E
            new Vec2i(-4, 0),
            new Vec2i(-4, 1),
            new Vec2i(-4, 2),
            new Vec2i(-4, 3),
            new Vec2i(-4, 4),
            new Vec2i(-4, 5),

            new Vec2i(-3, 0),
            new Vec2i(-2, 0),

            new Vec2i(-3, 2),
            new Vec2i(-2, 2),

            new Vec2i(-3, 5),
            new Vec2i(-2, 5),

            // B
            new Vec2i(0, 0),
            new Vec2i(0, 1),
            new Vec2i(0, 2),
            new Vec2i(0, 3),
            new Vec2i(0, 4),
            new Vec2i(0, 5),

            new Vec2i(1, 0),
            new Vec2i(1, 2),
            new Vec2i(1, 5),

            new Vec2i(2, 0),
            new Vec2i(2, 1),
            new Vec2i(2, 3),
            new Vec2i(2, 4),

            // L
            new Vec2i(4, 0),
            new Vec2i(4, 1),
            new Vec2i(4, 2),
            new Vec2i(4, 3),
            new Vec2i(4, 4),
            new Vec2i(4, 5),

            new Vec2i(5, 0),
            new Vec2i(6, 0),

            // O
            new Vec2i(8, 1),
            new Vec2i(8, 2),
            new Vec2i(8, 3),
            new Vec2i(8, 4),

            new Vec2i(9, 0),
            new Vec2i(9, 5),

            new Vec2i(10, 0),
            new Vec2i(10, 5),

            new Vec2i(11, 1),
            new Vec2i(11, 2),
            new Vec2i(11, 3),
            new Vec2i(11, 4),

            // C
            new Vec2i(13, 1),
            new Vec2i(13, 2),
            new Vec2i(13, 3),
            new Vec2i(13, 4),

            new Vec2i(14, 0),
            new Vec2i(14, 5),

            new Vec2i(15, 0),
            new Vec2i(15, 5),

            // K
            new Vec2i(17, 0),
            new Vec2i(17, 1),
            new Vec2i(17, 2),
            new Vec2i(17, 3),
            new Vec2i(17, 4),
            new Vec2i(17, 5),

            new Vec2i(18, 2),
            new Vec2i(19, 1),
            new Vec2i(20, 0),

            new Vec2i(18, 3),
            new Vec2i(19, 4),
            new Vec2i(20, 5)
    ));
    public static final int BLOCK_WIDTH = 4;
    public static final int BLOCK_FADE_TIME = 10;

    private final GameblockOS os;

    protected GameblockLogoRenderer(GameblockOS os) {
        this.os = os;
    }

    public void render() {
        for (Vec2i block : GameblockLogoRenderer.TITLE_BLOCKS) {
            long time = block.getX() + block.getY() + 50;
            if (os.getGameTime() >= time) {
                time = os.getGameTime() - time;
                float f = Math.min(1.0f, ((float)time + os.getPartialTicks()) / BLOCK_FADE_TIME);

                float wave = 1.0f;
                float waveTime = os.getGameTime() - 85 + os.getPartialTicks();
                if (waveTime >= 0) {
                    wave = Mth.cos(waveTime / 2) / 2 + 0.5f;
                    wave = wave * 0.15f + 0.85f;
                }

                os.drawRectangle(block.getX() * BLOCK_WIDTH, (block.getY() - 2.5f) * BLOCK_WIDTH, // blocks are translated down 2.5 units so they're centered
                        BLOCK_WIDTH, BLOCK_WIDTH,
                        new ColorF(wave, wave, wave, f), 0);
            }
        }
    }
}
