package gameblock.packet;

import gameblock.game.GameInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class UpdateGamePacket<T extends GameInstance> {
    public UpdateGamePacket() {

    }

    public UpdateGamePacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    public abstract void writeToBuffer(FriendlyByteBuf buffer);

    public abstract void readFromBuffer(FriendlyByteBuf buffer);

    public abstract void handleGameUpdate(T game);

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> ClientPacketHandler.handleUpdateGamePacket(this));
        } else {
            context.get().enqueueWork(() -> ServerPacketHandler.handleUpdateGamePacket(this, context.get().getSender()));
        }
        context.get().setPacketHandled(true);
    }
}
