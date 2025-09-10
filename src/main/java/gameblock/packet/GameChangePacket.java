package gameblock.packet;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.util.ServerSafeMinecraftAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameType;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GameChangePacket implements IPacket {
    public GameInstance<? extends GameInstance<?>> game;

    public GameChangePacket(GameInstance<? extends GameInstance<?>> game) {
        this.game = game;
    }

    public GameChangePacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        if (game != null) {
            buffer.writeUtf(game.gameType.gameID);
            game.writeToBuffer(buffer);
        } else {
            buffer.writeUtf("");
        }
    }

    public void readFromBuffer(FriendlyByteBuf buffer) {
        GameblockGames.Game<? extends GameInstance<?>> gameType = GameblockGames.getGame(buffer.readUtf());
        if (gameType != null) {
            ServerSafeMinecraftAccess.accessPlayerObject((player) -> {
                game = gameType.createInstance(player);
                game.readFromBuffer(buffer);
            });
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> ClientPacketHandler.handleGameChangePacket(this));
        }
        context.get().setPacketHandled(true);
    }
}
