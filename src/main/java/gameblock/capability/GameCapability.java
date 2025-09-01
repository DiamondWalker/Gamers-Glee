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
    private GameInstance game = null;

    public boolean isPlaying() {
        return game != null;
    }

    public void setGameInstance(@Nonnull GameInstance<?> instance, ServerPlayer player) {
        GameblockPackets.sendToPlayer(player, new GameChangePacket(instance.gameType));
        if (game != null) game.removePlayer(player);
        this.game = instance;
    }

    public void setGame(GameblockGames.Game<?> gameType, Player player) {
        /*
            Sometimes game constructors will send new packets. If this happens before the game change packet is sent it'll cause issues
            Therefore we must send the game change packet before the game instance is created.
             */
            if (player instanceof ServerPlayer serverPlayer) {
                GameblockPackets.sendToPlayer(serverPlayer, new GameChangePacket(gameType));
                if (game != null) game.removePlayer(serverPlayer);
            }
            if (gameType != null) {
                game = gameType.createInstance(player);
            } else {
                this.game = null;
            }

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

    public GameInstance getGame() {
        return game;
    }
}
