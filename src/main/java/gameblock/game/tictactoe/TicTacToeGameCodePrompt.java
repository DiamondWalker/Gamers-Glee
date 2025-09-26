package gameblock.game.tictactoe;

import gameblock.game.GamePrompt;
import gameblock.util.rendering.ColorF;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class TicTacToeGameCodePrompt extends GamePrompt.GameCodePrompt<TicTacToeGame> {
    public TicTacToeGameCodePrompt(TicTacToeGame game) {
        super(game);
    }

    @Override
    public void render() {
        // TODO: localize
        game.drawText(0.0f, 13.0f, 0.85f, ColorF.WHITE, Component.literal("Type a game code:"));
        game.drawRectangle(0.0f, 0.0f, 45.0f, 8.0f, ColorF.WHITE, 0);
        game.drawText(0.0f, 0.0f, 0.85f, ColorF.BLACK, Component.literal(get()));
        if (conditionsForGameCodeMet()) {
            Vec2 mouse = game.getMouseCoordinates();
            if (Math.abs(mouse.x) <= 7.5f && Math.abs(mouse.y + 13.0f) <= 4.0f) {
                game.drawRectangle(0.0f, -13.0f, 15.0f, 8.0f, ColorF.WHITE, 0);
                game.drawText(0.0f, -13.0f, 0.85f, ColorF.BLACK, Component.literal("Go!"));
            } else {
                game.drawText(0.0f, -13.0f, 0.85f, ColorF.WHITE, Component.literal("Go!"));
            }
        }
    }

    @Override
    public boolean click(Vec2 clickCoordinates) {
        Vec2 mouse = game.getMouseCoordinates();
        if (conditionsForGameCodeMet() && Math.abs(mouse.x) <= 7.5f && Math.abs(mouse.y + 13.0f) <= 4.0f) {
            game.gameCode = get();
            // send packet
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
