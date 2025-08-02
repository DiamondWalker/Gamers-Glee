package gameblock.game.serpent;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.GameInstance;
import gameblock.registry.GameblockMusic;
import gameblock.registry.GameblockPackets;
import gameblock.registry.GameblockSounds;
import gameblock.util.ColorF;
import gameblock.util.Direction2D;
import gameblock.util.TileGrid2D;
import gameblock.util.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Random;

public class SerpentGame extends GameInstance {
    private static final int INITIAL_SNAKE_LENGTH = 2;
    private static final int SNAKE_LENGTH_INCREASE = 5;
    protected final TileGrid2D<Integer> tiles;

    protected int headX, headY;
    protected int targetSnakeLength = INITIAL_SNAKE_LENGTH;
    protected int snakeLength = targetSnakeLength;
    protected int foodX = Integer.MAX_VALUE, foodY = Integer.MAX_VALUE; // these values are why outside the game area so it's like the food doesn't exist

    private Direction2D snakeDirection = Direction2D.UP;
    private boolean snakeDirectionChanged = false; // so that if you press 2 direction change buttons in one tick you can't go into yourself

    final GameInstance.KeyBinding left = registerKey(InputConstants.KEY_LEFT, () -> setSnakeDirection(Direction2D.LEFT));
    final GameInstance.KeyBinding right = registerKey(InputConstants.KEY_RIGHT, () -> setSnakeDirection(Direction2D.RIGHT));
    final GameInstance.KeyBinding up = registerKey(InputConstants.KEY_UP, () -> setSnakeDirection(Direction2D.UP));
    final GameInstance.KeyBinding down = registerKey(InputConstants.KEY_DOWN, () -> setSnakeDirection(Direction2D.DOWN));

    public SerpentGame(Player player) {
        super(player);
        tiles = new TileGrid2D<>(-50, 50, -37, 37, -1);
        tiles.setAll((Integer num) -> Integer.MAX_VALUE);
        if (!isClientSide()) randomFoodPosition();
    }

    protected void setSnakeDirection(Direction2D dir) {
        if (dir == snakeDirection || dir == snakeDirection.getOpposite() || snakeDirectionChanged) return;
        snakeDirection = dir;

        if (isClientSide()) {
            ArrayList<Vec2i> coords = new ArrayList<>(snakeLength);
            int x = headX, y = headY;
            int i = 0;
            TILE_LOOP:
            while (true) {
                coords.add(new Vec2i(x, y));
                i++;

                for (Direction2D adjacent : Direction2D.values()) {
                    int newX = adjacent.getNormal().getX() + x, newY = adjacent.getNormal().getY() + y;
                    if (tiles.get(newX, newY) == i) {
                        x = newX;
                        y = newY;
                        continue TILE_LOOP;
                    }
                }

                break;
            }
            GameblockPackets.sendToServer(new SnakeUpdatePacket(snakeDirection, coords));
            snakeDirectionChanged = true;
        }
    }

    private void setSnakeTicksOfTile(int x, int y, int ticks) {
        tiles.set(x, y, ticks);
    }

    private int getSnakeTicksFromTile(int x, int y) {
        return tiles.get(x, y);
    }

    private boolean isSnakeTile(int x, int y) {
        return getSnakeTicksFromTile(x, y) < snakeLength;
    }

    private void randomFoodPosition() {
        Random random = new Random();
        do {
            foodX = random.nextInt(51) - 25;
            foodY = random.nextInt(51) - 25;
        } while (isSnakeTile(foodX, foodY));
        GameblockPackets.sendToPlayer((ServerPlayer) player, new EatFoodPacket(foodX, foodY, targetSnakeLength));
    }

    @Override
    protected void gameOver() {
        super.gameOver();
        playSound(GameblockSounds.SNAKE_DEATH.get());
    }

    @Override
    public void tick() {
        if (!isGameOver()) {
            int nextX = headX + snakeDirection.getNormal().getX();
            int nextY = headY + snakeDirection.getNormal().getY();
            snakeDirectionChanged = false;

            if (getSnakeTicksFromTile(nextX, nextY) == -1 || isSnakeTile(nextX, nextY) && !isClientSide()) {
                gameOver();
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

                if (targetSnakeLength > snakeLength) snakeLength++;

                if (headX == foodX && headY == foodY && !isClientSide()) {
                    targetSnakeLength += SNAKE_LENGTH_INCREASE;
                    randomFoodPosition();
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        for (int x = -50; x <= 50; x++) {
            for (int y = -37; y <= 37; y++) {
                if (isSnakeTile(x, y)) {
                    drawRectangle(graphics, x * 2, y * 2, 2.0f, 2.0f, new ColorF(1.0f, 1.0f, 1.0f, 1.0f), 0);
                }
            }
        }

        drawRectangle(graphics, foodX * 2, foodY * 2, 2.0f, 2.0f, new ColorF(1.0f, 0, 0, 1.0f), 0);
    }

    @Override
    public Music getMusic() {
        return !isGameOver() ? GameblockMusic.SNAKE : null;
    }
}
