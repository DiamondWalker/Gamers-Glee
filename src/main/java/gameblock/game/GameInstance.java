package gameblock.game;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockItems;
import gameblock.registry.GameblockPackets;
import gameblock.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class GameInstance<T extends GameInstance<?>> {
    public final GameblockGames.Game<T> gameType;
    private final HashMap<Integer, KeyBinding> keyBindings = new HashMap<>();
    private Vec2 mouseCoordinates = new Vec2(Float.NaN, Float.NaN);

    private final Player[] players;
    private final boolean clientSide;

    public static final int MAX_X = 100;
    public static final int MAX_Y = 75;

    private long gameTime = 0;
    private GameState gameState = GameState.ACTIVE;

    public GamePrompt prompt = null;

    public final GameblockSoundManager soundManager;

    public GameInstance(Player player, GameblockGames.Game<T> gameType) {
        clientSide = player.level().isClientSide();
        soundManager = clientSide ? new GameblockSoundManager() : null;
        this.players = new Player[clientSide ? 1 : getMaxPlayers()];
        this.players[0] = player;
        this.gameType = gameType;
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public int getMaxPlayers() {
        return 1;
    }

    public final Player getHostPlayer() {
        return players[0];
    }

    public final Player getPlayer(int i) {
        return players[i];
    }

    public final int getPlayerIndex(Player player) {
        for (int i = 0; i < players.length; i++) if (players[i] == player) return i;
        throw new IllegalArgumentException("Player not found!");
    }

    public void forEachPlayer(Consumer<Player> action) {
        for (Player player : players) {
            if (player != null) action.accept(player);
        }
    }

    public void forEachPlayerExcluding(Consumer<Player> action, Player excluded) {
        for (Player player : players) {
            if (player != null && player != excluded) action.accept(player);
        }
    }

    public final boolean addPlayer(ServerPlayer player) {
        GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
        if (cap != null) {
            for (int i = 1; i < players.length; i++) {
                if (players[i] == null) {
                    players[i] = player;
                    cap.setGameInstance(this, player);
                    onPlayerJoined(player);
                    return true;
                }
            }
        }
        return false;
    }

    protected void onPlayerJoined(ServerPlayer player) {

    }

    public final int getPlayerCount() {
        int count = 0;
        for (int i = 0; i < players.length; i++) {
            if (players[i] != null) count++;
        }
        return count;
    }

    public String getGameCode() {
        return null;
    }

    protected final void setGameState(GameState state) {
        gameState = state;
        if (!isClientSide()) {
            for (Player player : players) GameblockPackets.sendToPlayer((ServerPlayer) player, new GameStatePacket(state));
        }
        if (state == GameState.WIN) {
            onGameWin();
        } else if (state == GameState.LOSS) {
            onGameLoss();
        }
    }

    public final void save() {
        ItemStack gameblockItem = null;
        for (InteractionHand hand : InteractionHand.values()) {
            gameblockItem = getHostPlayer().getItemInHand(hand);
            if (gameblockItem.is(GameblockItems.GAMEBLOCK.get())) {
                break;
            } else {
                gameblockItem = null;
            }
        }

        if (gameblockItem != null) {
            String playerName = getHostPlayer().getGameProfile().getName();
            String gameName = gameType.gameID;
            CompoundTag tag = gameblockItem.getOrCreateTag();

            if (!tag.contains("gameSaveData")) tag.put("gameSaveData", new CompoundTag());
            tag = tag.getCompound("gameSaveData");

            if (!tag.contains(playerName)) tag.put(playerName, new CompoundTag());
            tag = tag.getCompound(playerName);

            CompoundTag saveData = writeSaveData();
            if (saveData != null) {
                tag.put(gameName, saveData);
            }
        }
    }

    public final void load() {
        ItemStack gameblockItem = null;
        for (InteractionHand hand : InteractionHand.values()) {
            gameblockItem = getHostPlayer().getItemInHand(hand);
            if (gameblockItem.is(GameblockItems.GAMEBLOCK.get())) {
                break;
            } else {
                gameblockItem = null;
            }
        }

        if (gameblockItem != null) {
            String playerName = getHostPlayer().getGameProfile().getName();
            String gameName = gameType.gameID;
            CompoundTag tag = gameblockItem.getOrCreateTag();

            if (tag.contains("gameSaveData")) {
                tag = tag.getCompound("gameSaveData");

                if (tag.contains(playerName)) {
                    tag = tag.getCompound(playerName);

                    if (tag.contains(gameName)) {
                        tag = tag.getCompound(gameName);

                        readSaveData(tag);
                    }
                }
            }
        }
    }

    protected CompoundTag writeSaveData() {
        return null;
    }

    protected void readSaveData(CompoundTag tag) {

    }

    public final void restart() {
        if (!isClientSide()) {
            for (Player player : players) {
                GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (cap != null) {
                    cap.setGame(gameType, player);
                }
            }
        } else {
            GameblockPackets.sendToServer(new GameRestartPacket());
        }
    }

    public final GameState getGameState() {
        return gameState;
    }

    protected void onGameWin() {

    }

    protected void onGameLoss() {

    }

    public long getGameTime() {
        return gameTime;
    }

    public void setMouseCoordinates(Vec2 coords) {
        this.mouseCoordinates = coords;
    }

    public Vec2 getMouseCoordinates() {
        return mouseCoordinates;
    }

    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {}

    protected boolean isGameOver() {
        return gameState != GameState.ACTIVE;
    }

    public final void baseTick(Player player) {
        if (prompt != null && prompt.shouldClose()) prompt = null;

        if (player == getHostPlayer()) {
            tick();
            gameTime++;
        }

        if (!isClientSide()) {
            boolean stayOpen = true;
            if ((!player.getItemInHand(InteractionHand.MAIN_HAND).is(GameblockItems.GAMEBLOCK.get()) && !player.getItemInHand(InteractionHand.OFF_HAND).is(GameblockItems.GAMEBLOCK.get()))) {
                stayOpen = false;
            } else if (getHostPlayer() == null || !getHostPlayer().isAlive()) {
                stayOpen = false;
            } else {
                GameCapability hostCapability = getHostPlayer().getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (hostCapability == null || !hostCapability.isPlaying()) stayOpen = false;
            }

            if (!stayOpen) {
                GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (cap != null && cap.isPlaying()) {
                    cap.setGame(null, player);
                }
            }
        }
    }

    protected abstract void tick();

    public abstract void render(GuiGraphics graphics, float partialTicks);

    public Music getMusic() {
        return null;
    }

    public final void playSound(SoundEvent event, float pitch, float volume) {
        if (soundManager != null) soundManager.play(event, pitch, volume);
    }

    public final void playSound(SoundEvent event) {
        playSound(event, 1.0f, 1.0f);
    }

    private void drawText(GuiGraphics graphics, float x, float y, float scale, Component txt, ColorF color) {
        Font font = Minecraft.getInstance().font;
        float width = font.width(txt);
        float height = font.lineHeight;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x - scale * (width / 2 - 0.5f), y + scale * (height / 2 - 1), 0);
        pose.scale(scale, -scale, 1.0f);

        graphics.drawString(font, txt, 0, 0, FastColor.ARGB32.color(Math.round(color.getAlpha() * 255), Math.round(color.getRed() * 255), Math.round(color.getGreen() * 255), Math.round(color.getBlue() * 255)), false);

        pose.popPose();
    }

    private void drawText(GuiGraphics graphics, float x, float y, float scale, Component[] txt, ColorF colorF) {
        Font font = Minecraft.getInstance().font;
        float height = font.lineHeight * txt.length + 2 * (txt.length - 1);

        int alpha = Math.round(colorF.getAlpha() * 255);
        if (alpha > 3) { // Minecraft text rendering bugs out at low alpha levels
            int red = Math.round(colorF.getRed() * 255);
            int green = Math.round(colorF.getGreen() * 255);
            int blue = Math.round(colorF.getBlue() * 255);
            int color = FastColor.ARGB32.color(alpha, red, green, blue);

            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(x, y + scale * (height / 2 - 1), 0);
            pose.scale(scale, -scale, 1.0f);

            for (Component line : txt) {
                float width = font.width(line);
                pose.pushPose();
                pose.translate(-width / 2 + 0.5f, 0.0f, 0.0f);
                graphics.drawString(font, line, 0, 0, color, false);
                pose.popPose();
                pose.translate(0.0f, font.lineHeight + 2, 0.0f);
            }
            pose.popPose();
        }
    }

    public final void drawText(GuiGraphics graphics, float x, float y, float scale, int maxWidth, int maxLines, ColorF color, Component txt) {
        TextRenderingRules rules = new TextRenderingRules().setMaxWidth(maxWidth).setMaxLines(maxLines);
        drawText(graphics, x, y, scale, rules.splitIntoLines(Minecraft.getInstance().font, txt), color);
    }

    public final void drawText(GuiGraphics graphics, float x, float y, float scale, ColorF color, Component... lines) {
        drawText(graphics, x, y, scale, lines, color);
    }

    public final void drawRectangle(GuiGraphics graphics, float x, float y, float width, float height, ColorF color, float angle) {
        drawRectangle(graphics, RenderType.gui(), x, y, width, height, color, angle);
    }

    public final void drawRectangle(GuiGraphics graphics, RenderType type, float x, float y, float width, float height, ColorF color, float angle) {
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

        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(type);
        vertexconsumer.vertex(matrix4f, pMinX, pMinY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinX, pMaxY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxX, pMaxY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxX, pMinY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        graphics.flush();

        pose.popPose();
    }

    public final void drawHollowRectangle(GuiGraphics graphics, float x, float y, float width, float height, float thickness, ColorF color, float angle) {
        drawHollowRectangle(graphics, RenderType.gui(), x, y, width, height, thickness, color, angle);
    }

    public final void drawHollowRectangle(GuiGraphics graphics, RenderType type, float x, float y, float width, float height, float thickness, ColorF color,  float angle) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        pose.mulPose(Axis.ZP.rotation(angle));
        Matrix4f matrix4f = pose.last().pose();

        float pMinOuterX = -width / 2;
        float pMaxOuterX = width / 2;
        float pMinOuterY = -height / 2;
        float pMaxOuterY = height / 2;

        if (pMinOuterX < pMaxOuterX) {
            float i = pMinOuterX;
            pMinOuterX = pMaxOuterX;
            pMaxOuterX = i;
        }

        if (pMinOuterY < pMaxOuterY) {
            float j = pMinOuterY;
            pMinOuterY = pMaxOuterY;
            pMaxOuterY = j;
        }

        float pMinInnerX = pMinOuterX + thickness;
        float pMaxInnerX = pMaxOuterX - thickness;
        float pMinInnerY = pMinOuterY + thickness;
        float pMaxInnerY = pMaxOuterY - thickness;

        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(type);

        // left
        vertexconsumer.vertex(matrix4f, pMinOuterX, pMinOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinOuterX, pMaxOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinInnerX, pMaxInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinInnerX, pMinInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        // top
        vertexconsumer.vertex(matrix4f, pMinOuterX, pMaxOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxOuterX, pMaxOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxInnerX, pMaxInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinInnerX, pMaxInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        // right
        vertexconsumer.vertex(matrix4f, pMaxOuterX, pMaxOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxOuterX, pMinOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxInnerX, pMinInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxInnerX, pMaxInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        // bottom
        vertexconsumer.vertex(matrix4f, pMaxOuterX, pMinOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinOuterX, pMinOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinInnerX, pMinInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxInnerX, pMinInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        graphics.flush();

        pose.popPose();
    }

    public final void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle, int u, int v, int uWidth, int vHeight) {
        drawTexture(graphics, texture, x, y, width, height, angle, u, v, uWidth, vHeight, new ColorF(1.0f));
    }

    public final void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle) {
        drawTexture(graphics, texture, x, y, width, height, angle, new ColorF(1.0f));
    }

    public final void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle, int u, int v, int uWidth, int vHeight, ColorF color) {
        float minU = (float) u / 256;
        float minV = (float) v / 256;
        float maxU = (float) (u + uWidth) / 256;
        float maxV = (float) (v + vHeight) / 256;
        drawTexture(graphics, texture, x, y, width, height, angle, minU, maxU, minV, maxV, color);
    }

    public final void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle, ColorF color) {
        drawTexture(graphics, texture, x, y, width, height, angle, 0.0f, 1.0f, 0.0f, 1.0f, color);
    }

    private void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle, float minU, float maxU, float minV, float maxV, ColorF color) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
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

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix4f, pMinX, pMinY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(maxU, minV).endVertex();
        bufferbuilder.vertex(matrix4f, pMinX, pMaxY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(maxU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, pMaxX, pMaxY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(minU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, pMaxX, pMinY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(minU, minV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

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
        if (isGameOver()) return false;
        KeyBinding binding = keyBindings.get(key);
        if (binding != null) {
            if (binding.pressAction != null) binding.pressAction.run();
            binding.pressed = true;
            return true;
        }
        return false;
    }

    public final boolean releaseKey(int key) {
        if (isGameOver()) return false;
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
