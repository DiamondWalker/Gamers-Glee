package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class SaturnParticleCosmetic extends BaseParticleCosmetic {
    public SaturnParticleCosmetic(Player player) {
        super(player, GameblockCosmetics.SATURN);
    }

    @Override
    public void render() {
        for (int i = 0; i < 45; i++) {
            float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
            float radiusRand = player.getRandom().nextFloat();
            float radius = radiusRand * 4.0f * player.getBbWidth() + 2;
            Vec3 particlePos = player.position().add(Math.cos(angle) * radius, player.getBbHeight() * 0.5, Math.sin(angle) * radius);
            Vector3f color = new Vector3f(1.0f, 0.9f, 0.6f);
            float thickness = 1.0f;//0.6f + 0.6f * (Mth.sin(radius * 10) / 2 + 0.5f);
            if (radiusRand < 0.4f) {
                color = color.mul(0.2f + 0.8f * radiusRand / 0.4f);
            } else {
                thickness *= Math.min(Math.abs(radiusRand - 0.7f) / 0.1f, 1.0f);
            }
            player.level().addParticle(new DustParticleOptions(color,  thickness), particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }
    }
}
