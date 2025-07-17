package gameblock.game.serpent;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.Game;
import gameblock.util.Direction2D;
import gameblock.util.TileGrid2D;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class SerpentGame extends Game {
    private static final int INITIAL_SNAKE_LENGTH = 2;
    private static final int SNAKE_LENGTH_INCREASE = 5;
    private final TileGrid2D<Integer> tiles;

    private int headX, headY;
    private int snakeLength = INITIAL_SNAKE_LENGTH;
    private int targetSnakeLength = INITIAL_SNAKE_LENGTH;
    private int foodX, foodY;

    private Direction2D snakeDirection = Direction2D.UP;
    private boolean snakeDirectionChanged = false; // so that if you press 2 direction change buttons in one tick you can't go into yourself

    final Game.KeyBinding left = registerKey(InputConstants.KEY_LEFT, () -> setSnakeDirection(Direction2D.LEFT));
    final Game.KeyBinding right = registerKey(InputConstants.KEY_RIGHT, () -> setSnakeDirection(Direction2D.RIGHT));
    final Game.KeyBinding up = registerKey(InputConstants.KEY_UP, () -> setSnakeDirection(Direction2D.UP));
    final Game.KeyBinding down = registerKey(InputConstants.KEY_DOWN, () -> setSnakeDirection(Direction2D.DOWN));

    private boolean gameOver = false;

    public SerpentGame(Player player) {
        super(player);
        tiles = new TileGrid2D<>(-50, 50, -37, 37, -1);
        tiles.setAll((Integer num) -> Integer.MAX_VALUE);
        randomFoodPosition();
    }

    private void setSnakeDirection(Direction2D dir) {
        if (dir != snakeDirection && dir != snakeDirection.getOpposite() && !snakeDirectionChanged) {
            snakeDirection = dir;
            snakeDirectionChanged = true;
        }
    }

    private void setSnakeTicksOfTile(int x, int y, int ticks) {
        tiles.set(x, y, ticks);
    }

    private int getSnakeTicksFromTile(int x, int y) {
        return tiles.get(x, y);
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
            int nextX = headX + snakeDirection.getNormal().getX();
            int nextY = headY + snakeDirection.getNormal().getY();
            snakeDirectionChanged = false;

            int value = tiles.get(nextX, nextY);
            if (value == -1 || value < snakeLength) {
                gameOver = true;
            } else {
                headX = nextX;
                headY = nextY;

                tiles.setAll((Integer num) -> {
                    if (num < Integer.MAX_VALUE) {
                        return num + 1;
                    }
                    return num;
                });

                setSnakeTicksOfTile(headX, headY, 0);

                if (headX == foodX && headY == foodY) {
                    targetSnakeLength += SNAKE_LENGTH_INCREASE;
                    randomFoodPosition();
                }
                if (targetSnakeLength > snakeLength) snakeLength++;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        for (int x = -50; x <= 50; x++) {
            for (int y = -37; y <= 37; y++) {
                if (getSnakeTicksFromTile(x, y) < snakeLength) {
                    drawRectangle(graphics, x * 2, y * 2, 2.0f, 2.0f, 255, 255, 255, 255, 0);
                }
            }
        }

        drawRectangle(graphics, foodX * 2, foodY * 2, 2.0f, 2.0f, 255, 0, 0, 255, 0);
    }
}
