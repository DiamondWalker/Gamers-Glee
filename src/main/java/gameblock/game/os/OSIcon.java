package gameblock.game.os;

import com.mojang.blaze3d.vertex.PoseStack;
import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.util.ColorF;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class OSIcon<T extends GameInstance> {
    public static final int SELECTION_TIME = 5;

    public final GameblockOS os;
    public final GameblockGames.Game<T> game;
    public final int index;
    private boolean selected = false;
    private int selectionTicks = 0;

    public OSIcon(GameblockOS os, GameblockGames.Game<T> game, int index) {
        this.os = os;
        this.game = game;
        this.index = index;
    }

    private Vec2 getPosition() {
        int x = (index % 4) * 40 - 60;
        int y = 45 - (index / 4) * 30;
        return new Vec2(x, y);
    }

    public boolean isOverIcon(Vec2 position) {
        Vec2 offset = position.add(getPosition().negated());
        return Math.abs(offset.x) < 7.5f && Math.abs(offset.y) < 7.5f;
    }

    public void tick(Vec2 mousePosition) {
        if (isOverIcon(mousePosition)) {
            selected = true;
            if (selectionTicks < SELECTION_TIME) selectionTicks++;
        } else {
            selected = false;
            if (selectionTicks > 0) selectionTicks--;
        }
    }

    public void render(GuiGraphics graphics, float partialTicks, float transparency) {
        Vec2 center = getPosition();
        float selectedFade = ((selected ? partialTicks : -partialTicks) + selectionTicks) / SELECTION_TIME;
        selectedFade = Mth.clamp(selectedFade, 0.0f, 1.0f);
        float scale = 1.0f + 0.2f * selectedFade;
        ColorF textColor = new ColorF(1.0f).fadeTo(new ColorF(1.0f, 1.0f, 0.0f), selectedFade);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(center.x, center.y, 0.0f);
        poseStack.scale(scale, scale, scale);

        os.drawTexture(graphics, game.logo, 0, 5.0f, 15.0f, 15.0f, 0, new ColorF(1.0f).withAlpha(transparency));
        os.drawText(graphics, 0, -5.5f, 0.5f, 0, 2, textColor.withAlpha(transparency), "game.gameblock." + game.gameID);

        poseStack.popPose();
    }
}
