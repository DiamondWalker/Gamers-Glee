package gameblock.game.os;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gameblock.game.GameInstance;
import gameblock.item.CartridgeItem;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockMusic;
import gameblock.registry.GameblockPackets;
import gameblock.util.CircularStack;
import gameblock.util.ColorF;
import gameblock.util.Vec2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class GameblockOS extends GameInstance {
    private static final int BLOCK_WIDTH = 4;
    private static final int BLOCK_FADE_TIME = 10;
    private static final int MENU_LOAD_TIME = 160;
    private final ArrayList<Vec2i> titleBlocks = new ArrayList<>(List.of(
            // G
            new Vec2i(-19, 0),
            new Vec2i(-18, 0),
            new Vec2i(-17, 1),
            new Vec2i(-17, 2),
            new Vec2i(-18, 2),
            new Vec2i(-20, 1),
            new Vec2i(-20, 2),
            new Vec2i(-20, 3),
            new Vec2i(-20, 4),
            new Vec2i(-19, 5),
            new Vec2i(-18, 5),

            // A
            new Vec2i(-15, 0),
            new Vec2i(-15, 1),
            new Vec2i(-15, 2),
            new Vec2i(-15, 3),
            new Vec2i(-15, 4),

            new Vec2i(-14, 3),
            new Vec2i(-13, 3),

            new Vec2i(-14, 5),
            new Vec2i(-13, 5),

            new Vec2i(-12, 0),
            new Vec2i(-12, 1),
            new Vec2i(-12, 2),
            new Vec2i(-12, 3),
            new Vec2i(-12, 4),

            // M
            new Vec2i(-10, 0),
            new Vec2i(-10, 1),
            new Vec2i(-10, 2),
            new Vec2i(-10, 3),
            new Vec2i(-10, 4),

            new Vec2i(-9, 5),

            new Vec2i(-8, 0),
            new Vec2i(-8, 1),
            new Vec2i(-8, 2),
            new Vec2i(-8, 3),
            new Vec2i(-8, 4),

            new Vec2i(-7, 5),

            new Vec2i(-6, 0),
            new Vec2i(-6, 1),
            new Vec2i(-6, 2),
            new Vec2i(-6, 3),
            new Vec2i(-6, 4),

            // E
            new Vec2i(-4, 0),
            new Vec2i(-4, 1),
            new Vec2i(-4, 2),
            new Vec2i(-4, 3),
            new Vec2i(-4, 4),
            new Vec2i(-4, 5),

            new Vec2i(-3, 0),
            new Vec2i(-2, 0),

            new Vec2i(-3, 2),
            new Vec2i(-2, 2),

            new Vec2i(-3, 5),
            new Vec2i(-2, 5),

            // B
            new Vec2i(0, 0),
            new Vec2i(0, 1),
            new Vec2i(0, 2),
            new Vec2i(0, 3),
            new Vec2i(0, 4),
            new Vec2i(0, 5),

            new Vec2i(1, 0),
            new Vec2i(1, 2),
            new Vec2i(1, 5),

            new Vec2i(2, 0),
            new Vec2i(2, 1),
            new Vec2i(2, 3),
            new Vec2i(2, 4),

            // L
            new Vec2i(4, 0),
            new Vec2i(4, 1),
            new Vec2i(4, 2),
            new Vec2i(4, 3),
            new Vec2i(4, 4),
            new Vec2i(4, 5),

            new Vec2i(5, 0),
            new Vec2i(6, 0),

            // O
            new Vec2i(8, 1),
            new Vec2i(8, 2),
            new Vec2i(8, 3),
            new Vec2i(8, 4),

            new Vec2i(9, 0),
            new Vec2i(9, 5),

            new Vec2i(10, 0),
            new Vec2i(10, 5),

            new Vec2i(11, 1),
            new Vec2i(11, 2),
            new Vec2i(11, 3),
            new Vec2i(11, 4),

            // C
            new Vec2i(13, 1),
            new Vec2i(13, 2),
            new Vec2i(13, 3),
            new Vec2i(13, 4),

            new Vec2i(14, 0),
            new Vec2i(14, 5),

            new Vec2i(15, 0),
            new Vec2i(15, 5),

            // K
            new Vec2i(17, 0),
            new Vec2i(17, 1),
            new Vec2i(17, 2),
            new Vec2i(17, 3),
            new Vec2i(17, 4),
            new Vec2i(17, 5),

            new Vec2i(18, 2),
            new Vec2i(19, 1),
            new Vec2i(20, 0),

            new Vec2i(18, 3),
            new Vec2i(19, 4),
            new Vec2i(20, 5)
    ));

    private final CircularStack<BackgroundBlock> cubes = new CircularStack<>(150);

    protected HashSet<GameblockGames.Game> gamesFound = null;

    public GameblockOS(Player player) {
        super(player);
    }

    @Override
    protected void tick() {
        if (!isClientSide() && gamesFound == null) {
            gamesFound = new HashSet<>();
            Inventory playerInventory = player.getInventory();
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                ItemStack stack = playerInventory.getItem(i);
                if (stack != null && stack.getItem() instanceof CartridgeItem<?> cartridge) {
                    if (!gamesFound.contains(cartridge.gameType)) gamesFound.add(cartridge.gameType);
                }
            }
            GameblockPackets.sendToPlayer((ServerPlayer) player, new GamesListPacket(gamesFound.toArray(new GameblockGames.Game[0])));
        }

        if (isClientSide()) {
            if (getGameTime() % 10 == 0) {
                cubes.enqueue(new BackgroundBlock());
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        if (getGameTime() <= MENU_LOAD_TIME) {
            for (Vec2i block : titleBlocks) {
                long time = block.getX() + block.getY() + 50;
                if (getGameTime() >= time) {
                    time = getGameTime() - time;
                    float f = Math.min(1.0f, ((float)time + partialTicks) / BLOCK_FADE_TIME);

                    float wave = 1.0f;
                    float waveTime = getGameTime() - 85 + partialTicks;
                    if (waveTime >= 0) {
                        wave = Mth.cos(waveTime / 2) / 2 + 0.5f;
                        wave = wave * 0.15f + 0.85f;
                    }

                    drawRectangle(graphics, block.getX() * BLOCK_WIDTH, (block.getY() - 2.5f) * BLOCK_WIDTH, // blocks are translated down 2.5 units so they're centered
                            BLOCK_WIDTH, BLOCK_WIDTH,
                            new ColorF(wave, wave, wave, f), 0);
                }
            }
        } else if (menuLoaded()) {
            drawRectangle(graphics, 0, 0, 200, 200, new ColorF(1, 18, 23, 255), 0);
            PoseStack pose = graphics.pose();

            cubes.forEach((BackgroundBlock block) -> {
                for (int i = 0; i < 8; i++) {
                    float time = partialTicks + getGameTime() - block.timeStart;
                    time -= i * 10;
                    float scale = (time * time) / 30000;
                    float alpha = Mth.clamp((80 - (time - 1200)) / 80, 0.0f, 1.0f);
                    alpha *= (1.0f - (float)i / 8);

                    pose.pushPose();
                    pose.mulPose(Axis.ZP.rotation((partialTicks + getGameTime() - i * 10) / 400));
                    pose.scale(scale, scale, 1);
                    pose.translate(block.x, block.y, /*100 - time*/0);
                    drawRectangle(graphics, RenderType.gui(), 0,0, 0.2f, 0.2f, new ColorF(5, 129, 62).withAlpha(0.8f * alpha), 0);
                    pose.popPose();
                }
            });

            float iconTransparency = (partialTicks + getGameTime() - 200) / 40;
            iconTransparency = Mth.clamp(iconTransparency, 0.0f, 1.0f);

            int cubeCount = 0;
            for (GameblockGames.Game game : gamesFound) {
                int x = (cubeCount % 4) * 40 - 60;
                int y = 45 - (cubeCount / 4) * 30;
                drawRectangle(graphics, x, y + 5.5f, 9.0f, 9.0f, new ColorF(1.0f).withAlpha(iconTransparency), 0);
                //drawTexture(graphics, game.logo, x, y + 5.5f, 9.0f, 9.0f, 0, 0, 0, 5, 5);
                drawText(graphics, x, y - 5.5f, 0.45f, 0, 2, game.gameID, new ColorF(1.0f).withAlpha(iconTransparency));
                cubeCount++;
            }
        } else { // loading screen
            for (int i = 0; i < 8; i++) {
                float angle = Mth.HALF_PI - (Mth.TWO_PI / 8) * i;
                int currentlyLitRect = (int) ((getGameTime() / 3) % 8);
                drawRectangle(graphics, Mth.cos(angle) * 12, Mth.sin(angle) * 12, 5.0f, 4.0f, new ColorF(i == currentlyLitRect ? 1.0f : 0.3f), angle);
            }
        }
    }

    private boolean menuLoaded() {
        return getGameTime() > MENU_LOAD_TIME && gamesFound != null;
    }

    @Override
    public Music getMusic() {
        return menuLoaded() ? GameblockMusic.OS : null;
    }

    private class BackgroundBlock {
        private float x;
        private float y;
        private long timeStart;

        private BackgroundBlock() {
            Random rand = new Random();
            float magnitude = rand.nextFloat() * 7.071f;
            float angle = rand.nextFloat(Mth.TWO_PI);
            this.x = Mth.cos(angle) * magnitude;
            this.y = Mth.sin(angle) * magnitude;
            this.timeStart = getGameTime();
        }
    }
}
