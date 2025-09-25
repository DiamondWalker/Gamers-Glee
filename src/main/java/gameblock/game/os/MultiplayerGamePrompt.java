package gameblock.game.os;

import gameblock.game.GamePrompt;
import gameblock.game.os.packets.JoinGamePacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.rendering.ColorF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class MultiplayerGamePrompt extends GamePrompt.GameCodePrompt<GameblockOS> {
    public MultiplayerGamePrompt(GameblockOS game) {
        super(game);
    }

    @Override
    public boolean click(Vec2 clickCoordinates) {
        if (Math.abs(clickCoordinates.x) < 40 && Math.abs(clickCoordinates.y - -18) < 10) {
            if (get().length() == 8) {
                GameblockPackets.sendToServer(new JoinGamePacket(get()));
            }
            return true;
        }
        return false;
    }

    @Override
    public void render() {
        game.drawRectangle(0, 0, 150, 80, ColorF.BLACK, 0);
        game.drawHollowRectangle(0, 0, 150, 80, 2, ColorF.WHITE, 0);

        String entry = get();
        ColorF fieldColor;
        if (entry.length() == 8) {
            fieldColor = new ColorF(0.5f, 1.0f, 0.5f);
        } else if (!entry.isEmpty()) {
            fieldColor = new ColorF(1.0f, 0.5f, 0.5f);
        } else {
            fieldColor = ColorF.WHITE;
        }
        game.drawText(0, 26, 0.8f, ColorF.WHITE, Component.translatable("gui.gameblock.os.enter_game_code"));
        game.drawHollowRectangle(0, 8, 100, 12, 1, fieldColor, 0);
        game.drawText(0, 8, 0.8f, new ColorF(0.8f), Component.literal(entry));

        if (System.nanoTime() % 1e9 < 5e8) {
            Font font = Minecraft.getInstance().font;
            float width = font.width(entry.substring(0, location));
            float height = font.lineHeight;
            game.drawRectangle(-(float) font.width(entry) * 0.8f / 2 + width * 0.8f, 8, 1.0f, height * 0.8f, new ColorF(0.8f), 0);
        }


        Vec2 mouse = game.getMouseCoordinates();
        ColorF buttonColor = Math.abs(mouse.x) < 40 && Math.abs(mouse.y - -18) < 10 ? ColorF.YELLOW : ColorF.WHITE;
        game.drawHollowRectangle(0, -18, 80, 20, 1, buttonColor, 0);
        game.drawText(0, -18, 0.8f, buttonColor, Component.translatable("gui.gameblock.os.select_game_code"));
    }
}
