package gameblock.packet;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerPacketHandler {
    public static void handleGameClosePacket(GameClosePacket packet, ServerPlayer sender) {
        if (sender != null) {
            GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                cap.setGame(null, sender);
            }
        }
    }

    public static <T extends GameInstance> void handleUpdateGamePacket(UpdateGamePacket<T> packet, ServerPlayer sender) {
        if (sender != null) {
            GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                try {
                    packet.gameUpdateReceivedOnServer((T) cap.getGame(), sender);
                } catch (ClassCastException e) {
                    // do nothing
                }
            }
        }
    }
}
