package gameblock.registry;

import gameblock.game.GameRestartPacket;
import gameblock.game.GameStatePacket;
import gameblock.game.blockbreak.packets.*;
import gameblock.game.defusal.packets.*;
import gameblock.game.flyingchicken.packets.PipeSpawnPacket;
import gameblock.game.flyingchicken.packets.ScorePacket;
import gameblock.game.flyingchicken.packets.WingFlapPacket;
import gameblock.game.os.packets.JoinGamePacket;
import gameblock.game.os.packets.MultiplayerPromptPacket;
import gameblock.game.os.packets.SelectGamePacket;
import gameblock.game.paddles.packets.*;
import gameblock.game.serpent.packets.EatFoodPacket;
import gameblock.game.serpent.packets.SnakeUpdatePacket;
import gameblock.packet.CosmeticSyncPacket;
import gameblock.packet.GameChangePacket;
import gameblock.packet.GameClosePacket;
import gameblock.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

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
        register(GameChangePacket.class, GameChangePacket::new);
        register(GameStatePacket.class, GameStatePacket::new);
        register(GameClosePacket.class, GameClosePacket::new);
        register(GameRestartPacket.class, GameRestartPacket::new);
        register(CosmeticSyncPacket.class, CosmeticSyncPacket::new);

        // OS
        register(SelectGamePacket.class, SelectGamePacket::new);
        register(MultiplayerPromptPacket.class, MultiplayerPromptPacket::new);
        register(JoinGamePacket.class, JoinGamePacket::new);

        // block break
        register(BallUpdatePacket.class, BallUpdatePacket::new);
        register(BallLaunchPacket.class, BallLaunchPacket::new);
        register(BrickUpdatePacket.class, BrickUpdatePacket::new);
        register(ScoreUpdatePacket.class, ScoreUpdatePacket::new);

        // serpent
        register(SnakeUpdatePacket.class, SnakeUpdatePacket::new);
        register(EatFoodPacket.class, EatFoodPacket::new);

        // flying chicken
        register(WingFlapPacket.class, WingFlapPacket::new);
        register(PipeSpawnPacket.class, PipeSpawnPacket::new);
        register(ScorePacket.class, ScorePacket::new);

        // defusal
        register(TileClickPacket.class, TileClickPacket::new);
        register(TileRevealPacket.class, TileRevealPacket::new);
        register(BombRevealPacket.class, BombRevealPacket::new);
        register(TileStatePacket.class, TileStatePacket::new);
        register(BombCountPacket.class, BombCountPacket::new);
        register(TimePacket.class, TimePacket::new);

        // paddles
        register(PaddleGameStatePacket.class, PaddleGameStatePacket::new);
        register(ClientToServerPaddleUpdatePacket.class, ClientToServerPaddleUpdatePacket::new);
        register(ServerToClientPaddleUpdatePacket.class, ServerToClientPaddleUpdatePacket::new);
        register(PaddleGameCodeSelectionPacket.class, PaddleGameCodeSelectionPacket::new);
        register(PaddleGameCodeConfirmationPacket.class, PaddleGameCodeConfirmationPacket::new);
    }

    private static <MSG extends IPacket> void register(Class<MSG> clazz, Function<FriendlyByteBuf, MSG> constructor) {
        INSTANCE.registerMessage(
                id++,
                clazz,
                MSG::writeToBuffer,
                constructor,
                MSG::handle
        );
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
