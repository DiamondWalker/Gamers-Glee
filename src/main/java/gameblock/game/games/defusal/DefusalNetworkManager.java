package gameblock.game.games.defusal;

import gameblock.game.GameUpdate;
import gameblock.game.GameUpdateType;
import gameblock.game.NetworkManager;
import gameblock.registry.GameblockSounds;
import gameblock.util.Direction1D;
import gameblock.util.Vec2i;

import java.util.UUID;

public class DefusalNetworkManager extends NetworkManager<DefusalGame> {
    private final GameUpdateType<DefusalGame> bombCountUpdate = registerMessage(this::receiveBombCountUpdate, false, true);
    private final GameUpdateType<DefusalGame> bombReveal = registerMessage(this::receiveBombReveal, false, true);
    private final GameUpdateType<DefusalGame> tileClick = registerMessage(this::receiveTileClick, true, false);
    private final GameUpdateType<DefusalGame> tileReveal = registerMessage(this::receiveTileReveal, false, true);
    private final GameUpdateType<DefusalGame> tileState = registerMessage(this::receiveTileState, false, true);
    private final GameUpdateType<DefusalGame> time = registerMessage(this::receiveTime, false, true);

    public DefusalNetworkManager(DefusalGame game) {
        super(game);
    }

    public void sendBombCountUpdate(int bombCount, UUID sender) {
        bombCountUpdate.sendToClient(sender).add(bombCount);
    }

    public void sendBombCountUpdateToAll(int bombCount) {
        bombCountUpdate.sendToAllClients().add(bombCount);
    }

    public void receiveBombCountUpdate(GameUpdate.Incoming update, UUID sender) {
        int bombCount = update.get(Integer.class);
        game.bombCount = bombCount;
    }

    public void sendBombReveal(Vec2i[] bombs) {
        GameUpdate.Outgoing update = bombReveal.sendToAllClients();
        update.add(bombs.length);

        for (Vec2i bomb : bombs) {
            update.add(bomb.getX()).add(bomb.getY());
        }
    }

    public void receiveBombReveal(GameUpdate.Incoming update, UUID sender) {
        int bombs = update.get(Integer.class);

        for (int i = 0; i < bombs; i++) {
            game.tiles.get(update.get(Integer.class), update.get(Integer.class)).setBomb();
        }
    }

    public void sendTileClick(Direction1D mouse, Vec2i tile) {
        tileClick.sendToServer().add(mouse.ordinal()).add(tile.getX()).add(tile.getY());
    }

    public void receiveTileClick(GameUpdate.Incoming update, UUID sender) {
        Direction1D mouse = Direction1D.values()[update.get(Integer.class)];
        Vec2i tileClicked = new Vec2i(update.get(Integer.class), update.get(Integer.class));

        if (mouse == Direction1D.LEFT) {
            game.reveal(tileClicked);
        } else if (mouse == Direction1D.RIGHT) {
            game.cycle(tileClicked);
        }
    }

    public void sendTileReveal(TileInfo[] tiles) {
        GameUpdate.Outgoing update = tileReveal.sendToAllClients();

        update.add(tiles.length);
        for (TileInfo tile : tiles) {
            update
                    .add((short) tile.coords.getX())
                    .add((short) tile.coords.getY())
                    .add((byte) tile.adjacentBombs);
        }
    }

    public void receiveTileReveal(GameUpdate.Incoming update, UUID sender) {
        int tileCount = update.get(Integer.class);

        for (int i = 0; i < tileCount; i++) {
            TileInfo tile = new TileInfo(new Vec2i(update.get(Short.class), update.get(Short.class)), update.get(Byte.class));

            DefusalTile defusalTile = game.tiles.get(tile.coords.getX(), tile.coords.getY());
            defusalTile.adjacentBombs = tile.adjacentBombs;
            defusalTile.reveal();
            game.lastRevealTime = game.getGameTime();
        }

        game.playSound(GameblockSounds.DEFUSAL_SELECT.get(), 1.0f, 0.5f);
    }

    public void sendTileState(Vec2i tile, DefusalTile.State state) {
        tileState.sendToAllClients()
                .add((short) tile.getX())
                .add((short) tile.getY())
                .add(state.ordinal());
    }

    public void receiveTileState(GameUpdate.Incoming update, UUID sender) {
        Vec2i tile = new Vec2i(update.get(Short.class), update.get(Short.class));
        DefusalTile.State state = DefusalTile.State.values()[update.get(Integer.class)];

        game.tiles.get(tile.getX(), tile.getY()).setState(state);
    }

    public void sendTime(int ticks, UUID player) {
        time.sendToClient(player).add(ticks / 20);
    }

    public void sendTimeToAll(int ticks) {
        time.sendToAllClients().add(ticks / 20);
    }

    public void receiveTime(GameUpdate.Incoming update, UUID sender) {
        int seconds = update.get(Integer.class);

        game.timeLeft = seconds * 20;
        game.playSound(GameblockSounds.DEFUSAL_TIMER.get());
    }

    public static class TileInfo {
        private Vec2i coords;
        private int adjacentBombs;

        public TileInfo(Vec2i coords, int adjacentBombs) {
            this.coords = coords;
            this.adjacentBombs = adjacentBombs;
        }
    }
}
