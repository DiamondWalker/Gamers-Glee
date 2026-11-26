package gameblock.game.games.paddles;

import gameblock.game.GamePrompt;
import gameblock.util.ColorF;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class PaddlesGameCodePrompt extends GamePrompt.GameCodePrompt<PaddlesGame> {
    private boolean isCodeTaken = false;

    public PaddlesGameCodePrompt(PaddlesGame game) {
        super(game);
    }

    @Override
    public boolean handleKeyPress(int key) {
        if (key == GLFW.GLFW_KEY_ENTER && conditionsForGameCodeMet()) {
            game.gameCode = get();
            game.networkManager.sendGameCodeSelection(game.getGameCode());
            return true;
        }
        return super.handleKeyPress(key);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(float partialTicks) {
        game.renderer.drawText(0.0f, 15.0f, 1.0f, ColorF.WHITE, Component.translatable("gui.gameblock.paddles.enter_game_code"));
        game.renderer.drawText(0.0f, 0.0f, 1.0f, ColorF.WHITE, Component.literal(get()));
        if (isCodeTaken) {
            if (!get().isEmpty()) {
                isCodeTaken = false;
            } else {
                game.renderer.drawText(0.0f, 0.0f, 1.0f, ColorF.RED, Component.translatable("gui.gameblock.paddles.game_code_already_used"));
            }
        }
        game.renderer.drawText(0.0f, -15.0f, 0.7f, ColorF.WHITE, Component.translatable("gui.gameblock.paddles.game_code_info"));
    }

    public void setFailed() {
        isCodeTaken = true;
        clear();
    }
}
