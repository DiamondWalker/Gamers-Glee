package gameblock.packet;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
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
        context.get().enqueueWork(() -> PacketHandler.handleUpdateGamePacket(this, context));
    }
}
