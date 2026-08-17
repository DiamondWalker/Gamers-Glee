package gameblock.capability;

import gameblock.cosmetics.particles.BaseParticleCosmetic;
import gameblock.game.GameInstance;
import gameblock.gui.GUIHandler;
import gameblock.packet.CosmeticSyncPacket;
import gameblock.packet.GameChangePacket;
import gameblock.registry.GameblockCosmetics;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.lang.reflect.InvocationTargetException;

@AutoRegisterCapability
public class GameCapability {
    // game
    private final Player player;
    private GameInstance game = null;

    protected GameCapability(Player player) {
        this.player = player;
    }

    public boolean isPlaying() {
        return game != null;
    }

    public void setGame(GameblockGames.Game<?> gameType, Player player) {
        /*
            Sometimes game constructors will send new packets. If this happens before the game change packet is sent it'll cause issues
            Therefore we must send the game change packet before the game instance is created.
             */
            if (player instanceof ServerPlayer serverPlayer) {
                GameblockPackets.sendToPlayer(serverPlayer, new GameChangePacket(gameType));
                if (game != null) game.save();
            }
            if (gameType != null) {
                try {
                    game = gameType.createInstance(player);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
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
