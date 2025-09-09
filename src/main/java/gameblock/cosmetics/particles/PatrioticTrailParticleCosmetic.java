package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class PatrioticTrailParticleCosmetic extends BaseParticleCosmetic {

    public PatrioticTrailParticleCosmetic(Player player) {
        super(player, GameblockCosmetics.PATRIOTIC_TRAIL);
    }

    @Override
    public void render() {
        if (player.getDeltaMovement().lengthSqr() > 0.01f) {
            for (int i = 0; i < 2; i++) {
                Vector3f color;
                switch (player.getRandom().nextInt(3)) {
                    case 0: {
                        color = new Vector3f(1.0f, 1.0f, 1.0f);
                        break;
                    }
                    case 1: {
                        color = new Vector3f(1.0f, 0, 0);
                        break;
                    }
                    default: {
                        color = new Vector3f(0, 0, 1.0f);
                    }
                }
                Vec3 playerPos = player.position();
                playerPos = playerPos.add(
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth() * 2,
                        player.getRandom().nextFloat() * player.getBbHeight(),
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth() * 2
                );

                player.level().addParticle(new DustParticleOptions(color, 1.0f), playerPos.x, playerPos.y, playerPos.z, 0, 0, 0);
            }
        }
    }
}
