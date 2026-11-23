package gameblock.game.games.os;

import gameblock.util.ColorF;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class OSIcon {
    public static final int SELECTION_TIME = 5;

    public final GameblockOS os;
    public final Runnable clickAction;
    public final ResourceLocation icon;
    public final Component translationKey;
    private final Vec2 iconPos;
    private boolean selected = false;
    private int selectionTicks = 0;

    public OSIcon(GameblockOS os, Runnable clickAction, ResourceLocation icon, Component translationKey, Vec2 iconPos) {
        this.os = os;
        this.clickAction = clickAction;
        this.icon = icon;
        this.translationKey = translationKey;
        this.iconPos = iconPos;
    }

    public boolean isOverIcon(Vec2 position) {
        Vec2 offset = position.add(iconPos.negated());
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

    public void render(float partialTicks, float transparency) {
        float selectedFade = ((selected ? partialTicks : -partialTicks) + selectionTicks) / SELECTION_TIME;
        selectedFade = Mth.clamp(selectedFade, 0.0f, 1.0f);
        float scale = 1.0f + 0.2f * selectedFade;
        ColorF textColor = new ColorF(1.0f).fadeTo(new ColorF(1.0f, 1.0f, 0.0f), selectedFade);

        os.renderer.push();
        os.renderer.translate(iconPos.x, iconPos.y);
        os.renderer.scale(scale);

        os.renderer.drawTexture(icon, 0, 5.0f, 15.0f, 15.0f, 0, new ColorF(1.0f).withAlpha(transparency));
        os.renderer.drawText(0, -5.5f, 0.5f, 0, 2, textColor.withAlpha(transparency), translationKey);

        os.renderer.pop();
    }
}
