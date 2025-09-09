package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class BloodHelixParticleCosmetic extends BaseParticleCosmetic {
    public BloodHelixParticleCosmetic(Player player) {
        super(player, GameblockCosmetics.BLOOD_HELIX);
    }

    @Override
    public void render() {
        Vec3 playerPos = player.position();
        float time = (float) player.tickCount / 20;
        for (int i = -5; i < 22; i++) {
            float progress = (float) i / 22;
            float height = 3.0f * progress;
            float angle = 5.0f * progress - time;

            for (int j = 0; j < 2; j++) {
                Vec3 particlePos = playerPos.add(Math.cos(angle), height, Math.sin(angle));
                player.level().addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, 0.5f), particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
                angle += Mth.TWO_PI / 2;
            }
        }
    }
}
