package gameblock.game;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.world.phys.Vec2;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class GamePrompt<T extends GameInstance<?>> {
    protected final T game;

    private final StringBuilder builder = new StringBuilder();
    private int location = 0;

    private boolean shouldClose = false;

    public GamePrompt(T game) {
        this.game = game;
    }

    public boolean handleKeyPress(int key) {
        return true;
    }

    public void handleCharTyped(char character) {
        builder.insert(location, character);
        location++;
    }

    public boolean click(Vec2 clickCoordinates) {
        return true;
    }

    public void close() {
        shouldClose = true;
    }

    public boolean shouldClose() {
        return shouldClose;
    }

    public String get() {
        return builder.toString();
    }

    public abstract void render(GuiGraphics graphics, float partialTicks);
}
