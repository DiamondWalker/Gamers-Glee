package gameblock.game;

import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;
import java.util.function.Consumer;

public abstract class Game {
    private final HashMap<Integer, KeyBinding> keyBindings = new HashMap<>();

    public abstract void tick();

    public abstract void render(GuiGraphics graphics, float partialTicks);

    protected final KeyBinding registerKey(int key) {
        KeyBinding binding = new KeyBinding();
        keyBindings.put(key, binding);
        return binding;
    }

    public final boolean pressKey(int key) {
        KeyBinding binding = keyBindings.get(key);
        if (binding != null) {
            if (binding.pressAction != null) binding.pressAction.run();
            binding.pressed = true;
            return true;
        }
        return false;
    }

    public final boolean releaseKey(int key) {
        if (keyBindings.containsKey(key)) {
            keyBindings.get(key).pressed = false;
            return true;
        }
        return false;
    }

    protected static class KeyBinding {
        public boolean pressed = false;
        public Runnable pressAction;

        protected KeyBinding() {
            this(null);
        }

        protected KeyBinding(Runnable action) {
            this.pressAction = action;
        }
    }
}
