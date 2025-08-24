package gameblock.game.paddles;

import gameblock.game.GamePrompt;
import gameblock.registry.GameblockPackets;
import gameblock.util.ColorF;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class PaddleGameCodePrompt extends GamePrompt.GameCodePrompt<PaddlesGame> {
    private boolean isCodeTaken = false;

    public PaddleGameCodePrompt(PaddlesGame game) {
        super(game);
    }

    @Override
    public boolean handleKeyPress(int key) {
        if (key == GLFW.GLFW_KEY_ENTER) { // TODO: make it check if this game code is already used
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
    public void render(GuiGraphics graphics, float partialTicks) {
        // TODO: translate
        game.drawText(graphics, 0.0f, 15.0f, 1.0f, new ColorF(1.0f), Component.literal("Ender an 8-digit game code:")); // TODO: translate
        game.drawText(graphics, 0.0f, 0.0f, 1.0f, new ColorF(1.0f), Component.literal(get()));
        if (isCodeTaken) {
            if (!get().isEmpty()) {
                isCodeTaken = false;
            } else {
                game.drawText(graphics, 0.0f, 0.0f, 1.0f, new ColorF(1.0f, 0.0f, 0.0f), Component.literal("That game code is already used!"));
            }
        }
        game.drawText(graphics, 0.0f, -15.0f, 0.7f, new ColorF(1.0f), Component.literal("(Other players will need this code to join!)")); // TODO: translate
    }

    public void setFailed() {
        isCodeTaken = true;
        clear();
    }
}
