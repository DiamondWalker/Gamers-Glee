package gameblock.game.serpent;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.GameblockMod;
import gameblock.game.GameInstance;
import gameblock.game.serpent.packets.EatFoodPacket;
import gameblock.game.serpent.packets.SnakeUpdatePacket;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockMusic;
import gameblock.registry.GameblockPackets;
import gameblock.registry.GameblockSounds;
import gameblock.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Random;

public class SerpentGame extends GameInstance<SerpentGame> {
    public static ResourceLocation SPRITE = new ResourceLocation(GameblockMod.MODID, "textures/gui/game/serpent.png");
    private static final int INITIAL_SNAKE_LENGTH = 2;
    private static final int SNAKE_LENGTH_INCREASE = 5;
    public final TileGrid2D<Integer> tiles;

    public int headX, headY;
    public int targetSnakeLength = INITIAL_SNAKE_LENGTH;
    public int snakeLength = targetSnakeLength;
    public int foodX, foodY;

    private Direction2D snakeDirection = Direction2D.UP;
    private boolean snakeDirectionChanged = false; // so that if you press 2 direction change buttons in one tick you can't go into yourself

    public int foodEaten = 0;
    private long endTime = Integer.MIN_VALUE;

    final GameInstance.KeyBinding left = registerKey(InputConstants.KEY_LEFT, () -> setSnakeDirection(Direction2D.LEFT));
    final GameInstance.KeyBinding right = registerKey(InputConstants.KEY_RIGHT, () -> setSnakeDirection(Direction2D.RIGHT));
    final GameInstance.KeyBinding up = registerKey(InputConstants.KEY_UP, () -> setSnakeDirection(Direction2D.UP));
    final GameInstance.KeyBinding down = registerKey(InputConstants.KEY_DOWN, () -> setSnakeDirection(Direction2D.DOWN));

    public SerpentGame(Player player) {
        super(player, GameblockGames.SERPENT_GAME);
        tiles = new TileGrid2D<>(-20, 20, -20, 20, -1);
        tiles.setAll((Integer num) -> Integer.MAX_VALUE);
        if (!isClientSide()) randomFoodPosition();
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeByte(foodX);
        buffer.writeByte(foodY);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        super.readFromBuffer(buffer);
        foodX = buffer.readByte();
        foodY = buffer.readByte();
    }

    public void setSnakeDirection(Direction2D dir) {
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
            foodX = random.nextInt((tiles.maxX - tiles.minX) + 1) + tiles.minX;
            foodY = random.nextInt((tiles.maxY - tiles.minY) + 1) + tiles.minY;
        } while (isSnakeTile(foodX, foodY));
    }

    @Override
    protected void onGameLoss() {
        playSound(GameblockSounds.SNAKE_DEATH.get());
        endTime = getGameTime();
    }

    @Override
    public void tick() {
        if (!isGameOver()) {
            int nextX = headX + snakeDirection.getNormal().getX();
            int nextY = headY + snakeDirection.getNormal().getY();
            snakeDirectionChanged = false;

            if (getSnakeTicksFromTile(nextX, nextY) == -1 || isSnakeTile(nextX, nextY) && !isClientSide()) {
                setGameState(GameState.LOSS);
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
                    foodEaten++;
                    randomFoodPosition();
                    sendToAllPlayers(new EatFoodPacket(foodX, foodY, targetSnakeLength, foodEaten), null);
                }
            }
        } else if (!isClientSide() && getGameTime() - endTime > 20 * 3) {
            restart();
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        int rectMinX = tiles.minX * 3 ;
        int rectMaxX = tiles.maxX * 3 ;
        int rectMinY = tiles.minY * 3 - 9;
        int rectMaxY = tiles.maxY * 3 - 9;
        float midX = (float)(rectMinX + rectMaxX) / 2;
        float midY = (float)(rectMinY + rectMaxY) / 2;
        int sideWidth = rectMaxX - rectMinX;
        int sideHeight = rectMaxY - rectMinY;

        drawHollowRectangle(graphics, midX, midY, sideWidth + 3, sideHeight + 3, 3.0f, new ColorF(1.0f), 0);
        /*drawRectangle(graphics, midX, rectMaxY, sideWidth, 2, new ColorF(1.0f), 0); // top
        drawRectangle(graphics, midX, rectMinY, sideWidth, 2, new ColorF(1.0f), 0); // bottom
        drawRectangle(graphics, rectMinX, midY, 2, sideHeight, new ColorF(1.0f), 0); // left
        drawRectangle(graphics, rectMaxX, midY, 2, sideHeight, new ColorF(1.0f), 0); // right*/

        float y = (75.0f - rectMaxY + 1) / 2 + rectMaxY;
        drawText(graphics, 60.0f, y, 1.0f, new ColorF(1.0f), Component.literal(String.valueOf(foodEaten)));
        drawTexture(graphics, SPRITE, 50.0f, y, 8.0f, 8.0f, 0, 0, 0, 13, 13, new ColorF(1.0f));

        tiles.forEach((Vec2i coords, Integer i) -> {
            if (isSnakeTile(coords.getX(), coords.getY())) {
                drawRectangle(graphics, coords.getX() * 3, coords.getY() * 3 - 9, 3.0f, 3.0f, new ColorF(1.0f), 0);
            }
        });
        /*for (int x = tiles.minX; x <= tiles.maxX; x++) {
            for (int y = tiles.minY; y <= tiles.maxY; y++) {
                //if (isSnakeTile(x, y)) {
                    drawRectangle(graphics, x * 2, y * 2, 2.0f, 2.0f, new ColorF(1.0f, 1.0f, 1.0f, 1.0f), 0);
                //}
            }
        }*/

        drawRectangle(graphics, foodX * 3, foodY * 3 - 9, 3.0f, 3.0f, new ColorF(1.0f, 0, 0, 1.0f), 0);
        //System.out.println(foodY);
    }

    @Override
    public Music getMusic() {
        return !isGameOver() ? GameblockMusic.SNAKE : null;
    }
}
