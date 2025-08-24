package gameblock.game;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.world.phys.Vec2;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class GamePrompt<T extends GameInstance<?>> {
    protected final T game;

    private final StringBuilder builder = new StringBuilder();
    protected int location = 0;

    private boolean shouldClose = false;

    public GamePrompt(T game) {
        this.game = game;
    }

    public boolean handleKeyPress(int key) {
        if (key == InputConstants.KEY_ESCAPE) {
            close();
        } else if (key == InputConstants.KEY_LEFT) {
            location = Math.max(location - 1, 0);
        } else if (key == InputConstants.KEY_RIGHT) {
            location = Math.min(location + 1, builder.length());
        } else if (key == InputConstants.KEY_BACKSPACE) {
            if (location > 0) {
                builder.deleteCharAt(location - 1);
                location--;
            }
        } else if (key == InputConstants.KEY_END) {
            location = builder.length();
        } else if (key == InputConstants.KEY_HOME) {
            location = 0;
        } else if (key == InputConstants.KEY_DELETE) {
            if (location < builder.length()) builder.deleteCharAt(location);
        }
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

    public abstract static class GameCodePrompt<T extends GameInstance<?>> extends GamePrompt<T> {
        public GameCodePrompt(T game) {
            super(game);
        }

        @Override
        public void handleCharTyped(char character) {
            if (get().length() < 8 && Character.isLetterOrDigit(character)) super.handleCharTyped(Character.toUpperCase(character));
        }
    }
}
