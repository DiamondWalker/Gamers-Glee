package gameblock.packet;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.gui.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EndGamePacket {

    public EndGamePacket() {}

    public EndGamePacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {}

    public void readFromBuffer(FriendlyByteBuf buffer) {}

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (cap != null) cap.setGame(null, true);
            }
        } else {
            GameCapability cap = context.get().getSender().getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) cap.setGame(null, false);
        }

        context.get().setPacketHandled(true);
    }
}
