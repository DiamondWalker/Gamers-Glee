package gameblock.game.defusal;

public class DefusalTile {
    private State state = State.HIDDEN;
    public int adjacentBombs = 0;
    private boolean bomb = false;

    public void reveal() {
        state = State.REVEALED;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setBomb() {
        bomb = true;
    }

    public boolean isBomb() {
        return bomb;
    }

    public void cycleState() {
        state = switch (state) {
            case HIDDEN -> State.FLAGGED;
            case FLAGGED -> State.QUESTION;
            case QUESTION -> State.HIDDEN;
            default -> state;
        };
    }

    enum State {
        HIDDEN,
        REVEALED,
        FLAGGED,
        QUESTION
    }
}
