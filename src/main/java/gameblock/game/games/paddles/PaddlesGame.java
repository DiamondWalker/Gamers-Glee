package gameblock.game.games.paddles;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class PaddlesGame extends GameInstance {
    public PaddlesGame(Player player) {
        super(player, GameblockGames.PADDLES.get());
    }

    @Override
    public int getMaxPlayers() {
        return 2;
    }

    @Override
    public String getGameCode() {
        if (players.getPlayerCount() == getMaxPlayers()) return null;
        return "TESTTEST";
    }

    @Override
    protected void tick() {

    }

    @Override
    public void render(float partialTicks) {

    }
}
