package gameblock.game.os;

import gameblock.game.GameInstance;
import gameblock.item.CartridgeItem;
import gameblock.registry.*;
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

public class GameblockOS extends GameInstance {
    private static final int BLOCK_WIDTH = 4;
    private static final int BLOCK_FADE_TIME = 10;
    private final ArrayList<Vec2i> titleBlocks = new ArrayList<>();

    protected HashSet<GameblockGames.Game> gamesFound = null;

    private void addBlock(int x, int y) {
        titleBlocks.add(new Vec2i(x, y));
    }

    public GameblockOS(Player player) {
        super(player);

        // G
        addBlock(-19, 0);
        addBlock(-18, 0);
        addBlock(-17, 1);
        addBlock(-17, 2);
        addBlock(-18, 2);
        addBlock(-20, 1);
        addBlock(-20, 2);
        addBlock(-20, 3);
        addBlock(-20, 4);
        addBlock(-19, 5);
        addBlock(-18, 5);

        // A
        addBlock(-15, 0);
        addBlock(-15, 1);
        addBlock(-15, 2);
        addBlock(-15, 3);
        addBlock(-15, 4);

        addBlock(-14, 3);
        addBlock(-13, 3);

        addBlock(-14, 5);
        addBlock(-13, 5);

        addBlock(-12, 0);
        addBlock(-12, 1);
        addBlock(-12, 2);
        addBlock(-12, 3);
        addBlock(-12, 4);

        // M
        addBlock(-10, 0);
        addBlock(-10, 1);
        addBlock(-10, 2);
        addBlock(-10, 3);
        addBlock(-10, 4);

        addBlock(-9, 5);

        addBlock(-8, 0);
        addBlock(-8, 1);
        addBlock(-8, 2);
        addBlock(-8, 3);
        addBlock(-8, 4);

        addBlock(-7, 5);

        addBlock(-6, 0);
        addBlock(-6, 1);
        addBlock(-6, 2);
        addBlock(-6, 3);
        addBlock(-6, 4);

        // E
        addBlock(-4, 0);
        addBlock(-4, 1);
        addBlock(-4, 2);
        addBlock(-4, 3);
        addBlock(-4, 4);
        addBlock(-4, 5);

        addBlock(-3, 0);
        addBlock(-2, 0);

        addBlock(-3, 2);
        addBlock(-2, 2);

        addBlock(-3, 5);
        addBlock(-2, 5);

        // B
        addBlock(0, 0);
        addBlock(0, 1);
        addBlock(0, 2);
        addBlock(0, 3);
        addBlock(0, 4);
        addBlock(0, 5);

        addBlock(1, 0);
        addBlock(1, 2);
        addBlock(1, 5);

        addBlock(2, 0);
        addBlock(2, 1);
        addBlock(2, 3);
        addBlock(2, 4);

        // L
        addBlock(4, 0);
        addBlock(4, 1);
        addBlock(4, 2);
        addBlock(4, 3);
        addBlock(4, 4);
        addBlock(4, 5);

        addBlock(5, 0);
        addBlock(6, 0);

        // O
        addBlock(8, 1);
        addBlock(8, 2);
        addBlock(8, 3);
        addBlock(8, 4);

        addBlock(9, 0);
        addBlock(9, 5);

        addBlock(10, 0);
        addBlock(10, 5);

        addBlock(11, 1);
        addBlock(11, 2);
        addBlock(11, 3);
        addBlock(11, 4);

        // C
        addBlock(13, 1);
        addBlock(13, 2);
        addBlock(13, 3);
        addBlock(13, 4);

        addBlock(14, 0);
        addBlock(14, 5);

        addBlock(15, 0);
        addBlock(15, 5);

        // K
        addBlock(17, 0);
        addBlock(17, 1);
        addBlock(17, 2);
        addBlock(17, 3);
        addBlock(17, 4);
        addBlock(17, 5);

        addBlock(18, 2);
        addBlock(19, 1);
        addBlock(20, 0);

        addBlock(18, 3);
        addBlock(19, 4);
        addBlock(20, 5);
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
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        if (getGameTime() <= 160) {
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
        } else if (getGameTime() > 200) {
            drawRectangle(graphics, RenderType.endPortal(), 0, 0, 200, 200, new ColorF(0, 0, 0, 1.0f), 0);

            float iconTransparency = (partialTicks + getGameTime() - 200) / 40;
            iconTransparency = Mth.clamp(iconTransparency, 0.0f, 1.0f);

            int count = 0;
            for (GameblockGames.Game game : gamesFound) { // FIXME: if lag or something prevents the games from being sent during the loading screen this will cause a crash
                int x = (count % 4) * 40 - 60;
                int y = 45 - (count / 4) * 30;
                drawRectangle(graphics, x, y + 5.5f, 9.0f, 9.0f, new ColorF(1.0f).withAlpha(iconTransparency), 0);
                //drawTexture(graphics, game.logo, x, y + 5.5f, 9.0f, 9.0f, 0, 0, 0, 5, 5);
                drawText(graphics, x, y - 5.5f, 0.45f, 0, 2, game.gameID, new ColorF(1.0f).withAlpha(iconTransparency));
                count++;
            }
        }
        //drawText(graphics, 0, 0, 1.0f, 50, 3, "This is some long text to test the rendering");
    }

    @Override
    public Music getMusic() {
        return getGameTime() > 200 ? GameblockMusic.OS : null;
    }
}
