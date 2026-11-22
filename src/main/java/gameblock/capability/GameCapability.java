package gameblock.capability;

import gameblock.cosmetics.particles.BaseParticleCosmetic;
import gameblock.game.Game;
import gameblock.game.GameInstance;
import gameblock.gui.GUIHandler;
import gameblock.packet.CosmeticSyncPacket;
import gameblock.packet.GameChangePacket;
import gameblock.registry.GameblockCosmetics;
import gameblock.registry.GameblockPackets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

@AutoRegisterCapability
public class GameCapability {
    // game
    private final Player player;
    private GameInstance<?> game = null;

    protected GameCapability(Player player) {
        this.player = player;
    }

    public boolean isPlaying() {
        return game != null;
    }

    /**
     * Initializes a new instance of the given game and switches to it
     * <p>
     * A value of null closes the current game
     * <p>
     * Should be called when a game is starting a game, closing a game, switching to another game, or receiving a {@link GameChangePacket}
     *
     * @param gameType The game to start. A value of null simply closes the current game
     */
    public void startGame(Game<?> gameType) {
        leaveCurrentGame();

        if (gameType != null) {
            GameInstance<?> gameInstance = gameType.constructor.apply(player.level().isClientSide());
            gameInstance.players.addPlayer(player);

            if (!gameInstance.isClientSide()) {
                gameInstance.players.setHost(player);
            } else {
                gameInstance.players.setLocalPlayer(player);
            }

            if (!gameInstance.isClientSide()) gameInstance.load();
            gameInstance.initialize();

            setGameInstance(gameInstance);
        } else {
            setGameInstance(null);
        }
    }

    /**
     * Sets the current game instance to a preexisting one and updates its players list to include this player
     * <p>
     * Should only be called when connecting to a multiplayer game
     *
     * @param gameInstance The game instance to join
     */
    public void joinGame(@NotNull GameInstance<?> gameInstance) {
        leaveCurrentGame();

        gameInstance.players.addPlayer(player);
        setGameInstance(gameInstance);
    }

    private void leaveCurrentGame() {
        if (game != null) {
            if (!game.isClientSide() && game.players.getHostPlayer() == player) game.save();
            game.players.removePlayer(player.getUUID());
        }
    }

    private void setGameInstance(GameInstance<?> newGame) {
        game = newGame;

        if (player instanceof ServerPlayer serverPlayer) {
            if (game != null) {
                GameblockPackets.sendToPlayer(
                        serverPlayer,
                        new GameChangePacket(
                                game.gameType,
                                game.networkManager.getPendingUpdatesFor(serverPlayer.getUUID())
                        )
                );
            } else {
                GameblockPackets.sendToPlayer(
                        serverPlayer,
                        new GameChangePacket(null, Collections.emptySet())
                );
            }
        } else {
            if (game == null) {
                GUIHandler.closeGameScreen();
            } else {
                GUIHandler.openGameScreen(game);
            }
        }
    }

    public GameInstance<?> getGame() {
        return game;
    }


    // cosmetic
    private BaseParticleCosmetic cosmetic = null;

    public BaseParticleCosmetic getCosmetic() {
        return cosmetic;
    }

    public void setCosmetic(GameblockCosmetics.CosmeticType cosmetic) {
        this.cosmetic = cosmetic != null ? cosmetic.constructor.apply(player) : null;
        if (player instanceof ServerPlayer serverPlayer) GameblockPackets.sendToPlayerAndOthers(serverPlayer, new CosmeticSyncPacket(player, cosmetic));
    }



    protected void writeToNBT(CompoundTag nbt) {
        if (cosmetic != null) nbt.putString("cosmetic", cosmetic.type.id);
    }

    protected void readFromNBT(CompoundTag nbt) {
        if (nbt.contains("cosmetic")) setCosmetic(GameblockCosmetics.getTypeFromID(nbt.getString("cosmetic")));
    }
}
