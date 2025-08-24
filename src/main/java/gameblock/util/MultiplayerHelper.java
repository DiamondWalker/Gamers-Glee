package gameblock.util;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class MultiplayerHelper {
    public static GameInstance<?> findGameWithGameCode(MinecraftServer server, String code) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                GameInstance<?> joinGame = cap.getGame();
                String joinGameCode = joinGame.getGameCode();
                if (joinGameCode != null && code != null && joinGameCode.matches(code)) {
                    return joinGame;
                }
            }
        }

        return null;
    }
}
