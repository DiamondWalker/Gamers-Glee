package gameblock.registry;

import gameblock.packet.CosmeticSyncPacket;
import gameblock.packet.GameChangePacket;
import gameblock.packet.GameClosePacket;
import gameblock.packet.GameUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class GameblockPackets {
    private static int id = 0;

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("gameblock", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        INSTANCE.registerMessage(id++, GameChangePacket.class, GameChangePacket::writeToBuffer, GameChangePacket::new, GameChangePacket::handle);
        INSTANCE.registerMessage(id++, GameClosePacket.class, GameClosePacket::writeToBuffer, GameClosePacket::new, GameClosePacket::handle);
        INSTANCE.registerMessage(id++, CosmeticSyncPacket.class, CosmeticSyncPacket::writeToBuffer, CosmeticSyncPacket::new, CosmeticSyncPacket::handle);
        INSTANCE.registerMessage(id++, GameUpdatePacket.class, GameUpdatePacket::writeToBuffer, GameUpdatePacket::new, GameUpdatePacket::handle);
    }

    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static <MSG> void sendToPlayerAndOthers(ServerPlayer player, MSG packet) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
    }
}
