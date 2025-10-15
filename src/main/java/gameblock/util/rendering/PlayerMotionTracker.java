package gameblock.util.rendering;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * This utility class helps deal with Minecraft's weird player movement system.
 * Every time the tick() function is called, it stores the player's position so it can be checked for changes between ticks.
 */
public class PlayerMotionTracker {
    private final Player playerTracked;
    private Vec3 prevPlayerPos;
    private boolean playerMoved = false;

    public PlayerMotionTracker(Player player) {
        this.playerTracked = player;
        prevPlayerPos = playerTracked.position();
    }

    public void tick() {
        playerMoved = playerTracked.position().distanceToSqr(prevPlayerPos) > 0;
        prevPlayerPos = playerTracked.position();
    }

    public Vec3 getPreviousPlayerPos() {
        return prevPlayerPos;
    }

    public boolean hasPlayerMoved() {
        return playerMoved;
    }
}
