package gameblock.event;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.item.CartridgeItem;
import gameblock.registry.GameblockItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerTickHandler {
    @SubscribeEvent
    public static void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide()) {
            Player player = event.player;

            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                if (event.player.getItemInHand(InteractionHand.MAIN_HAND).is(GameblockItems.GAMEBLOCK.get())) {
                    if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof CartridgeItem cartridge) {
                        if (cartridge.isInstance(cap.getGame())) return;
                    }
                }

                cap.setGame(null);
                // TODO: send packet
            }
        }
    }
}
