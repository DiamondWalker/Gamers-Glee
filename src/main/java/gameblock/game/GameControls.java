package gameblock.game;

import net.minecraft.world.phys.Vec2;

import java.util.HashMap;

public class GameControls {
    private final HashMap<Integer, KeyBinding> keyBindings = new HashMap<>();
    private Vec2 mouseCoordinates = new Vec2(Float.NaN, Float.NaN);

    public void setMouseCoordinates(Vec2 coords) {
        this.mouseCoordinates = coords;
    }

    public Vec2 getMouseCoordinates() {
        return mouseCoordinates;
    }

    public final KeyBinding registerKey(int key) {
        KeyBinding binding = new KeyBinding();
        keyBindings.put(key, binding);
        return binding;
    }

    public final KeyBinding registerKey(int key, Runnable pressAction) {
        KeyBinding binding = new KeyBinding(pressAction);
        keyBindings.put(key, binding);
        return binding;
    }

    public final boolean pressKey(int key) {
        //if (isGameOver()) return false;
        KeyBinding binding = keyBindings.get(key);
        if (binding != null) {
            if (binding.pressAction != null) binding.pressAction.run();
            binding.pressed = true;
            return true;
        }
        return false;
    }

    public final boolean releaseKey(int key) {
        //if (isGameOver()) return false;
        if (keyBindings.containsKey(key)) {
            keyBindings.get(key).pressed = false;
            return true;
        }
        return false;
    }

    public static class KeyBinding {
        private boolean pressed = false;
        private final Runnable pressAction;

        protected KeyBinding() {
            this(null);
        }

        protected KeyBinding(Runnable action) {
            this.pressAction = action;
        }
    }
}
