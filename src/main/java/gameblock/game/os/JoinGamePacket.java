package gameblock.game.os;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.MultiplayerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class JoinGamePacket extends UpdateGamePacket<GameblockOS> {
    private String gameCode;

    public JoinGamePacket(String code) {
        this.gameCode = code;
    }

    public JoinGamePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(gameCode);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        gameCode = buffer.readUtf();
    }

    @Override
    public void gameUpdateReceivedOnServer(GameblockOS game, ServerPlayer sender) {
        GameInstance<?> joinGame = MultiplayerHelper.findGameWithGameCode(sender.getServer(), gameCode);
        if (joinGame != null) {
            GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) cap.attemptToJoinGame(joinGame);
        }
    }
}
