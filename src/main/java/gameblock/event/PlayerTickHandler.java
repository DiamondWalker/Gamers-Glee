package gameblock.event;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerTickHandler {
    @SubscribeEvent
    public static void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Player player = event.player;

            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                if (cap.isPlayingGame()) {
                    cap.getGame().baseTick(player);
                }

                if (player.level().isClientSide() && cap.getCosmetic() != null) {
                    cap.getCosmetic().tick();
                }
            }
        }
    }
}
