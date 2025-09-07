package gameblock.game.defusal;

import gameblock.GameblockMod;
import gameblock.game.GameInstance;
import gameblock.game.defusal.packets.*;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import gameblock.util.*;
import gameblock.util.rendering.ColorF;
import gameblock.util.datastructure.TileGrid2D;
import gameblock.util.physics.Direction1D;
import gameblock.util.physics.Direction2D;
import gameblock.util.physics.Vec2i;
import gameblock.util.rendering.TextUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefusalGame extends GameInstance<DefusalGame> {
    public static final ResourceLocation SPRITE = new ResourceLocation(GameblockMod.MODID, "textures/gui/game/defusal.png");

    public static final ColorF[] NUMBER_COLORS = new ColorF[] {
            new ColorF(0.0f, 0.0f, 1.0f), // 1
            new ColorF(0.0f, 1.0f, 0.0f), // 2
            new ColorF(1.0f, 0.0f, 0.0f), // 3
            new ColorF(0.0f, 0.0f, 0.5f), // 4
            new ColorF(0.5f, 0.0f, 0.0f), // 5
            new ColorF(0.0f, 0.5f, 0.5f), // 6
            new ColorF(0.5f, 0.0f, 0.5f), // 7
            new ColorF(0.0f, 0.0f, 0.0f), // 8
    };

    public final TileGrid2D<DefusalTile> tiles;
    public int bombCount;
    private boolean bombsSpawned = false;
    public int timeLeft = 20 * 60 * 4 + 19;

    public long lastRevealTime = Integer.MIN_VALUE;

    private ArrayList<SweatDrop> sweatDrops;

    public DefusalGame(Player player) {
        super(player, GameblockGames.DEFUSAL_GAME);

        tiles = new TileGrid2D<>(-12, 12, -8, 8);
        tiles.setAll((DefusalTile t) -> new DefusalTile());

        if (!isClientSide()) {
            bombCount = 30;
        } else {
            sweatDrops = new ArrayList<>();
        }
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeInt(timeLeft);
        buffer.writeShort(bombCount);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        super.readFromBuffer(buffer);
        timeLeft = buffer.readInt();
        bombCount = buffer.readShort();
    }

    private boolean setBomb(int x, int y) {
        final Vec2i[] adjacentTiles = new Vec2i[] {
                new Vec2i(1, 0),
                new Vec2i(-1, 0),
                new Vec2i(0, 1),
                new Vec2i(0, -1),

                new Vec2i(1, 1),
                new Vec2i(1, -1),
                new Vec2i(-1, 1),
                new Vec2i(-1, -1),
        };

        DefusalTile tile = tiles.get(x, y);
        if (tile.isBomb()) return false;
        tile.setBomb();
        for (Vec2i adjacentOffset : adjacentTiles) {
            DefusalTile adjacentTile = tiles.get(adjacentOffset.getX() + x, adjacentOffset.getY() + y);
            if (adjacentTile != null) adjacentTile.adjacentBombs++;
        }
        return true;
    }

    @Override
    protected void tick() {
        if (!isGameOver() && timeLeft > 0 && !isClientSide()) {
            int secondsBefore = timeLeft / 20;
            timeLeft--;
            int secondsAfter = timeLeft / 20;
            if (secondsBefore != secondsAfter) {
                sendToAllPlayers(new TimePacket(timeLeft), null);
            }
            if (timeLeft <= 0) {
                generateBombsIfTheyHaventBeenGeneratedYet(null);
                setGameState(GameState.LOSS);
            }
        }
    }

    private void checkWin() {
        AtomicBoolean isWon = new AtomicBoolean(false);
        if (!isGameOver() && bombCount == 0) {
            isWon.set(true);
            tiles.forEach((Vec2i coords, DefusalTile tile) -> {
                if (tile.getState() != (tile.isBomb() ? DefusalTile.State.FLAGGED : DefusalTile.State.REVEALED)) {
                    isWon.set(false);
                }
            });
        }

        if (isWon.get()) setGameState(GameState.WIN);
    }

    private void generateBombsIfTheyHaventBeenGeneratedYet(Vec2i safeTile) {
        if (!bombsSpawned) {
            Random rand = new Random();
            int count = 0;
            while (count < bombCount) {
                int randX = tiles.minX + rand.nextInt((tiles.maxX - tiles.minX) + 1);
                int randY = tiles.minY + rand.nextInt((tiles.maxY - tiles.minY) + 1);
                if (safeTile == null || (Math.abs(safeTile.getX() - randX) > 1 && Math.abs(safeTile.getY() - randY) > 1)) {
                    if (setBomb(randX, randY)) {
                        count++;
                    }
                }
            }
            bombsSpawned = true;
        }
    }

    public void reveal(Vec2i tile) {
        generateBombsIfTheyHaventBeenGeneratedYet(tile);

        DefusalTile defusalTile = tiles.get(tile.getX(), tile.getY());
        if (defusalTile != null && defusalTile.getState() == DefusalTile.State.HIDDEN && defusalTile.isBomb()) {
            setGameState(GameState.LOSS);
            return;
        }
        ArrayList<TileRevealPacket.TileInfo> tileInfos = new ArrayList<>();
        recursiveReveal(tile.getX(), tile.getY(), tileInfos);
        sendToAllPlayers(new BombCountPacket(bombCount), null);
        sendToAllPlayers(new TileRevealPacket(tileInfos.toArray(new TileRevealPacket.TileInfo[]{})), null);

        checkWin();
    }

    private void recursiveReveal(int x, int y, ArrayList<TileRevealPacket.TileInfo> tileInfos) {
        DefusalTile tile = tiles.get(x, y);
        if (tile != null && tile.getState() != DefusalTile.State.REVEALED) {
            if (tile.getState() == DefusalTile.State.FLAGGED) bombCount++;
            tile.reveal();
            tileInfos.add(new TileRevealPacket.TileInfo(new Vec2i(x, y), tile.adjacentBombs));
            if (tile.adjacentBombs == 0) {
                for (Direction2D dir : Direction2D.values()) {
                    Vec2i adjacent = dir.getNormal().offset(x, y);
                    recursiveReveal(adjacent.getX(), adjacent.getY(), tileInfos);
                }
            }
        }
    }

    public void cycle(Vec2i tile) {
        DefusalTile defusalTile = tiles.get(tile.getX(), tile.getY());
        if (defusalTile != null && defusalTile.getState() != DefusalTile.State.REVEALED) {
            if (defusalTile.getState() == DefusalTile.State.FLAGGED) bombCount++;
            defusalTile.cycleState();
            if (defusalTile.getState() == DefusalTile.State.FLAGGED) bombCount--;
            sendToAllPlayers(new BombCountPacket(bombCount), null);
            sendToAllPlayers(new TileStatePacket(tile, defusalTile.getState()), null);

            checkWin();
        }
    }

    private Vec2i getTile(Vec2 mouse) {
        Vec2 mouseCoords = mouse.add(new Vec2(0, 8));
        Vec2i tileCoords = new Vec2i(Math.round(mouseCoords.x / 7), Math.round(mouseCoords.y / 7));
        if (tileCoords.getX() >= tiles.minX && tileCoords.getX() <= tiles.maxX && tileCoords.getY() >= tiles.minY && tileCoords.getY() <= tiles.maxY) {
            return tileCoords;
        }
        return null;
    }

    @Override
    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {
        if (Math.abs(clickCoordinates.x) < 7.5f && Math.abs(clickCoordinates.y - 63.5f) < 7.5f) restart();

        Vec2i tileCoords = getTile(clickCoordinates);
        if (!isGameOver() && tileCoords != null) {
            if (buttonPressed != Direction1D.CENTER) {
                GameblockPackets.sendToServer(new TileClickPacket(buttonPressed, tileCoords));
            }
        }
    }

    @Override
    protected void onGameLoss() {
        ArrayList<Vec2i> bombs = new ArrayList<>();
        tiles.forEach((Vec2i coords, DefusalTile otherDefusalTile) -> {
            if (otherDefusalTile.isBomb()) bombs.add(coords);
        });
        sendToAllPlayers(new BombRevealPacket(bombs.toArray(new Vec2i[]{})), null);
        super.onGameLoss();
        playSound(SoundEvents.GENERIC_EXPLODE);
    }

    @Override
    public void render() {
        drawRectangle(0, 0, 200, 200, new ColorF(0.8f), 0);
        drawRectangle(0, -8, 168 + 8, 112 + 8, new ColorF(0.4f), 0);

        tiles.forEach((Vec2i coords, DefusalTile tile) -> {
            if (tile.getState() == DefusalTile.State.REVEALED) {
                drawRectangle(coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, new ColorF(0.6f), 0);
                //drawTexture(graphics, SPRITE, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, 0, 29, 0, 6, 6, new ColorF(1.0f));
                if (tile.adjacentBombs > 0 && !tile.isBomb()) {
                    drawText(coords.getX() * 7, coords.getY() * 7 - 8, 0.7f, NUMBER_COLORS[tile.adjacentBombs - 1], Component.literal(String.valueOf(tile.adjacentBombs)));
                }
            } else {
                //drawRectangle(graphics, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, new ColorF(0.8f), 0);
                drawTexture(SPRITE, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, 0, 32, 0, 6, 6, new ColorF(1.0f));

                if (getGameState() == GameState.LOSS && tile.isBomb()) {
                    drawTexture(SPRITE, coords.getX() * 7, coords.getY() * 7 - 8, 7, 7, 0, 0, 0, 7, 7, new ColorF(1.0f));
                    //drawRectangle(graphics, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, new ColorF(1.0f, 0.0f, 0.0f), 0);
                }

                if (tile.getState() == DefusalTile.State.FLAGGED) {
                    if (getGameState() == GameState.LOSS && !tile.isBomb()) {
                        drawTexture(SPRITE, coords.getX() * 7, coords.getY() * 7 - 8, 7, 7, 0, 0, 0, 7, 7, new ColorF(1.0f));
                        drawTexture(SPRITE, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, 0, 21, 0, 7, 7, new ColorF(1.0f));
                    } else {
                        drawTexture(SPRITE, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, 0, 7, 0, 7, 7, new ColorF(1.0f));
                    }
                } else if (tile.getState() == DefusalTile.State.QUESTION) {
                    drawTexture(SPRITE, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, 0, 14, 0, 7, 7, new ColorF(1.0f));
                    //drawText(graphics, coords.getX() * 7, coords.getY() * 7 - 8, 0.7f, new ColorF(1.0f), "?");
                }
            }
        });

        Vec2i tileCoords = getTile(getMouseCoordinates());
        if (!isGameOver() && tileCoords != null && tiles.get(tileCoords.getX(), tileCoords.getY()) != null && tiles.get(tileCoords.getX(), tileCoords.getY()).getState() != DefusalTile.State.REVEALED) {
            drawRectangle(tileCoords.getX() * 7, tileCoords.getY() * 7 - 8, 6, 6, new ColorF(1.0f).withAlpha(1.0f), 0);
        }

        boolean panikTime = !isGameOver() && timeLeft < 20 * 60;

        drawRectangle(70, 59.5f, 21, 10, new ColorF(0.0f), 0);
        drawText(70, 59.5f, 1.0f, new ColorF(1.0f, 0.0f, 0.0f), Component.literal(TextUtil.formatWithUnits(bombCount, 3)));

        drawRectangle(-70, 59.5f, 21, 10, new ColorF(0.0f), 0);
        drawText(-70, 59.5f, 1.0f, panikTime && getGameTime() % 10 < 5?
                new ColorF(1.0f, 0.0f, 0.0f) :
                new ColorF(0.0f, 0.0f, 1.0f), Component.literal(TextUtil.getTimeString(timeLeft, false, false)));

        //drawRectangle(graphics, 0, 62.0f, 15, 15, new ColorF(1.0f), 0);
        int u = 0;
        if (getGameState() == GameState.WIN) {
            u = 24;
        } else if (getGameState() == GameState.LOSS) {
            u = 8;
        } else if (getGameTime() - lastRevealTime < 5) {
            u = 16;
        }
        float x = 0;
        if (panikTime) {
            x += Mth.sin(getPartialTicks() + getGameTime());
            Random rand = new Random();
            if (rand.nextInt(80) == 0) {
                sweatDrops.add(new SweatDrop(x + rand.nextFloat(15) - 7.5f, 62.0f + rand.nextFloat(15) - 7.5f));
            }
        }
        drawTexture(SPRITE, x, 62.0f, 15, 15, 0, u, 7, 8, 8, new ColorF(1.0f));
        for (int i = 0; i < sweatDrops.size();) {
            if (sweatDrops.get(i).render()) {
                i++;
            } else {
                sweatDrops.remove(i);
            }
        }
    }


    private class SweatDrop {
        float x, y;
        long start;

        private SweatDrop(float x, float y) {
            this.x = x;
            this.y = y;
            start = getGameTime();
        }

        private boolean render() {
            float time = getPartialTicks() + (getGameTime() - start);

            float alpha = 1.0f;
            if (time < 10) {
                alpha = time / 10;
            } else if (time > 30) {
                alpha = 1.0f - (time - 30) / 10;
            }

            time /= 20;

            drawRectangle(x, y - time * time * time, 1.0f, 2.0f, new ColorF(0.0f, 0.7f, 1.0f).withAlpha(alpha), 0);
            //drawTexture(graphics, SPRITE, x, y - time * time * time, 2.5f, 3, 0, 32, 9, 5, 6, new ColorF(1.0f).withAlpha(alpha));
            return true;
            //return alpha < 0;
        }
    }
}
