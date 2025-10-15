package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import gameblock.util.rendering.PlayerMotionTracker;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class RainbowTrailParticleCosmetic extends BaseParticleCosmetic {
    private final PlayerMotionTracker playerTracker;
    public RainbowTrailParticleCosmetic(Player player) {
        super(player, GameblockCosmetics.RAINBOW_TRAIL);
        playerTracker = new PlayerMotionTracker(player);
    }

    @Override
    public void tick() {
        playerTracker.tick();

        if (playerTracker.hasPlayerMoved()) {
            float time = (float) player.tickCount / 100;
            int colorCode = Mth.hsvToRgb(time % 1.0f, 1.0f, 1.0f);
            Vector3f color = new Vector3f(FastColor.ARGB32.red(colorCode), FastColor.ARGB32.green(colorCode), FastColor.ARGB32.blue(colorCode));
            color = color.mul(1.0f / 255);
            for (int i = 0; i < 8; i++) {
                Vec3 playerPos = player.position();
                playerPos = playerPos.add(
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth(),
                        player.getRandom().nextFloat() * player.getBbHeight(),
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth()
                );

                player.level().addParticle(new DustParticleOptions(color, 2.0f), playerPos.x, playerPos.y, playerPos.z, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean displaysInFirstPerson() {
        return false;
    }
}
