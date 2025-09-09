package gameblock.event;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.cosmetics.particles.BaseParticleCosmetic;
import gameblock.registry.GameblockCosmetics;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerRenderHandler {
    @SubscribeEvent
    public static void renderPlayer(RenderPlayerEvent.Post event) {
        if (Minecraft.getInstance().isPaused()) return;
        Player player = event.getEntity();

        GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME).orElse(null);
        if (cap != null) {
            BaseParticleCosmetic cosmetic = cap.getCosmetic();
            if (cosmetic != null) {
                cosmetic.render();
            }
        }
    }
}
