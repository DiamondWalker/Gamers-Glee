package gameblock.game;

import gameblock.util.Vec2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

public class GameblockOS extends Game {
    private static final int BLOCK_WIDTH = 3;
    private final ArrayList<Block> titleBlocks = new ArrayList<>();

    public GameblockOS(Player player) {
        super(player);

        // G
        addBlock(-19, 0, 10);
        addBlock(-18, 0, 10);
        addBlock(-17, 1, 10);
        addBlock(-17, 2, 10);
        addBlock(-18, 2, 10);
        addBlock(-20, 1, 10);
        addBlock(-20, 2, 10);
        addBlock(-20, 3, 10);
        addBlock(-20, 4, 10);
        addBlock(-19, 5, 10);
        addBlock(-18, 5, 10);

        // A
        addBlock(-15, 0, 20);
        addBlock(-15, 1, 20);
        addBlock(-15, 2, 20);
        addBlock(-15, 3, 20);
        addBlock(-15, 4, 20);

        addBlock(-14, 3, 20);
        addBlock(-13, 3, 20);

        addBlock(-14, 5, 20);
        addBlock(-13, 5, 20);

        addBlock(-12, 0, 20);
        addBlock(-12, 1, 20);
        addBlock(-12, 2, 20);
        addBlock(-12, 3, 20);
        addBlock(-12, 4, 20);

        // M
        addBlock(-10, 0, 30);
        addBlock(-10, 1, 30);
        addBlock(-10, 2, 30);
        addBlock(-10, 3, 30);
        addBlock(-10, 4, 30);

        addBlock(-9, 5, 30);

        addBlock(-8, 0, 30);
        addBlock(-8, 1, 30);
        addBlock(-8, 2, 30);
        addBlock(-8, 3, 30);
        addBlock(-8, 4, 30);

        addBlock(-7, 5, 30);

        addBlock(-6, 0, 30);
        addBlock(-6, 1, 30);
        addBlock(-6, 2, 30);
        addBlock(-6, 3, 30);
        addBlock(-6, 4, 30);

        // E
        addBlock(-4, 0, 40);
        addBlock(-4, 1, 40);
        addBlock(-4, 2, 40);
        addBlock(-4, 3, 40);
        addBlock(-4, 4, 40);
        addBlock(-4, 5, 40);

        addBlock(-3, 0, 40);
        addBlock(-2, 0, 40);

        addBlock(-3, 2, 40);
        addBlock(-2, 2, 40);

        addBlock(-3, 5, 40);
        addBlock(-2, 5, 40);

        // B
        addBlock(0, 0, 50);
        addBlock(0, 1, 50);
        addBlock(0, 2, 50);
        addBlock(0, 3, 50);
        addBlock(0, 4, 50);
        addBlock(0, 5, 50);

        addBlock(1, 0, 50);
        addBlock(1, 2, 50);
        addBlock(1, 5, 50);

        addBlock(2, 0, 50);
        addBlock(2, 1, 50);
        addBlock(2, 3, 50);
        addBlock(2, 4, 50);

        // L
        addBlock(4, 0, 60);
        addBlock(4, 1, 60);
        addBlock(4, 2, 60);
        addBlock(4, 3, 60);
        addBlock(4, 4, 60);
        addBlock(4, 5, 60);

        addBlock(5, 0, 60);
        addBlock(6, 0, 60);

        // O
        addBlock(8, 1, 70);
        addBlock(8, 2, 70);
        addBlock(8, 3, 70);
        addBlock(8, 4, 70);

        addBlock(9, 0, 70);
        addBlock(9, 5, 70);

        addBlock(10, 0, 70);
        addBlock(10, 5, 70);

        addBlock(11, 1, 70);
        addBlock(11, 2, 70);
        addBlock(11, 3, 70);
        addBlock(11, 4, 70);

        // C
        addBlock(13, 1, 80);
        addBlock(13, 2, 80);
        addBlock(13, 3, 80);
        addBlock(13, 4, 80);

        addBlock(14, 0, 80);
        addBlock(14, 5, 80);

        addBlock(15, 0, 80);
        addBlock(15, 5, 80);

        // K
        addBlock(17, 0, 90);
        addBlock(17, 1, 90);
        addBlock(17, 2, 90);
        addBlock(17, 3, 90);
        addBlock(17, 4, 90);
        addBlock(17, 5, 90);

        addBlock(18, 2, 90);
        addBlock(19, 1, 90);
        addBlock(20, 0, 90);

        addBlock(18, 3, 90);
        addBlock(19, 4, 90);
        addBlock(20, 5, 90);
    }

    private void addBlock(int x, int y, int time) {
        titleBlocks.add(new Block(x, y, time));
    }

    @Override
    protected void tick() {
        long time = getGameTime();

        for (Block block : titleBlocks) {
            if (time == block.time) {
                // do thing
                break;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        drawRectangle(graphics, 0, 0, 100, 100, 0, 0, 0, 255, 0);
        for (Block block : titleBlocks) {
            if (getGameTime() >= block.time) drawRectangle(graphics, block.x * BLOCK_WIDTH, block.y * BLOCK_WIDTH, BLOCK_WIDTH, BLOCK_WIDTH, 255, 255, 255, 255, 0);
        }
        //drawText(graphics, 0, 0, "Cartridge not found.");
    }

    private class Block {
        int x, y, time;

        private Block(int x, int y, int time) {
            this.x = x;
            this.y = y;
            this.time = time;
        }
    }
}
