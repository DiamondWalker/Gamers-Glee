package gameblock.capability;

import gameblock.game.GameInstance;
import gameblock.gui.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public class GameCapability {
    private GameInstance game = null;

    public boolean isPlaying() {
        return game != null;
    }

    public void setGame(GameInstance game, boolean clientSide) {
        this.game = game;
        if (clientSide) {
            if (game == null) {
                if (Minecraft.getInstance().screen instanceof GameScreen) Minecraft.getInstance().screen.onClose();
            } else {
                Minecraft.getInstance().setScreen(new GameScreen(game));
            }
        }
    }

    public GameInstance getGame() {
        return game;
    }
}
