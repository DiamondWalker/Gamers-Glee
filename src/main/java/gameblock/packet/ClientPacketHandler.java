package gameblock.packet;

import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
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
                    cap.setGame(packet.game, player);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void handleUpdateGamePacket(UpdateGamePacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                try {
                    cap.getGame().networkManager.receive(packet, null);
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
