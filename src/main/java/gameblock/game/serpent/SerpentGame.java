package gameblock.game.serpent;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.Game;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;

import java.util.Random;

public class SerpentGame extends Game {
    private static final int INITIAL_SNAKE_LENGTH = 5;
    private int[][] tiles = new int[101][101];

    private int headX, headY;
    private int snakeLength = INITIAL_SNAKE_LENGTH;
    private int foodX, foodY;

    private Direction direction = Direction.UP;

    final Game.KeyBinding left = registerKey(InputConstants.KEY_LEFT, () -> direction = Direction.WEST);
    final Game.KeyBinding right = registerKey(InputConstants.KEY_RIGHT, () -> direction = Direction.EAST);
    final Game.KeyBinding up = registerKey(InputConstants.KEY_UP, () -> direction = Direction.UP);
    final Game.KeyBinding down = registerKey(InputConstants.KEY_DOWN, () -> direction = Direction.DOWN);

    private boolean gameOver = false;

    public SerpentGame() {
        for (int x = 0; x < 101; x++) {
            for (int y = 0; y < 101; y++) {
                tiles[x][y] = Integer.MAX_VALUE;
            }
        }
        randomFoodPosition();
    }

    private void setSnakeTicksOfTile(int x, int y, int ticks) {
        x += 50;
        y += 50;
        tiles[x][y] = ticks;
    }

    private int getSnakeTicksFromTile(int x, int y) {
        x += 50;
        y += 50;
        return tiles[x][y];
    }

    private void randomFoodPosition() {
        Random random = new Random();
        do {
            foodX = random.nextInt(51) - 25;
            foodY = random.nextInt(51) - 25;
        } while (getSnakeTicksFromTile(foodX, foodY) < snakeLength);
    }

    private int getScore() {
        return snakeLength - INITIAL_SNAKE_LENGTH;
    }

    @Override
    public void tick() {
        if (!gameOver) {
            if (getSnakeTicksFromTile(headX + direction.getNormal().getX(), headY + direction.getNormal().getY()) < snakeLength) {
                gameOver = true;
            } else {
                headX += direction.getNormal().getX();
                headY += direction.getNormal().getY();

                setSnakeTicksOfTile(headX, headY, 0);

                for (int x = -50; x <= 50; x++) {
                    for (int y = -50; y <= 50; y++) {
                        if (getSnakeTicksFromTile(x, y) < Integer.MAX_VALUE) {
                            setSnakeTicksOfTile(x, y, getSnakeTicksFromTile(x, y) + 1);
                        }
                    }
                }

                if (headX == foodX && headY == foodY) {
                    snakeLength++;
                    randomFoodPosition();
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        for (int x = -50; x <= 50; x++) {
            for (int y = -50; y <= 50; y++) {
                if (getSnakeTicksFromTile(x, y) < snakeLength) {
                    drawRectangle(graphics, x * 2, y * 2, 2.0f, 2.0f, 255, 255, 255, 255, 0);
                }
            }
        }

        drawRectangle(graphics, foodX * 2, foodY * 2, 2.0f, 2.0f, 255, 0, 0, 255, 0);
    }
}
