package gameblock.packet;

import gameblock.event.CosmeticSyncHandler;
import gameblock.registry.GameblockCosmetics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CosmeticSyncPacket implements IPacket {
    private int player;
    private GameblockCosmetics.CosmeticType cosmetic;

    public CosmeticSyncPacket(Player player, GameblockCosmetics.CosmeticType cosmetic) {
        this.player = player.getId();
        this.cosmetic = cosmetic;
    }

    public CosmeticSyncPacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(player);
        buffer.writeUtf(cosmetic.id);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        player = buffer.readInt();
        cosmetic = GameblockCosmetics.getTypeFromID(buffer.readUtf());
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> ClientPacketHandler.handleCosmeticSyncPacket(this));
        }
        context.get().setPacketHandled(true);
    }
}
