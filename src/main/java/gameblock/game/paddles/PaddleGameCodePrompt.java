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
        // TODO: translate
        game.drawText(0.0f, 15.0f, 1.0f, new ColorF(1.0f), Component.literal("Enter an 8-digit game code:")); // TODO: translate
        game.drawText(0.0f, 0.0f, 1.0f, new ColorF(1.0f), Component.literal(get()));
        if (isCodeTaken) {
            if (!get().isEmpty()) {
                isCodeTaken = false;
            } else {
                game.drawText(0.0f, 0.0f, 1.0f, new ColorF(1.0f, 0.0f, 0.0f), Component.literal("That game code is already used!"));
            }
        }
        game.drawText(0.0f, -15.0f, 0.7f, new ColorF(1.0f), Component.literal("(Other players will need this code to join!)")); // TODO: translate
    }

    public void setFailed() {
        isCodeTaken = true;
        clear();
    }
}
