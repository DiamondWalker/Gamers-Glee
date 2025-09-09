package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RainCloudCosmetic extends BaseParticleCosmetic {
    public RainCloudCosmetic(Player player) {
        super(player, GameblockCosmetics.RAIN_CLOUD);
    }

    @Override
    public void tick() {
        for (int i = 0; i < 3; i++) {
            float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
            float radius = player.getRandom().nextFloat() * 1.6f * player.getBbWidth();
            Vec3 particlePos = player.position().add(Math.cos(angle) * radius, player.getBbHeight() * 1.6, Math.sin(angle) * radius);
            player.level().addParticle(ParticleTypes.CLOUD, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }
        float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
        float radius = player.getRandom().nextFloat() * 1.6f * player.getBbWidth();
        Vec3 particlePos = player.position().add(Math.cos(angle) * radius, player.getBbHeight() * 1.6, Math.sin(angle) * radius);
        player.level().addParticle(ParticleTypes.FALLING_WATER, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
    }
}
