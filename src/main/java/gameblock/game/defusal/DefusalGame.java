package gameblock.game.defusal;

import gameblock.GameblockMod;
import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import gameblock.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefusalGame extends GameInstance<DefusalGame> {
    public static ResourceLocation SPRITE = new ResourceLocation(GameblockMod.MODID, "textures/gui/game/defusal.png");

    protected TileGrid2D<DefusalTile> tiles;
    public DefusalGame(Player player) {
        super(player, GameblockGames.DEFUSAL_GAME);

        tiles = new TileGrid2D<>(-12, 12, -8, 8);
        tiles.setAll((DefusalTile t) -> new DefusalTile());

        if (!isClientSide()) {
            int bombCount = 0;
            Random rand = new Random();
            while (bombCount < 30) {
                int randX = tiles.minX + rand.nextInt((tiles.maxX - tiles.minX) + 1);
                int randY = tiles.minY + rand.nextInt((tiles.maxY - tiles.minY) + 1);
                if (setBomb(randX, randY)) bombCount++;
            }
        }
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

    }

    protected void reveal(Vec2i tile) {
        DefusalTile defusalTile = tiles.get(tile.getX(), tile.getY());
        if (defusalTile != null && defusalTile.isBomb()) {
            ArrayList<Vec2i> bombs = new ArrayList<>();
            tiles.forEach((Vec2i coords, DefusalTile otherDefusalTile) -> {
                if (otherDefusalTile.isBomb()) bombs.add(coords);
            });
            GameblockPackets.sendToPlayer((ServerPlayer) player, new BombRevealPacket(bombs.toArray(new Vec2i[]{})));
            setGameState(GameState.LOSS);
            return;
        }
        ArrayList<TileRevealPacket.TileInfo> tileInfos = new ArrayList<>();
        recursiveReveal(tile.getX(), tile.getY(), tileInfos);
        GameblockPackets.sendToPlayer((ServerPlayer) player, new TileRevealPacket(tileInfos.toArray(new TileRevealPacket.TileInfo[]{})));

        // TODO: win condition
    }

    private void recursiveReveal(int x, int y, ArrayList<TileRevealPacket.TileInfo> tileInfos) {
        DefusalTile tile = tiles.get(x, y);
        if (tile != null && tile.getState() != DefusalTile.State.REVEALED) {
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

    private Vec2i getTile(Vec2 mouse) {
        Vec2 mouseCoords = mouse.add(new Vec2(0, 8));
        Vec2i tileCoords = new Vec2i(Math.round(mouseCoords.x / 7), Math.round(mouseCoords.y) / 7);
        if (tileCoords.getX() >= tiles.minX && tileCoords.getX() <= tiles.maxX && tileCoords.getY() >= tiles.minY && tileCoords.getY() <= tiles.maxY) {
            return tileCoords;
        }
        return null;
    }

    @Override
    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {
        Vec2i tileCoords = getTile(clickCoordinates);
        if (tileCoords != null) {
            if (buttonPressed != Direction1D.CENTER) {
                GameblockPackets.sendToServer(new TileClickPacket(buttonPressed, tileCoords));
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        drawRectangle(graphics, 0, 0, 200, 200, new ColorF(0.8f), 0);
        drawRectangle(graphics, 0, -8, 168 + 8, 112 + 8, new ColorF(0.4f), 0);
        tiles.forEach((Vec2i coords, DefusalTile tile) -> {
            if (tile.getState() == DefusalTile.State.REVEALED) {
                drawRectangle(graphics, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, new ColorF(0.6f), 0);
                if (tile.isBomb()) {
                    drawRectangle(graphics, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, new ColorF(1.0f, 0.0f, 0.0f), 0);
                } else if (tile.adjacentBombs > 0) {
                    drawText(graphics, coords.getX() * 7, coords.getY() * 7 - 8, 0.7f, new ColorF(1.0f), String.valueOf(tile.adjacentBombs));
                }
            } else {
                drawRectangle(graphics, coords.getX() * 7, coords.getY() * 7 - 8, 6, 6, new ColorF(0.8f), 0);
            }
        });

        Vec2i tileCoords = getTile(getMouseCoordinates());
        if (tileCoords != null && tiles.get(tileCoords.getX(), tileCoords.getY()) != null && tiles.get(tileCoords.getX(), tileCoords.getY()).getState() != DefusalTile.State.REVEALED) {
            drawRectangle(graphics, tileCoords.getX() * 7, tileCoords.getY() * 7 - 8, 6, 6, new ColorF(1.0f).withAlpha(1.0f), 0);
        }
    }
}
