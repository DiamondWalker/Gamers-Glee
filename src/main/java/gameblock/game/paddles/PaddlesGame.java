package gameblock.game.paddles;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PaddlesGame extends GameInstance<PaddlesGame> {
    public PaddlesGame(Player player) {
        super(player, GameblockGames.PADDLES_GAME);
    }

    @Override
    public int getMaxPlayers() {
        return 2;
    }

    @Override
    public String getGameCode() {
        if (getPlayerCount() == getMaxPlayers()) return null;
        return "TESTTEST";
    }

    @Override
    protected void tick() {

    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {

    }
}
