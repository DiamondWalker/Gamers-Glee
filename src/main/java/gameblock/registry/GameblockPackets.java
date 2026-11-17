package gameblock.registry;

import gameblock.game.GameRestartPacket;
import gameblock.game.GameStatePacket;
import gameblock.game.games.blockbreak.*;
import gameblock.game.games.defusal.*;
import gameblock.game.games.flyingchicken.FlyingChickenHighScorePacket;
import gameblock.game.games.flyingchicken.PipeSpawnPacket;
import gameblock.game.games.flyingchicken.ScorePacket;
import gameblock.game.games.flyingchicken.WingFlapPacket;
import gameblock.game.games.os.JoinGamePacket;
import gameblock.game.games.os.MultiplayerPromptPacket;
import gameblock.game.games.os.SelectGamePacket;
import gameblock.game.games.serpent.EatFoodPacket;
import gameblock.game.games.serpent.SnakeUpdatePacket;
import gameblock.packet.CosmeticSyncPacket;
import gameblock.packet.GameChangePacket;
import gameblock.packet.GameClosePacket;
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

        // serpent
        INSTANCE.registerMessage(id++, SnakeUpdatePacket.class, SnakeUpdatePacket::writeToBuffer, SnakeUpdatePacket::new, SnakeUpdatePacket::handle);
        INSTANCE.registerMessage(id++, EatFoodPacket.class, EatFoodPacket::writeToBuffer, EatFoodPacket::new, EatFoodPacket::handle);

        // flying chicken
        INSTANCE.registerMessage(id++, WingFlapPacket.class, WingFlapPacket::writeToBuffer, WingFlapPacket::new, WingFlapPacket::handle);
        INSTANCE.registerMessage(id++, PipeSpawnPacket.class, PipeSpawnPacket::writeToBuffer, PipeSpawnPacket::new, PipeSpawnPacket::handle);
        INSTANCE.registerMessage(id++, ScorePacket.class, ScorePacket::writeToBuffer, ScorePacket::new, ScorePacket::handle);
        INSTANCE.registerMessage(id++, FlyingChickenHighScorePacket.class, FlyingChickenHighScorePacket::writeToBuffer, FlyingChickenHighScorePacket::new, FlyingChickenHighScorePacket::handle);

        // defusal
        INSTANCE.registerMessage(id++, TileClickPacket.class, TileClickPacket::writeToBuffer, TileClickPacket::new, TileClickPacket::handle);
        INSTANCE.registerMessage(id++, TileRevealPacket.class, TileRevealPacket::writeToBuffer, TileRevealPacket::new, TileRevealPacket::handle);
        INSTANCE.registerMessage(id++, BombRevealPacket.class, BombRevealPacket::writeToBuffer, BombRevealPacket::new, BombRevealPacket::handle);
        INSTANCE.registerMessage(id++, TileStatePacket.class, TileStatePacket::writeToBuffer, TileStatePacket::new, TileStatePacket::handle);
        INSTANCE.registerMessage(id++, BombCountPacket.class, BombCountPacket::writeToBuffer, BombCountPacket::new, BombCountPacket::handle);
        INSTANCE.registerMessage(id++, TimePacket.class, TimePacket::writeToBuffer, TimePacket::new, TimePacket::handle);
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
