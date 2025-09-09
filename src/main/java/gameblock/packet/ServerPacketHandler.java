package gameblock.packet;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import net.minecraft.server.level.ServerPlayer;

public class ServerPacketHandler {
    public static void handleGameClosePacket(GameClosePacket packet, ServerPlayer sender) {
        if (sender != null) {
            GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                cap.setGame(null);
            }
        }
    }

    public static <T extends GameInstance> void handleUpdateGamePacket(UpdateGamePacket<T> packet, ServerPlayer sender) {
        if (sender != null) {
            GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlayingGame()) {
                try {
                    packet.gameUpdateReceivedOnServer((T) cap.getGame(), sender);
                } catch (ClassCastException e) {
                    // do nothing
                }
            }
        }
    }
}
