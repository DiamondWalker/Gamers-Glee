package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class FireTrailParticleCosmetic extends BaseParticleCosmetic {
    public FireTrailParticleCosmetic(Player player) {
        super(player, GameblockCosmetics.FIRE_TRAIL);
    }

    @Override
    public void tick() {
        if (player.onGround() && player.getDeltaMovement().lengthSqr() > 0.01f) {
            for (int i = 0; i < 1; i++) {
                float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
                float radius = player.getRandom().nextFloat() * 0.6f;
                Vec3 particlePos = player.position().add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius);
                player.level().addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean displaysInFirstPerson() {
        return true;
    }
}
