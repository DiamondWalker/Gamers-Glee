package gameblock.packet;

import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPacketHandler {
    public static void handleGameChangePacket(GameChangePacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                cap.setGame(packet.game);
            }
        }
    }

    public static <T extends GameInstance> void handleUpdateGamePacket(UpdateGamePacket<T> packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                try {
                    packet.gameUpdateReceivedOnClient((T) cap.getGame());
                } catch (ClassCastException e) {
                    throw new IllegalStateException("Could not handle UpdateGamePacket as game was not found.", e);
                }
            }
        }
    }

    public static void handleCosmeticSyncPacket(CosmeticSyncPacket packet) {

    }
}
