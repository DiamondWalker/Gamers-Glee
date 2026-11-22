package gameblock.packet;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameUpdate;
import net.minecraft.server.level.ServerPlayer;

public class ServerPacketHandler {
    public static void handleGameClosePacket(GameClosePacket packet, ServerPlayer sender) {
        if (sender != null) {
            GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                cap.startGame(null);
            }
        }
    }

    public static void handleUpdateGamePacket(GameUpdatePacket packet, ServerPlayer sender) {
        if (sender != null) {
            GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                try {
                    for (GameUpdate update : packet.updates) {
                        cap.getGame().networkManager.receive((GameUpdate.Incoming) update, sender.getUUID());
                    }
                } catch (ClassCastException e) {
                    // do nothing
                }
            }
        }
    }
}
