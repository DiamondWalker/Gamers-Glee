package gameblock.event;

import gameblock.GameblockConfig;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.cosmetics.particles.BaseParticleCosmetic;
import gameblock.game.GameInstance;
import gameblock.registry.GameblockItems;
import gameblock.util.ServerSafeMinecraftAccess;
import net.minecraft.world.InteractionHand;
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
                if (cap.isPlaying()) {
                    GameInstance game = cap.getGame();
                    game.baseTick(player);

                    if (!player.level().isClientSide()) {
                        boolean stayOpen = true;
                        if ((!player.getItemInHand(InteractionHand.MAIN_HAND).is(GameblockItems.GAMEBLOCK.get()) && !player.getItemInHand(InteractionHand.OFF_HAND).is(GameblockItems.GAMEBLOCK.get()))) {
                            stayOpen = false;
                        } else if (game.players.mainPlayer() == null || !game.players.mainPlayer().isAlive()) {
                            stayOpen = false;
                        } else {
                            GameCapability hostCapability = game.players.mainPlayer().getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                            if (hostCapability == null || hostCapability.getGame() != game) stayOpen = false;
                        }

                        if (!stayOpen) {
                            cap.setGame(null);
                        }
                    }
                }

                if (player.level().isClientSide() && cap.getCosmetic() != null) {
                    BaseParticleCosmetic cosmetic = cap.getCosmetic();
                    if (
                            !ServerSafeMinecraftAccess.isFirstPersonCharacter(player) ||
                                    cosmetic.displaysInFirstPerson() ||
                                    GameblockConfig.PARTICLE_EFFECTS_IN_FIRST_PERSON.get()
                    ) {
                        cosmetic.tick();
                    }
                }
            }
        }
    }
}
