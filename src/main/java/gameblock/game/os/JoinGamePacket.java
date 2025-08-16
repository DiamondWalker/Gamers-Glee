package gameblock.game.os;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import gameblock.packet.UpdateGamePacket;
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
    public void handleGameUpdate(GameblockOS game) {
        if (game.getHostPlayer() instanceof ServerPlayer serverPlayer) { // TODO: eventually this handle method will be updated to receive a player instance as an argument. When it is use that instead of getHostPlayer()
            for (ServerPlayer player : serverPlayer.getServer().getPlayerList().getPlayers()) {
                GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (cap != null && cap.isPlaying()) {
                    GameInstance<?> joinGame = cap.getGame();
                    if (joinGame.getGameCode().matches(gameCode)) {
                        if (joinGame.addPlayer(serverPlayer)) return;
                    }
                }
            }
        } else {
            throw new IllegalStateException("Host player should be an instance of ServerPlayer, but it isn't! Was the packet handled on the client?");
        }
    }
}
