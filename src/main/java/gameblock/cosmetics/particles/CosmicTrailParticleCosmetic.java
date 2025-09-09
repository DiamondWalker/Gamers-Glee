package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CosmicTrailParticleCosmetic extends BaseParticleCosmetic {
    public CosmicTrailParticleCosmetic(Player player) {
        super(player, GameblockCosmetics.COSMIC_TRAIL);
    }

    @Override
    public void tick() {
        if (player.getDeltaMovement().lengthSqr() > 0.01f) {
            for (int i = 0; i < 25; i++) {
                Vector3f color;
                switch (player.getRandom().nextInt(8)) {
                    case 0: {
                        color = new Vector3f(1.0f, 1.0f, 0);
                        break;
                    }
                    case 1: {
                        color = new Vector3f(1.0f, 0, 1.0f);
                        break;
                    }
                    default: {
                        color = new Vector3f(0, 0, 0);
                    }
                }
                Vec3 playerPos = player.position();
                playerPos = playerPos.add(
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth(),
                        player.getRandom().nextFloat() * player.getBbHeight(),
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth()
                );

                player.level().addParticle(new DustParticleOptions(color, 1.4f), playerPos.x, playerPos.y, playerPos.z, 0, 0, 0);
            }
        }
    }
}
