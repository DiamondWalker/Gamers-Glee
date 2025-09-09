package gameblock.event;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.cosmetics.particles.BaseParticleCosmetic;
import gameblock.packet.CosmeticSyncPacket;
import gameblock.registry.GameblockPackets;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CosmeticSyncHandler {
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer newTracker && event.getTarget() instanceof Player player) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME).orElse(null);
            if (cap != null) {
                BaseParticleCosmetic cosmetic = cap.getCosmetic();
                if (cosmetic != null) {
                    GameblockPackets.sendToPlayer(newTracker, new CosmeticSyncPacket(player, cosmetic.type));
                }
            }
        }
    }
}
