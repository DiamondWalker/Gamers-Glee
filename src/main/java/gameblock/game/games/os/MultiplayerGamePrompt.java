package gameblock.game.games.os;

import gameblock.game.GamePrompt;
import gameblock.registry.GameblockPackets;
import gameblock.util.ColorF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class MultiplayerGamePrompt extends GamePrompt<GameblockOS> {
    public MultiplayerGamePrompt(GameblockOS game) {
        super(game);
    }

    @Override
    public void handleCharTyped(char character) {
        if (get().length() < 8 && Character.isLetterOrDigit(character)) super.handleCharTyped(Character.toUpperCase(character));
    }

    @Override
    public boolean click(Vec2 clickCoordinates) {
        if (Math.abs(clickCoordinates.x) < 40 && Math.abs(clickCoordinates.y - -18) < 10) {
            if (get().length() == 8) {
                game.networkManager.sendJoinGame(get());
            }
            return true;
        }
        return false;
    }

    @Override
    public void render(float partialTicks) {
        game.renderer.drawRectangle(0, 0, 150, 80, new ColorF(0.0f), 0);
        game.renderer.drawHollowRectangle(0, 0, 150, 80, 2, new ColorF(1.0f), 0);

        String entry = get();
        ColorF fieldColor;
        if (entry.length() == 8) {
            fieldColor = new ColorF(0.5f, 1.0f, 0.5f);
        } else if (!entry.isEmpty()) {
            fieldColor = new ColorF(1.0f, 0.5f, 0.5f);
        } else {
            fieldColor = new ColorF(1.0f);
        }
        game.renderer.drawText(0, 26, 0.8f, new ColorF(1.0f), Component.literal("Enter 8-digit game code:"));
        game.renderer.drawHollowRectangle(0, 8, 100, 12, 1, fieldColor, 0);
        game.renderer.drawText(0, 8, 0.8f, new ColorF(0.8f), Component.literal(entry));

        if (System.nanoTime() % 1e9 < 5e8) {
            Font font = Minecraft.getInstance().font;
            float width = font.width(entry.substring(0, location));
            float height = font.lineHeight;
            game.renderer.drawRectangle(-(float) font.width(entry) * 0.8f / 2 + width * 0.8f, 8, 1.0f, height * 0.8f, new ColorF(0.8f), 0);
        }


        Vec2 mouse = game.controls.getMouseCoordinates();
        ColorF buttonColor = Math.abs(mouse.x) < 40 && Math.abs(mouse.y - -18) < 10 ? new ColorF(1.0f, 1.0f, 0.0f) : new ColorF(1.0f);
        game.renderer.drawHollowRectangle(0, -18, 80, 20, 1, buttonColor, 0);
        game.renderer.drawText(0, -18, 0.8f, buttonColor, Component.literal("Enter"));
    }
}
