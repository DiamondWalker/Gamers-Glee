package gameblock.event;

import gameblock.GameblockConfig;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.cosmetics.particles.BaseParticleCosmetic;
import gameblock.game.GameInstance;
import gameblock.game.GameUpdate;
import gameblock.packet.GameUpdatePacket;
import gameblock.registry.GameblockItems;
import gameblock.registry.GameblockPackets;
import gameblock.util.ServerSafeMinecraftAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.UUID;

@Mod.EventBusSubscriber
public class PlayerTickHandler {
    @SubscribeEvent
    public static void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Player player = event.player;

            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                if (cap.isPlaying()) {
                    GameInstance<?> game = cap.getGame();

                    boolean stayOpen = true;
                    if (!game.isClientSide()) {
                        if ((!player.getItemInHand(InteractionHand.MAIN_HAND).is(GameblockItems.GAMEBLOCK.get()) && !player.getItemInHand(InteractionHand.OFF_HAND).is(GameblockItems.GAMEBLOCK.get()))) {
                            stayOpen = false;
                        } else if (game.players.getHostPlayer() == null || !game.players.getHostPlayer().isAlive()) {
                            stayOpen = false; // TODO: some kind of "connection lost" message
                        } else {
                            GameCapability hostCapability = game.players.getHostPlayer().getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                            if (hostCapability == null || hostCapability.getGame() != game) stayOpen = false;
                        }

                        if (!stayOpen) {
                            cap.startGame(null);
                        }
                    }

                    if (stayOpen) {
                        if (player == game.players.getHostPlayer() || game.players.getHostPlayer() == null) {
                            game.baseTick();

                            if (!game.isClientSide()) {
                                for (UUID sendTo : game.players.getPlayers()) {
                                    Collection<GameUpdate.Outgoing> updates = game.networkManager.getPendingUpdatesFor(sendTo);
                                    if (!updates.isEmpty()) {
                                        GameblockPackets.sendToPlayer((ServerPlayer)game.players.getPlayerEntity(sendTo), new GameUpdatePacket(updates));
                                    }
                                }
                            } else {
                                Collection<GameUpdate.Outgoing> updates = game.networkManager.getPendingUpdatesFor(null);
                                if (!updates.isEmpty()) {
                                    GameblockPackets.sendToServer(new GameUpdatePacket(updates));
                                }
                            }
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
