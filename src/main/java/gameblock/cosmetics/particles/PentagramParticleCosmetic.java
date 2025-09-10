package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PentagramParticleCosmetic extends BaseParticleCosmetic {
    public PentagramParticleCosmetic(Player player) {
        super(player, GameblockCosmetics.PENTAGRAM);
    }

    @Override
    public void tick() {
        if (player.onGround() && player.getDeltaMovement().lengthSqr() < 0.01f) {
            // small smoke
            for (int i = 0; i < 600; i++) {
                float x = player.getRandom().nextFloat() * 10 - 5;
                float y = player.getRandom().nextFloat() * 10 - 5;
                if (Mth.sqrt(x * x + y * y) < 5) {
                    Vec3 particlePos = player.position().add(x, 0, y);
                    player.level().addParticle(ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
                }
            }

            // large smoke
            for (int i = 0; i < 2; i++) {
                float x = player.getRandom().nextFloat() * 10 - 5;
                float y = player.getRandom().nextFloat() * 10 - 5;
                if (Mth.sqrt(x * x + y * y) < 5) {
                    Vec3 particlePos = player.position().add(x, 0, y);
                    player.level().addParticle(ParticleTypes.LARGE_SMOKE, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
                }
            }

            // fire ring
            for (int i = 0; i < 9; i++) {
                float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
                Vec3 particlePos = player.position().add(Math.cos(angle) * 4, 0, Math.sin(angle) * 4);
                player.level().addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y + 0.2, particlePos.z, 0, 0, 0);
            }

            // fire star
            for (int i = 0; i < 15; i++) {
                float angle1 = (Mth.TWO_PI / 5) * i;
                float angle2 = (Mth.TWO_PI / 5) * (i + 2);
                Vec3 particlePos1 = player.position().add(Math.cos(angle1) * 4, 0, Math.sin(angle1) * 4);
                Vec3 particlePos2 = player.position().add(Math.cos(angle2) * 4, 0, Math.sin(angle2) * 4);
                Vec3 trueParticlePos = particlePos1.lerp(particlePos2, player.getRandom().nextFloat());
                player.level().addParticle(ParticleTypes.FLAME, trueParticlePos.x, trueParticlePos.y + 0.2, trueParticlePos.z, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean displaysInFirstPerson() {
        return false;
    }
}
