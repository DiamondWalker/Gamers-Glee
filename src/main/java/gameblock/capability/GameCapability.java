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
        /*
            Sometimes game constructors will send new packets. If this happens before the game change packet is sent it'll cause issues
            Therefore we must send the game change packet before the game instance is created.
             */
            if (player instanceof ServerPlayer serverPlayer) {
                GameblockPackets.sendToPlayer(serverPlayer, new GameChangePacket(newGame));
                if (game != null) game.removePlayer(serverPlayer);
            }

            game = newGame;

            if (player.level().isClientSide()) {
                if (game == null) {
                    GUIHandler.closeGameScreen();
                } else {
                    GUIHandler.openGameScreen(game);
                }
            } else {
                if (game != null) game.load();
            }
    }

    public GameInstance<? extends GameInstance<?>> getGame() {
        return game;
    }
}
