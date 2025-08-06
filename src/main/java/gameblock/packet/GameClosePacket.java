package gameblock.packet;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.registry.GameblockGames;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GameClosePacket {
    public GameClosePacket() {}

    public GameClosePacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {}

    public void readFromBuffer(FriendlyByteBuf buffer) {}

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                Player player = context.get().getSender();
                if (player != null) {
                    GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                    if (cap != null) {
                        try {
                            cap.setGame(null, player);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            context.get().setPacketHandled(true);
        });
    }
}
