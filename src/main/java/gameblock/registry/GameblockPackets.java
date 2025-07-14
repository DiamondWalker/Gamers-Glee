package gameblock.registry;

import gameblock.packet.EndGamePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class GameblockPackets {
    private static int id = 0;

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("gameblock", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        INSTANCE.registerMessage(id++, EndGamePacket.class, EndGamePacket::writeToBuffer, EndGamePacket::new, EndGamePacket::handle);
    }

    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
