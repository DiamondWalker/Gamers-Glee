package gameblock.gui;

import gameblock.game.GameInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class GUIHandler {
    public static void closeGameScreen() {
        if (Minecraft.getInstance().screen instanceof GameScreen) Minecraft.getInstance().screen.onClose();
    }

    public static void openGameScreen(GameInstance<?> game) {
        Minecraft.getInstance().setScreen(new GameScreen(game));
    }
}
