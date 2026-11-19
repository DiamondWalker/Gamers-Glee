package gameblock.capability;

import gameblock.cosmetics.particles.BaseParticleCosmetic;
import gameblock.game.Game;
import gameblock.game.GameInstance;
import gameblock.gui.GUIHandler;
import gameblock.packet.CosmeticSyncPacket;
import gameblock.packet.GameChangePacket;
import gameblock.packet.GameUpdatePacket;
import gameblock.registry.GameblockCosmetics;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.function.Supplier;

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

    public void setGame(GameInstance<?> newGame) {
        if (!player.level().isClientSide()) {
            if (game != null) game.save();
            if (newGame != null) newGame.load();
        } else {
            if (newGame == null) {
                GUIHandler.closeGameScreen();
            } else {
                GUIHandler.openGameScreen(newGame);
            }
        }

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
        }
    }

    public GameInstance getGame() {
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
