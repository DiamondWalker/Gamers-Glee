package gameblock.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.client.particle.DustParticleBase;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber
public class PlayerRenderEvent {
    @SubscribeEvent
    public static void renderPlayer(RenderPlayerEvent.Post event) {
        if (Minecraft.getInstance().isPaused()) return;
        Player player = event.getEntity();
        // rainbow trail
        /*if (player.getDeltaMovement().lengthSqr() > 0.01f) {
            float time = (event.getPartialTick() + player.tickCount) / 100;
            int colorCode = Mth.hsvToRgb(time % 1.0f, 1.0f, 1.0f);
            Vector3f color = new Vector3f(FastColor.ARGB32.red(colorCode), FastColor.ARGB32.green(colorCode), FastColor.ARGB32.blue(colorCode));
            color = color.mul(1.0f / 255);
            for (int i = 0; i < 4; i++) {
                Vec3 playerPos = player.getPosition(event.getPartialTick());
                playerPos = playerPos.add(
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth(),
                        player.getRandom().nextFloat() * player.getBbHeight(),
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth()
                );

                player.level().addParticle(new DustParticleOptions(color, 2.0f), playerPos.x, playerPos.y, playerPos.z, 0, 0, 0);
            }
        }*/

        // trans trail
        /*if (player.getDeltaMovement().lengthSqr() > 0.01f) {
            for (int i = 0; i < 4; i++) {
                float randF = player.getRandom().nextFloat();
                float distanceFromCenter = Math.abs((randF - 0.5f) * 2);
                Vector3f[] colors = new Vector3f[]{
                        new Vector3f(1.0f, 1.0f, 1.0f),
                        new Vector3f(245.0f / 255, 169.0f / 255, 184.0f / 255),
                        new Vector3f(91.0f / 255, 206.0f / 255, 250.0f / 255)
                };
                Vector3f color = colors[(int) (distanceFromCenter * 3)];
                Vec3 playerPos = player.getPosition(event.getPartialTick());
                playerPos = playerPos.add(0, randF * player.getBbHeight() + 0.1f, 0);

                player.level().addParticle(new DustParticleOptions(color, 2.5f), playerPos.x, playerPos.y, playerPos.z, 0, 0, 0);
            }
        }*/

        // fire trail
        /*if (player.onGround() && player.getDeltaMovement().lengthSqr() > 0.01f) {
            for (int i = 0; i < 1; i++) {
                float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
                float radius = player.getRandom().nextFloat() * 0.6f;
                Vec3 particlePos = player.position().add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius);
                player.level().addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }
        }*

        // saturn rings
        /*for (int i = 0; i < 45; i++) {
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
        }*/

        // rain cloud
        /*for (int i = 0; i < 3; i++) {
            float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
            float radius = player.getRandom().nextFloat() * 1.6f * player.getBbWidth();
            Vec3 particlePos = player.position().add(Math.cos(angle) * radius, player.getBbHeight() * 1.6, Math.sin(angle) * radius);
            player.level().addParticle(ParticleTypes.CLOUD, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }
        float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
        float radius = player.getRandom().nextFloat() * 1.6f * player.getBbWidth();
        Vec3 particlePos = player.position().add(Math.cos(angle) * radius, player.getBbHeight() * 1.6, Math.sin(angle) * radius);
        player.level().addParticle(ParticleTypes.FALLING_WATER, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);*/


        // pentagram
        /*if (player.onGround() && player.getDeltaMovement().lengthSqr() < 0.01f) {
            // small smoke
            for (int i = 0; i < 100; i++) {
                float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
                float radius = player.getRandom().nextFloat() * 5;
                Vec3 particlePos = player.position().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                player.level().addParticle(ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }

            // large smoke
            for (int i = 0; i < 2; i++) {
                float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
                float radius = player.getRandom().nextFloat() * 5;
                Vec3 particlePos = player.position().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                player.level().addParticle(ParticleTypes.LARGE_SMOKE, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }

            // fire ring
            for (int i = 0; i < 3; i++) {
                float angle = player.getRandom().nextFloat() * Mth.TWO_PI;
                Vec3 particlePos = player.position().add(Math.cos(angle) * 4, 0, Math.sin(angle) * 4);
                player.level().addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y + 0.2, particlePos.z, 0, 0, 0);
            }

            // fire star
            for (int i = 0; i < 5; i++) {
                float angle1 = (Mth.TWO_PI / 5) * i;
                float angle2 = (Mth.TWO_PI / 5) * (i + 2);
                Vec3 particlePos1 = player.position().add(Math.cos(angle1) * 4, 0, Math.sin(angle1) * 4);
                Vec3 particlePos2 = player.position().add(Math.cos(angle2) * 4, 0, Math.sin(angle2) * 4);
                Vec3 trueParticlePos = particlePos1.lerp(particlePos2, player.getRandom().nextFloat());
                player.level().addParticle(ParticleTypes.FLAME, trueParticlePos.x, trueParticlePos.y + 0.2, trueParticlePos.z, 0, 0, 0);
            }
        }*/

        // patriotic trail
        /*if (player.getDeltaMovement().lengthSqr() > 0.01f) {
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
                Vec3 playerPos = player.getPosition(event.getPartialTick());
                playerPos = playerPos.add(
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth() * 2,
                        player.getRandom().nextFloat() * player.getBbHeight(),
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth() * 2
                );

                player.level().addParticle(new DustParticleOptions(color, 1.0f), playerPos.x, playerPos.y, playerPos.z, 0, 0, 0);
            }
        }*/

        // cosmic trail
        /*if (player.getDeltaMovement().lengthSqr() > 0.01f) {
            for (int i = 0; i < 2; i++) {
                Vector3f color;
                switch (player.getRandom().nextInt(5)) {
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
                Vec3 playerPos = player.getPosition(event.getPartialTick());
                playerPos = playerPos.add(
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth() * 2,
                        player.getRandom().nextFloat() * player.getBbHeight(),
                        (player.getRandom().nextFloat() - 0.5f) * player.getBbWidth() * 2
                );

                player.level().addParticle(new DustParticleOptions(color, 1.0f), playerPos.x, playerPos.y, playerPos.z, 0, 0, 0);
            }
        }*/

        // blood helix
        //if (player != Minecraft.getInstance().player || !Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            /*Vec3 playerPos = player.position();
            float time = (float) player.tickCount / 20 + event.getPartialTick();
            for (int i = -5; i < 22; i++) {
                float progress = (float) i / 22;
                float height = 3.0f * progress;
                float angle = 5.0f * progress - time;

                for (int j = 0; j < 2; j++) {
                    Vec3 particlePos = playerPos.add(Math.cos(angle), height, Math.sin(angle));
                    player.level().addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, 0.5f), particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
                    angle += Mth.TWO_PI / 2;
                }
            }*/
        //}
    }
}
