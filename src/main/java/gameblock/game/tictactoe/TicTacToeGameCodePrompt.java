package gameblock.game.tictactoe;

import gameblock.game.GamePrompt;
import gameblock.game.paddles.packets.PaddleGameCodeSelectionPacket;
import gameblock.game.tictactoe.packets.TicTacToeGameCodeSelectionPacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.rendering.ColorF;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class TicTacToeGameCodePrompt extends GamePrompt.GameCodePrompt<TicTacToeGame> {
    private boolean failed = false;

    public TicTacToeGameCodePrompt(TicTacToeGame game) {
        super(game);
    }

    @Override
    public void render() {
        // TODO: localize
        if (!failed) {
            game.drawText(0.0f, 13.0f, 0.85f, ColorF.WHITE, Component.literal("Type a game code:"));
        } else {
            game.drawText(0.0f, 15.0f, 0.85f, ColorF.RED, Component.literal("That game code is already in use."), Component.literal("Type a different one:"));
        }
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
            GameblockPackets.sendToServer(new TicTacToeGameCodeSelectionPacket(game.gameCode));
            return true;
        }
        return false;
    }

    public void setFailed() {
        failed = true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
