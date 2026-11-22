package gameblock.packet;

import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

public class ClientPacketHandler {
    public static void handleGameChangePacket(GameChangePacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                try {
                    cap.startGame(packet.game);

                    if (packet.game != null) {
                        for (GameUpdate update : packet.updates) {
                            cap.getGame().networkManager.receive((GameUpdate.Incoming) update, null);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void handleUpdateGamePacket(GameUpdatePacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                try {
                    for (GameUpdate update : packet.updates) {
                        cap.getGame().networkManager.receive((GameUpdate.Incoming) update, null);
                    }
                    //packet.handleGameUpdate((T) cap.getGame());
                } catch (ClassCastException e) {
                    GameblockMod.LOGGER.warn("Could not handle UpdateGamePacket as game was not found.");
                }
            }
        }
    }

    public static void handleCosmeticSyncPacket(CosmeticSyncPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level.getEntity(packet.player) instanceof Player player) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME).orElse(null);
            if (cap != null) cap.setCosmetic(packet.cosmetic);
        }
    }
}
