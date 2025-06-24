package gameblock.game;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.function.Consumer;

public abstract class Game {
    private final HashMap<Integer, KeyBinding> keyBindings = new HashMap<>();

    public static final int MAX_X = 100;
    public static final int MAX_Y = 75;

    public abstract void tick();

    public abstract void render(GuiGraphics graphics, float partialTicks);

    protected final void drawRectangle(GuiGraphics graphics, float x, float y, float width, float height, int red, int green, int blue, int alpha, float angle) {
        int color = FastColor.ARGB32.color(alpha, red, green, blue);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        pose.mulPose(Axis.ZP.rotation(angle));
        Matrix4f matrix4f = pose.last().pose();

        float pMinX = -width / 2;
        float pMaxX = width / 2;
        float pMinY = -height / 2;
        float pMaxY = height / 2;

        if (pMinX < pMaxX) {
            float i = pMinX;
            pMinX = pMaxX;
            pMaxX = i;
        }

        if (pMinY < pMaxY) {
            float j = pMinY;
            pMinY = pMaxY;
            pMaxY = j;
        }

        float f3 = (float)FastColor.ARGB32.alpha(color) / 255.0F;
        float f = (float)FastColor.ARGB32.red(color) / 255.0F;
        float f1 = (float)FastColor.ARGB32.green(color) / 255.0F;
        float f2 = (float)FastColor.ARGB32.blue(color) / 255.0F;
        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(RenderType.gui());
        vertexconsumer.vertex(matrix4f, pMinX, pMinY, 0.0f).color(f, f1, f2, f3).endVertex();
        vertexconsumer.vertex(matrix4f, pMinX, pMaxY, 0.0f).color(f, f1, f2, f3).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxX, pMaxY, 0.0f).color(f, f1, f2, f3).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxX, pMinY, 0.0f).color(f, f1, f2, f3).endVertex();
        graphics.flush();

        pose.popPose();
    }

    protected final KeyBinding registerKey(int key) {
        KeyBinding binding = new KeyBinding();
        keyBindings.put(key, binding);
        return binding;
    }

    protected final KeyBinding registerKey(int key, Runnable pressAction) {
        KeyBinding binding = new KeyBinding(pressAction);
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
