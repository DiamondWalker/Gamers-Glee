package gameblock.capability;

import gameblock.cosmetics.particles.BaseParticleCosmetic;
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

@AutoRegisterCapability
public class GameCapability {
    // game
    private final Player player;
    private GameInstance<? extends GameInstance<?>> game = null;

    protected GameCapability(Player player) {
        this.player = player;
    }

    public boolean isPlayingGame() {
        return game != null;
    }

    public void setGame(GameInstance<?> newGame) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (game != null) game.removePlayer(serverPlayer);
            game = newGame;
            if (game != null && game.getHostPlayer() == player) game.load();
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




    // cosmetic
    private BaseParticleCosmetic cosmetic = null;

    public BaseParticleCosmetic getCosmetic() {
        return cosmetic;
    }

    public void setCosmetic(GameblockCosmetics.CosmeticType cosmetic) {
        this.cosmetic = cosmetic != null ? cosmetic.constructor.apply(player) : null;
        if (player instanceof ServerPlayer serverPlayer) GameblockPackets.sendToPlayerAndOthers(serverPlayer, new CosmeticSyncPacket(player, cosmetic));
    }

    public void forceSync() {
        if (player instanceof ServerPlayer serverPlayer) GameblockPackets.sendToPlayerAndOthers(serverPlayer, new CosmeticSyncPacket(player, cosmetic.type));
    }



    protected void writeToNBT(CompoundTag nbt) {
        if (cosmetic != null) nbt.putString("cosmetic", cosmetic.type.id);
    }

    protected void readFromNBT(CompoundTag nbt) {
        if (nbt.contains("cosmetic")) setCosmetic(GameblockCosmetics.getTypeFromID(nbt.getString("cosmetic")));
    }
}
