package gameblock.game.serpent;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.Game;
import gameblock.registry.GameblockPackets;
import gameblock.util.Direction2D;
import gameblock.util.TileGrid2D;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Random;

public class SerpentGame extends Game {
    private static final int INITIAL_SNAKE_LENGTH = 2;
    private static final int SNAKE_LENGTH_INCREASE = 5;
    protected final TileGrid2D<Integer> tiles;

    protected int headX, headY;
    protected int snakeLength = INITIAL_SNAKE_LENGTH;
    protected int foodX = Integer.MAX_VALUE, foodY = Integer.MAX_VALUE; // these values are why outside the game area so it's like the food doesn't exist

    private Direction2D snakeDirection = Direction2D.UP;
    private boolean snakeDirectionChanged = false; // so that if you press 2 direction change buttons in one tick you can't go into yourself

    final Game.KeyBinding left = registerKey(InputConstants.KEY_LEFT, () -> setSnakeDirection(Direction2D.LEFT, true));
    final Game.KeyBinding right = registerKey(InputConstants.KEY_RIGHT, () -> setSnakeDirection(Direction2D.RIGHT, true));
    final Game.KeyBinding up = registerKey(InputConstants.KEY_UP, () -> setSnakeDirection(Direction2D.UP, true));
    final Game.KeyBinding down = registerKey(InputConstants.KEY_DOWN, () -> setSnakeDirection(Direction2D.DOWN, true));

    public SerpentGame(Player player) {
        super(player);
        tiles = new TileGrid2D<>(-50, 50, -37, 37, -1);
        tiles.setAll((Integer num) -> Integer.MAX_VALUE);
        if (!isClientSide()) randomFoodPosition();
    }

    protected void setSnakeDirection(Direction2D dir, boolean sendUpdate) { // TODO: make turns less noticeably desynced
        if (dir == snakeDirection) return;
        if (tiles.get(headX + dir.getNormal().getX(), headY + dir.getNormal().getY()) == 1) return;
        if (isClientSide()) {
            if (sendUpdate) {
                if (!snakeDirectionChanged) {
                    GameblockPackets.sendToServer(new SnakeDirectionChangePacket(dir));
                    snakeDirectionChanged = true;
                }
            } else {
                snakeDirection = dir;
            }
        } else {
            snakeDirection = dir;
            if (sendUpdate) {
                ArrayList<Integer> coords = new ArrayList<>(snakeLength * 2);
                int x = headX, y = headY;
                int i = 0;
                TILE_LOOP: while (true) {
                    coords.add(x);
                    coords.add(y);
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
                GameblockPackets.sendToPlayer((ServerPlayer) player, new SnakeUpdatePacket(snakeDirection, coords));
            }
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
        GameblockPackets.sendToPlayer((ServerPlayer) player, new EatFoodPacket(foodX, foodY, snakeLength));
    }

    @Override
    public void tick() {
        if (!isGameOver()) {
            int nextX = headX + snakeDirection.getNormal().getX();
            int nextY = headY + snakeDirection.getNormal().getY();
            snakeDirectionChanged = false;

            int value = tiles.get(nextX, nextY);
            if (value == -1 || value < snakeLength && !isClientSide()) {
                gameOver();
            } else {
                headX = nextX;
                headY = nextY;

                tiles.setAll((Integer num) -> {
                    if (num < Integer.MAX_VALUE) {
                        if (num < snakeLength) return num + 1;
                        return Integer.MAX_VALUE;
                    }
                    return num;
                });

                setSnakeTicksOfTile(headX, headY, 0);

                if (headX == foodX && headY == foodY && !isClientSide()) {
                    snakeLength += SNAKE_LENGTH_INCREASE;
                    randomFoodPosition();
                }
                //if (targetSnakeLength > snakeLength) snakeLength++;
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
