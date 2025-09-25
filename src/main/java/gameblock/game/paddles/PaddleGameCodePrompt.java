package gameblock.game.paddles;

import gameblock.game.GamePrompt;
import gameblock.game.paddles.packets.PaddleGameCodeSelectionPacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.rendering.ColorF;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class PaddleGameCodePrompt extends GamePrompt.GameCodePrompt<PaddlesGame> {
    private boolean isCodeTaken = false;

    public PaddleGameCodePrompt(PaddlesGame game) {
        super(game);
    }

    @Override
    public boolean handleKeyPress(int key) {
        if (key == GLFW.GLFW_KEY_ENTER) {
            game.gameCode = get();
            GameblockPackets.sendToServer(new PaddleGameCodeSelectionPacket(game.gameCode));
            return true;
        }
        return super.handleKeyPress(key);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render() {
        game.drawText(0.0f, 15.0f, 1.0f, ColorF.WHITE, Component.translatable("gui.gameblock.paddles.enter_game_code"));
        game.drawText(0.0f, 0.0f, 1.0f, ColorF.WHITE, Component.literal(get()));
        if (isCodeTaken) {
            if (!get().isEmpty()) {
                isCodeTaken = false;
            } else {
                game.drawText(0.0f, 0.0f, 1.0f, ColorF.RED, Component.translatable("gui.gameblock.paddles.game_code_already_used"));
            }
        }
        game.drawText(0.0f, -15.0f, 0.7f, ColorF.WHITE, Component.translatable("gui.gameblock.paddles.game_code_info"));
    }

    public void setFailed() {
        isCodeTaken = true;
        clear();
    }
}
