package gameblock.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LeaveGameblockPacket implements IPacket {
    public LeaveGameblockPacket() {}

    public LeaveGameblockPacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {}

    public void readFromBuffer(FriendlyByteBuf buffer) {}

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            context.get().enqueueWork(() -> ServerPacketHandler.handleGameblockExitPacket(this, context.get().getSender()));
        }
        context.get().setPacketHandled(true);
    }
}
