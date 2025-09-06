package gameblock.game.blockbreak;

import gameblock.util.ColorF;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class BlockBreakParticleManager {
    private final BlockBreakGame game;
    private final ArrayList<Particle> particles = new ArrayList<>();

    protected BlockBreakParticleManager(BlockBreakGame game) {
        this.game = game;
    }

    public void addParticle(float x, float y, float motionX, float motionY, int duration, ColorF color) {
        particles.add(new Particle(x, y, motionX, motionY, duration, color));
    }

    public void tick() {
        for (int i = 0; i < particles.size();) {
            if (particles.get(i).update()) {
                i++;
            } else {
                particles.remove(i);
            }
        }
    }

    public void render() {
        float partialTicks = game.getPartialTicks();
        for (Particle particle : particles) {
            float f = particle.time < 10 ? (float) particle.time / 10 : 1.0f;
            float glowiness = Mth.sin(-partialTicks + particle.time) / 2 + 0.5f;
            glowiness = glowiness * 0.4f + 0.6f;

            ColorF col = particle.color.multiply(new ColorF(glowiness)).withAlpha(f);
            game.drawRectangle(
                    particle.oldX + (particle.x - particle.oldX) * partialTicks,
                    particle.oldY + (particle.y - particle.oldY) * partialTicks, 1.0f, 1.0f,
                    col, 0);
        }
    }

    private class Particle {
        private float x, y;
        private float oldX, oldY;
        private float motionX, motionY;
        private ColorF color;
        private int time;

        private Particle(float x, float y, float motionX, float motionY, int time, ColorF color) {
            this.x = this.oldX = x;
            this.y = this.oldY = y;
            this.motionX = motionX;
            this.motionY = motionY;
            this.time = time;
            this.color = color;
        }

        private boolean update() {
            this.oldX = x;
            this.oldY = y;

            x += motionX;
            y += motionY;

            motionX *= 0.95f;
            motionY *= 0.95f;

            return time-- > 0;
        }
    }
}
