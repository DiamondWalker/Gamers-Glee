package gameblock.util;

import gameblock.GameblockMod;
import gameblock.game.GameInstance;

public final class TickTimer {
    private final GameInstance<?, ?> gameInstance;

    private long targetTicks;
    private long ticksElapsed;
    private Runnable scheduledAction;
    private TimerState state = TimerState.STOPPED;

    public TickTimer(GameInstance<?, ?> game) {
        this.gameInstance = game;
    }

    public void start() {
        start(-1);
    }

    public void start(long time) {
        start(time, null);
    }

    public void start(long time, Runnable action) {
        if (state == TimerState.RUNNING) {
            GameblockMod.LOGGER.warn("Timer was already running!");
        }
        ticksElapsed = 0;
        targetTicks = time;
        state = TimerState.RUNNING;
        this.scheduledAction = action;
        gameInstance.addTickTimer(this);
    }

    public void reset() {
        ticksElapsed = 0;
        targetTicks = 0;
        state = TimerState.STOPPED;
        scheduledAction = null;
    }

    public long getTicksElapsed() {
        return ticksElapsed;
    }

    public float getInterpolatedTicksElapsed() {
        return state == TimerState.RUNNING ? gameInstance.getPartialTicks() + ticksElapsed : (float) ticksElapsed;
    }

    public long getTicksRemaining() {
        return targetTicks - ticksElapsed;
    }

    public float getProgress() {
        return getInterpolatedTicksElapsed() / targetTicks;
    }
    public TimerState getState() {
        return state;
    }

    public void tick() {
        ticksElapsed++;
        if (targetTicks >= 0 && ticksElapsed >= targetTicks) {
            state = TimerState.DONE;
        }
    }

    public void executeScheduledAction() {
        if (scheduledAction != null) scheduledAction.run();
    }

    public enum TimerState {
        STOPPED,
        RUNNING,
        DONE
    }
}
