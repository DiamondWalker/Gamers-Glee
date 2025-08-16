package gameblock.packet;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketHandler {
    public static void handleGameChangePacket(GameChangePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
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

        context.get().setPacketHandled(true);
    }

    public static void handleGameClosePacket(GameClosePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                Player player = context.get().getSender();
                if (player != null) {
                    GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                    if (cap != null) {
                        cap.setGame(null, player);
                    }
                }
            }

            context.get().setPacketHandled(true);
    }

    public static <T extends GameInstance> void handleUpdateGamePacket(UpdateGamePacket<T> packet, Supplier<NetworkEvent.Context> context) {
        Player player = context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT ? Minecraft.getInstance().player : context.get().getSender();
            if (player != null) {
                GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (cap != null && cap.isPlaying()) {
                    try {
                        packet.handleGameUpdate((T) cap.getGame());
                    } catch (ClassCastException e) {
                        // do nothing
                    }
                }
            }

            context.get().setPacketHandled(true);
    }
}
