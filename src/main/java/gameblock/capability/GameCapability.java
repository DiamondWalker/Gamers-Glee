package gameblock.capability;

import gameblock.game.GameInstance;
import gameblock.gui.GUIHandler;
import gameblock.packet.GameChangePacket;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

@AutoRegisterCapability
public class GameCapability {
    private final Player player;
    private GameInstance<? extends GameInstance<?>> game = null;

    protected GameCapability(Player player) {
        this.player = player;
    }

    public boolean isPlaying() {
        return game != null;
    }

    public void setGame(GameInstance<?> newGame) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (game != null) game.removePlayer(serverPlayer);
            game = newGame;
            if (game.getHostPlayer() == player) game.load();
            GameblockPackets.sendToPlayer(serverPlayer, new GameChangePacket(newGame));
        } else {
            game = newGame;
            if (game == null) {
                GUIHandler.closeGameScreen();
            } else {
                GUIHandler.openGameScreen(game);
            }
        }
    }

    public void attemptToJoinGame(GameInstance<?> newGame) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (!newGame.isPlaying(serverPlayer)) {
                setGame(newGame);
                newGame.addPlayer(serverPlayer);
            }
            throw new IllegalStateException(player.getName().getString() + " attempted to join game they're already in is already in!");
        }
        throw new IllegalStateException("Attempted to join multiplayer game from the client!");
    }

    public GameInstance<? extends GameInstance<?>> getGame() {
        return game;
    }
}
