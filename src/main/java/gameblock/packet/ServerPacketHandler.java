package gameblock.packet;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;

public class ServerPacketHandler {
    public static void handleGameClosePacket(GameClosePacket packet, ServerPlayer sender) {
        if (sender != null) {
            GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                cap.setGame(null, sender);
            }
        }
    }

    public static void handleUpdateGamePacket(UpdateGamePacket packet, ServerPlayer sender) {
        if (sender != null) {
            GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                try {
                    cap.getGame().networkManager.receive(packet, sender.getUUID());
                } catch (ClassCastException e) {
                    // do nothing
                }
            }
        }
    }
}
