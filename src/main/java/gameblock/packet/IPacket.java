package gameblock.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Supplier;

public interface IPacket {
    void writeToBuffer(FriendlyByteBuf buffer);
    void readFromBuffer(FriendlyByteBuf buffer);
    void handle(Supplier<NetworkEvent.Context> context);
}
