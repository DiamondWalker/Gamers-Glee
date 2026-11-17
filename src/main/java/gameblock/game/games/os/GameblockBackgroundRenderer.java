package gameblock.game.games.os;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gameblock.util.CircularStack;
import gameblock.util.ColorF;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

import java.util.Random;

public class GameblockBackgroundRenderer {
    private final GameblockOS os;
    private final CircularStack<BackgroundBlock> cubes = new CircularStack<>(150);

    protected GameblockBackgroundRenderer(GameblockOS os) {
        this.os = os;
    }

    public void tick() {
        if (os.getGameTime() % 10 == 0) cubes.enqueue(new BackgroundBlock());
    }

    public void render(float partialTicks) {
        os.renderer.drawRectangle(0, 0, 200, 200, new ColorF(1, 18, 23, 255), 0);

        cubes.forEach((BackgroundBlock block) -> {
            for (int i = 0; i < 8; i++) {
                float time = partialTicks + os.getGameTime() - block.timeStart;
                time -= i * 10;
                float scale = (time * time) / 30000;
                float alpha = Mth.clamp((80 - (time - 1200)) / 80, 0.0f, 1.0f);
                alpha *= (1.0f - (float)i / 8);

                os.renderer.push();
                os.renderer.rotateRadians((partialTicks + os.getGameTime() - i * 10) / 400);
                os.renderer.scale(scale);
                os.renderer.translate(block.x, block.y);
                os.renderer.drawRectangle(RenderType.gui(), 0,0, 0.2f, 0.2f, new ColorF(5, 129, 62).withAlpha(0.8f * alpha), 0);
                os.renderer.pop();
            }
        });
    }

    private class BackgroundBlock {
        private float x;
        private float y;
        private long timeStart;

        private BackgroundBlock() {
            Random rand = new Random();
            float magnitude = rand.nextFloat() * 7.071f;
            float angle = rand.nextFloat(Mth.TWO_PI);
            this.x = Mth.cos(angle) * magnitude;
            this.y = Mth.sin(angle) * magnitude;
            this.timeStart = os.getGameTime();
        }
    }
}
