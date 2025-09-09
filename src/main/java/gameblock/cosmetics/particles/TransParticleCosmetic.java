package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class TransParticleCosmetic extends BaseParticleCosmetic {
    public TransParticleCosmetic(Player player) {
        super(player, GameblockCosmetics.TRANS);
    }

    @Override
    public void tick() {
        if (player.getDeltaMovement().lengthSqr() > 0.01f) {
            for (int i = 0; i < 12; i++) {
                float randF = player.getRandom().nextFloat();
                float distanceFromCenter = Math.abs((randF - 0.5f) * 2);
                Vector3f[] colors = new Vector3f[]{
                        new Vector3f(1.0f, 1.0f, 1.0f),
                        new Vector3f(245.0f / 255, 169.0f / 255, 184.0f / 255),
                        new Vector3f(91.0f / 255, 206.0f / 255, 250.0f / 255)
                };
                Vector3f color = colors[(int) (distanceFromCenter * 3)];
                Vec3 playerPos = player.position();
                playerPos = playerPos.add(0, randF * player.getBbHeight() + 0.1f, 0);

                player.level().addParticle(new DustParticleOptions(color, 2.5f), playerPos.x, playerPos.y, playerPos.z, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean displaysInFirstPerson() {
        return false;
    }
}
