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
    public void tick() {
        for (int i = 0; i < 150; i++) {
            float maxDist = 4.5f * player.getBbWidth() + 2;
            float minDist = 2;
            float x = player.getRandom().nextFloat() * maxDist * 2 - maxDist;
            float y = player.getRandom().nextFloat() * maxDist * 2 - maxDist;
            float dist = Mth.sqrt(x * x + y * y);

            if (dist > minDist && dist < maxDist) {
                float theThingForColor = (dist - minDist) / (maxDist - minDist);
                Vec3 particlePos = player.position().add(x, player.getBbHeight() * 0.5, y);
                Vector3f color = new Vector3f(1.0f, 0.9f, 0.6f);
                float thickness = 1.5f;//0.6f + 0.6f * (Mth.sin(radius * 10) / 2 + 0.5f);
                if (theThingForColor < 0.4f) {
                    color = color.mul(0.2f + 0.8f * theThingForColor / 0.4f);
                } else {
                    thickness *= Math.min(Math.abs(theThingForColor - 0.68f) / 0.12f, 1.0f);
                }
                player.level().addParticle(new DustParticleOptions(color,  thickness), particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean displaysInFirstPerson() {
        return false;
    }
}
