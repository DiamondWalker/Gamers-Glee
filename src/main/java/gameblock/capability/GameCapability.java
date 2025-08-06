package gameblock.capability;

import gameblock.game.GameInstance;
import gameblock.gui.GameScreen;
import gameblock.packet.GameChangePacket;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.lang.reflect.InvocationTargetException;

@AutoRegisterCapability
public class GameCapability {
    private GameInstance game = null;

    public boolean isPlaying() {
        return game != null;
    }

    public void setGame(GameblockGames.Game<?> gameType, Player player) {
        try {
            /*
            Sometimes game constructors will send new packets. If this happens before the game change packet is sent it'll cause issues
            Therefore we must send the game change packet before the game instance is created.
             */
            if (player instanceof ServerPlayer serverPlayer) {
                GameblockPackets.sendToPlayer(serverPlayer, new GameChangePacket(gameType));
            }
            this.game = gameType != null ? gameType.createInstance(player) : null;

            if (player.level().isClientSide()) {
                if (game == null) {
                    if (Minecraft.getInstance().screen instanceof GameScreen) Minecraft.getInstance().screen.onClose();
                } else {
                    Minecraft.getInstance().setScreen(new GameScreen(game));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GameInstance getGame() {
        return game;
    }
}
