package gameblock.game;

import net.minecraft.world.entity.player.Player;

public class GamePlayer<T extends GamePlayer.GamePlayerData> {
    private Player playerEntity;
    private T data;
    private int id;

    public GamePlayer(Player player, int id, T data) {
        this.playerEntity = player;
        this.id = id;
        this.data = data;
    }

    protected void invalidate() {
        playerEntity = null;
        data = null;
    }

    private void verifyOrThrowException() {
        if (!isActivePlayer()) throw new IllegalStateException("Attempted to access player " + id + ", which has left the game.");
    }

    public boolean isActivePlayer() {
        return playerEntity != null && data != null;
    }

    public Player playerEntity() {
        verifyOrThrowException();
        return playerEntity;
    }

    public T data() {
        verifyOrThrowException();
        return data;
    }

    protected class GamePlayerData {
    }
}
