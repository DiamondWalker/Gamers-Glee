package gameblock.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerRenderEvent {
    @SubscribeEvent
    public static void renderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        // blood helix
        /*if (player != Minecraft.getInstance().player || !Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            Vec3 playerPos = player.position();
            float time = (float) player.tickCount / 20 + event.getPartialTick();
            for (int i = 0; i < 22; i++) {
                float progress = (float) i / 22;
                float height = 3.0f * progress;
                float angle = 5.0f * progress - time;

                for (int j = 0; j < 3; j++) {
                    Vec3 particlePos = playerPos.add(Math.cos(angle), height, Math.sin(angle));
                    player.level().addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, 0.5f), particlePos.x, particlePos.y, particlePos.z, 0, 0.0f, 0);
                    angle += Mth.TWO_PI / 3;
                }
            }
        }*/

        // halo
        /*Vec3 headPos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 sideVec = new Vec3(lookVec.z, 0, -lookVec.x);
            Vec3 upVec = lookVec.cross(sideVec).normalize();
            Vec3 haloOrigin = headPos.add(upVec.scale(0.5));
            float pitch = (float)Math.atan2(Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z), lookVec.y);

            for (int i = 0; i < 10; i++) {
                Vec3 particlePos = haloOrigin.add(Vec3.directionFromRotation(pitch, player.getRandom().nextFloat() * 360).scale(0.5));

                player.level().addParticle(new DustParticleOptions(new Vector3f(1.0f, 1.0f, 0.6f), 0.8f), particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }*/
    }
}
