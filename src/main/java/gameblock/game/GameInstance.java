package gameblock.game;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import gameblock.GameblockConfig;
import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockItems;
import gameblock.registry.GameblockPackets;
import gameblock.util.*;
import gameblock.util.rendering.ColorF;
import gameblock.util.physics.Direction1D;
import gameblock.util.rendering.TextRenderingRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class GameInstance<T extends GameInstance<?, ?>, PlayerDataType extends GamePlayer.GamePlayerData> {
    public final GameblockGames.Game<T> gameType;
    private final HashMap<Integer, KeyBinding> keyBindings = new HashMap<>();
    private Vec2 mouseCoordinates = new Vec2(Float.NaN, Float.NaN);

    private final GamePlayer<PlayerDataType>[] players;
    private final Supplier<PlayerDataType> playerDataSupplier;

    private final LinkedList<TickTimer> gameTimers = new LinkedList<>();

    private final boolean clientSide;

    public static final float MAX_X = 100.0f;
    public static final float MIN_X = -100.0f;
    public static final float MAX_Y = 75.0f;
    public static final float MIN_Y = -75.0f;

    public static final float SCREEN_WIDTH = MAX_X - MIN_X;
    public static final float SCREEN_HEIGHT = MAX_Y - MIN_Y;

    private long gameTime = 0;
    private GameState gameState = GameState.PLAYING;

    public GamePrompt prompt = null;

    public final GameblockSoundManager soundManager;

    public GameInstance(Player player, GameblockGames.Game<T> gameType, Supplier<PlayerDataType> playerData) {
        clientSide = player.level().isClientSide();
        soundManager = clientSide ? new GameblockSoundManager() : null;
        this.playerDataSupplier = playerData;
        this.players = new GamePlayer[clientSide ? 1 : getMaxPlayers()];
        this.players[0] = new GamePlayer<>(player, 0, playerDataSupplier.get());
        this.gameType = gameType;
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public int getMaxPlayers() {
        return 1;
    }

    public final GamePlayer<PlayerDataType> getHostPlayer() {
        return players[0];
    }

    public final GamePlayer<PlayerDataType> getPlayer(int i) {
        return players[i];
    }

    public final GamePlayer<PlayerDataType> getGamePlayer(Player player) {
        for (GamePlayer<PlayerDataType> playerDataTypeGamePlayer : players)
            if (playerDataTypeGamePlayer.playerEntity() == player) return playerDataTypeGamePlayer;
        throw new IllegalArgumentException("Player not found!");
    }

    public void sendToAllPlayers(UpdateGamePacket<?> packet, GamePlayer<PlayerDataType> exception) {
        if (clientSide) throw new IllegalStateException("Cannot send a packet to the clients if already on a client!");
        for (GamePlayer<PlayerDataType> player : players) {
            if (player != null && player != exception) GameblockPackets.sendToPlayer((ServerPlayer) player.playerEntity(), packet);
        }
    }

    /**
     * Writes all the extra game data that has to be sent to a new player
     */
    public void writeToBuffer(FriendlyByteBuf buffer) {

    }

    /**
     * Writes all the extra game data that was sent to a new player
     */
    public void readFromBuffer(FriendlyByteBuf buffer) {

    }

    public void forEachPlayer(Consumer<GamePlayer<PlayerDataType>> action) {
        for (GamePlayer<PlayerDataType> player : players) {
            if (player != null) action.accept(player);
        }
    }

    public final boolean isPlaying(ServerPlayer player) {
        for (int i = 0; i < players.length; i++) if (players[i] != null && players[i].playerEntity() == player) return true;
        return false;
    }

    public boolean canJoin() {
        return getPlayerCount() < getMaxPlayers();
    }

    public final void addPlayer(ServerPlayer player) {
        for (int i = 1; i < players.length; i++) {
            if (players[i] == null) {
                players[i] = new GamePlayer<>(player, i, playerDataSupplier.get());
                onPlayerJoined(players[i]);
                return;
            }
        }
        throw new IllegalStateException("Attempted to add new players when there is no more room for players!");
    }

    public final void removePlayer(ServerPlayer player) {
        if (player == getHostPlayer().playerEntity()) {
            for (int i = 1; i < players.length; i++) {
                if (players[i] != null) removePlayer((ServerPlayer) players[i].playerEntity());
            }

            save();
            return;
        }

        for (int i = 1; i < players.length; i++) {
            if (players[i] != null && players[i].playerEntity() == player) {
                GamePlayer<PlayerDataType> gamePlayer = players[i];
                players[i] = null;
                onPlayerDisconnected(gamePlayer);
                gamePlayer.invalidate();
                return;
            }
        }
    }

    protected void onPlayerJoined(GamePlayer<PlayerDataType> player) {

    }

    protected void onPlayerDisconnected(GamePlayer<PlayerDataType> player) {

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

    public final void setGameState(GameState state) {
        GameState oldState = gameState;
        gameState = state;
        if (!isClientSide()) {
            sendToAllPlayers(new GameStatePacket(state), null);
        }
        onGameStateChange(oldState, gameState);
        /*if (state == GameState.GAME_OVER_WIN) {
            onGameWin();
        } else if (state == GameState.GAME_OVER_LOSS) {
            onGameLoss();
        }*/
    }

    public final void save() {
        ItemStack gameblockItem = null;
        for (InteractionHand hand : InteractionHand.values()) {
            gameblockItem = getHostPlayer().playerEntity().getItemInHand(hand);
            if (gameblockItem.is(GameblockItems.GAMEBLOCK.get())) {
                break;
            } else {
                gameblockItem = null;
            }
        }

        if (gameblockItem != null) {
            String playerName = getHostPlayer().playerEntity().getGameProfile().getName();
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
            gameblockItem = getHostPlayer().playerEntity().getItemInHand(hand);
            if (gameblockItem.is(GameblockItems.GAMEBLOCK.get())) {
                break;
            } else {
                gameblockItem = null;
            }
        }

        if (gameblockItem != null) {
            String playerName = getHostPlayer().playerEntity().getGameProfile().getName();
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
            T newGame = gameType.createInstance(getHostPlayer().playerEntity());
            for (GamePlayer<PlayerDataType> player : players) {
                GameCapability cap = player.playerEntity().getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (cap != null) {
                    cap.setGame(newGame);
                    if (player != getHostPlayer()) addPlayer((ServerPlayer) player.playerEntity());
                }
            }
        } else {
            GameblockPackets.sendToServer(new GameRestartPacket());
        }
    }

    public final GameState getGameState() {
        return gameState;
    }

    protected void onGameStateChange(GameState oldState, GameState newState) {

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

    public boolean isGameOver() {
        return gameState != GameState.PLAYING;
    }

    public void addTickTimer(TickTimer timer) {
        if (gameTimers.contains(timer)) {
            GameblockMod.LOGGER.warn("Timer is already being ticked!");
        } else {
            gameTimers.add(timer);
        }
    }

    public final void baseTick(Player player) {
        if (prompt != null && prompt.shouldClose()) prompt = null;

        if (player == getHostPlayer().playerEntity()) {
            if (!isClientSide()) {
                // ensure players that, for example, left the game, are removed
                for (int i = 0; i < players.length; i++) {
                    if (players[i] != null) {
                        ServerPlayer serverPlayer = (ServerPlayer) players[i].playerEntity();
                        GameCapability cap = serverPlayer.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                        if (cap == null || cap.getGame() != this || serverPlayer.isDeadOrDying()) removePlayer(serverPlayer);
                    }
                }
            }

            // tick game timers
            ListIterator<TickTimer> iter = gameTimers.listIterator();
            while (iter.hasNext()) {
                TickTimer timer = iter.next();
                timer.tick();
                if (timer.getState() != TickTimer.TimerState.RUNNING) {
                    timer.executeScheduledAction();
                    iter.remove();
                }
            }

            tick();
            gameTime++;
        }

        if (!isClientSide()) {
            boolean stayOpen = true;
            if ((!player.getItemInHand(InteractionHand.MAIN_HAND).is(GameblockItems.GAMEBLOCK.get()) && !player.getItemInHand(InteractionHand.OFF_HAND).is(GameblockItems.GAMEBLOCK.get()))) {
                stayOpen = false;
            } else if (getHostPlayer() == null || !getHostPlayer().playerEntity().isAlive()) {
                stayOpen = false;
            } else {
                GameCapability hostCapability = getHostPlayer().playerEntity().getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (hostCapability == null || !hostCapability.isPlayingGame()) stayOpen = false;
            }

            if (!stayOpen) {
                GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (cap != null && cap.isPlayingGame()) {
                    cap.setGame(null);
                }
            }
        }
    }

    protected abstract void tick();

    private GuiGraphics graphics = null;
    private float partialTicks = 0.0f;

    public final void startFrame(GuiGraphics graphics, float partialTicks) {
        this.graphics = graphics;
        this.partialTicks = partialTicks;
    }

    public final void endFrame() {
        this.graphics = null;
        this.partialTicks = 0.0f;
    }

    public GuiGraphics getGraphicsInstance() {
        return graphics;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public abstract void render();

    public Music getMusic() {
        return null;
    }

    public final void playSound(SoundEvent event, float pitch, float volume) {
        if (soundManager != null) soundManager.play(event, pitch, volume);
    }

    public final void playSound(SoundEvent event) {
        playSound(event, 1.0f, 1.0f);
    }

    private void drawText(float x, float y, float scale, Component txt, ColorF color) {
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

    private void drawText(float x, float y, float scale, Component[] txt, ColorF colorF) {
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

    public final void drawText(Vec2 pos, float scale, int maxWidth, int maxLines, ColorF color, Component txt) {
        drawText(pos.x, pos.y, scale, maxWidth, maxLines, color, txt);
    }

    public final void drawText(float x, float y, float scale, int maxWidth, int maxLines, ColorF color, Component txt) {
        TextRenderingRules rules = new TextRenderingRules().setMaxWidth(maxWidth).setMaxLines(maxLines);
        drawText(x, y, scale, rules.splitIntoLines(Minecraft.getInstance().font, txt), color);
    }

    public final void drawText(Vec2 pos, float scale, ColorF color, Component... lines) {
        drawText(pos.x, pos.y, scale, color, lines);
    }

    public final void drawText(float x, float y, float scale, ColorF color, Component... lines) {
        drawText(x, y, scale, lines, color);
    }

    public final void drawRectangle(Vec2 pos, float width, float height, ColorF color, float angle) {
        drawRectangle(pos.x, pos.y, width, height, color, angle);
    }

    public final void drawRectangle(float x, float y, float width, float height, ColorF color, float angle) {
        drawRectangle(RenderType.gui(), x, y, width, height, color, angle);
    }

    public final void drawRectangle(RenderType type, Vec2 pos, float width, float height, ColorF color, float angle) {
        drawRectangle(type, pos.x, pos.y, width, height, color, angle);
    }

    public final void drawRectangle(RenderType type, float x, float y, float width, float height, ColorF color, float angle) {
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

    public final void drawHollowRectangle(Vec2 pos, float width, float height, float thickness, ColorF color, float angle) {
        drawHollowRectangle(pos.x, pos.y, width, height, thickness, color, angle);
    }

    public final void drawHollowRectangle(float x, float y, float width, float height, float thickness, ColorF color, float angle) {
        drawHollowRectangle(RenderType.gui(), x, y, width, height, thickness, color, angle);
    }

    public final void drawHollowRectangle(RenderType type, Vec2 pos, float width, float height, float thickness, ColorF color, float angle) {
        drawHollowRectangle(type, pos.x, pos.y, width, height, thickness, color, angle);
    }

    public final void drawHollowRectangle(RenderType type, float x, float y, float width, float height, float thickness, ColorF color,  float angle) {
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

    public final void drawArc(Vec2 pos, float innerRadius, float outerRadius, float startAngle, float endAngle, ColorF color) {
        drawArc(pos.x, pos.y, innerRadius, outerRadius, startAngle, endAngle, color);
    }

    public final void drawArc(float x, float y, float innerRadius, float outerRadius, float startAngle, float endAngle, ColorF color) {
        drawArc(RenderType.gui(), x, y, innerRadius, outerRadius, startAngle, endAngle, color);
    }

    public final void drawArc(RenderType type, Vec2 pos, float innerRadius, float outerRadius, float startAngle, float endAngle, ColorF color) {
        drawArc(type, pos.x, pos.y, innerRadius, outerRadius, startAngle, endAngle, color);
    }

    public final void drawArc(RenderType type, float x, float y, float innerRadius, float outerRadius, float startAngle, float endAngle, ColorF color) {
        if (endAngle < startAngle) {
            // swap the angles
            float temp = startAngle;
            startAngle = endAngle;
            endAngle = temp;
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        Matrix4f matrix4f = pose.last().pose();

        VertexConsumer consumer = graphics.bufferSource().getBuffer(type);
        int subdivisions = GameblockConfig.CIRCLE_RENDERING_SUBDIVISIONS.get();
        boolean breakLoop = false;
        for (int i = 0; i < subdivisions; i++) {
            float angle1 = startAngle + Mth.TWO_PI * i / subdivisions;
            float angle2 = startAngle + Mth.TWO_PI * (i + 1) / subdivisions;

            if (angle2 > endAngle) {
                angle2 = endAngle;
                breakLoop = true;
            }

            Vec2 vec1 = MathHelper.getUnitVectorFromAngle(angle1);
            Vec2 vec2 = MathHelper.getUnitVectorFromAngle(angle2);


            consumer.vertex(matrix4f, vec1.x * innerRadius, vec1.y * innerRadius, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            consumer.vertex(matrix4f, vec1.x * outerRadius, vec1.y * outerRadius, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            consumer.vertex(matrix4f, vec2.x * outerRadius, vec2.y * outerRadius, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            consumer.vertex(matrix4f, vec2.x * innerRadius, vec2.y * innerRadius, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

            if (breakLoop) break;
        }

        graphics.flush();
        pose.popPose();
    }

    public final void drawRing(Vec2 pos, float innerRadius, float outerRadius, ColorF color) {
        drawRing(pos.x, pos.y, innerRadius, outerRadius, color);
    }

    public final void drawRing(float x, float y, float innerRadius, float outerRadius, ColorF color) {
        drawRing(RenderType.gui(), x, y, innerRadius, outerRadius, color);
    }

    public final void drawRing(RenderType type, Vec2 pos, float innerRadius, float outerRadius, ColorF color) {
        drawRing(type, pos.x, pos.y, innerRadius, outerRadius, color);
    }

    public final void drawRing(RenderType type, float x, float y, float innerRadius, float outerRadius, ColorF color) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        Matrix4f matrix4f = pose.last().pose();

        VertexConsumer consumer = graphics.bufferSource().getBuffer(type);
        int subdivisions = GameblockConfig.CIRCLE_RENDERING_SUBDIVISIONS.get();
        for (int i = 0; i < subdivisions; i++) {
            Vec2 angle1 = MathHelper.getUnitVectorFromAngle(Mth.TWO_PI * i / subdivisions);
            Vec2 angle2 = MathHelper.getUnitVectorFromAngle(Mth.TWO_PI * (i + 1) / subdivisions);

            consumer.vertex(matrix4f, angle1.x * innerRadius, angle1.y * innerRadius, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            consumer.vertex(matrix4f, angle1.x * outerRadius, angle1.y * outerRadius, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            consumer.vertex(matrix4f, angle2.x * outerRadius, angle2.y * outerRadius, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            consumer.vertex(matrix4f, angle2.x * innerRadius, angle2.y * innerRadius, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }

        graphics.flush();
        pose.popPose();
    }

    public final void drawCircle(Vec2 pos, float radius, ColorF color) {
        drawCircle(pos.x, pos.y, radius, color);
    }

    public final void drawCircle(float x, float y, float radius, ColorF color) {
        drawCircle(RenderType.gui(), x, y, radius, color);
    }

    public final void drawCircle(RenderType type, Vec2 pos, float radius, ColorF color) {
        drawCircle(type, pos.x, pos.y, radius, color);
    }

    public final void drawCircle(RenderType type, float x, float y, float radius, ColorF color) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        Matrix4f matrix4f = pose.last().pose();

        VertexConsumer consumer = graphics.bufferSource().getBuffer(type);
        int subdivisions = GameblockConfig.CIRCLE_RENDERING_SUBDIVISIONS.get();
        for (int i = 0; i < subdivisions; i++) {
            Vec2 angle1 = MathHelper.getUnitVectorFromAngle(Mth.TWO_PI * i / subdivisions).scale(radius);
            Vec2 angle2 = MathHelper.getUnitVectorFromAngle(Mth.TWO_PI * (i + 1) / subdivisions).scale(radius);

            consumer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            consumer.vertex(matrix4f, angle1.x, angle1.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            consumer.vertex(matrix4f, angle2.x, angle2.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            consumer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }

        graphics.flush();
        pose.popPose();
    }

    public final void drawLine(float startX, float startY, float endX, float endY, float width, boolean roundedEnds, ColorF color) {
        drawLine(new Vec2(startX, startY), new Vec2(endX, endY), width, roundedEnds, color);
    }

    public final void drawLine(Vec2 startPoint, Vec2 endPoint, float width, boolean roundedEnds, ColorF color) {
        drawLine(RenderType.gui(), startPoint, endPoint, width, roundedEnds, color);
    }

    public final void drawLine(RenderType type, float startX, float startY, float endX, float endY, float width, boolean roundedEnds, ColorF color) {
        drawLine(type, new Vec2(startX, startY), new Vec2(endX, endY), width, roundedEnds, color);
    }

    public final void drawLine(RenderType type, Vec2 startPoint, Vec2 endPoint, float width, boolean roundedEnds, ColorF color) {
        PoseStack pose = graphics.pose();
        Matrix4f matrix = pose.last().pose();
        float halfWidth = width / 2;

        VertexConsumer consumer = graphics.bufferSource().getBuffer(type);

        Vec2 vector = new Vec2(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
        Vec2 perpendicularVector = new Vec2(vector.y, -vector.x).normalized().scale(halfWidth);

        // the rectangle part
        consumer.vertex(matrix, startPoint.x + perpendicularVector.x, startPoint.y + perpendicularVector.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        consumer.vertex(matrix, startPoint.x - perpendicularVector.x, startPoint.y - perpendicularVector.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        consumer.vertex(matrix, startPoint.x + vector.x - perpendicularVector.x, startPoint.y + vector.y - perpendicularVector.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        consumer.vertex(matrix, startPoint.x + vector.x + perpendicularVector.x, startPoint.y + vector.y + perpendicularVector.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        if (roundedEnds) {
            float endPointAngle = (float) Math.atan2(vector.y, vector.x);

            boolean breakLoop = false;
            int subdivisions = GameblockConfig.CIRCLE_RENDERING_SUBDIVISIONS.get();
            for (int i = 0; i < subdivisions; i++) {
                float angle1 = -Mth.HALF_PI + Mth.TWO_PI * i / subdivisions;
                float angle2 = -Mth.HALF_PI + Mth.TWO_PI * (i + 1) / subdivisions;

                if (angle2 > Mth.HALF_PI) {
                    angle2 = Mth.HALF_PI;
                    breakLoop = true;
                }

                Vec2 vec1 = MathHelper.getUnitVectorFromAngle(angle1 + endPointAngle);
                Vec2 vec2 = MathHelper.getUnitVectorFromAngle(angle2 + endPointAngle);

                consumer.vertex(matrix, endPoint.x, endPoint.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                consumer.vertex(matrix, endPoint.x + vec1.x * halfWidth, endPoint.y + vec1.y * halfWidth, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                consumer.vertex(matrix, endPoint.x + vec2.x * halfWidth, endPoint.y + vec2.y * halfWidth, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                consumer.vertex(matrix, endPoint.x, endPoint.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

                consumer.vertex(matrix, startPoint.x, startPoint.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                consumer.vertex(matrix, startPoint.x - vec1.x * halfWidth, startPoint.y - vec1.y * halfWidth, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                consumer.vertex(matrix, startPoint.x - vec2.x * halfWidth, startPoint.y - vec2.y * halfWidth, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                consumer.vertex(matrix, startPoint.x, startPoint.y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

                if (breakLoop) break;
            }
        }

        graphics.flush();
    }

    public final void drawTexture(ResourceLocation texture, Vec2 pos, float width, float height, float angle, int u, int v, int uWidth, int vHeight) {
        drawTexture(texture, pos.x, pos.y, width, height, angle, u, v, uWidth, vHeight);
    }

    public final void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle, int u, int v, int uWidth, int vHeight) {
        drawTexture(texture, x, y, width, height, angle, u, v, uWidth, vHeight, ColorF.WHITE);
    }

    public final void drawTexture(ResourceLocation texture, Vec2 pos, float width, float height, float angle) {
        drawTexture(texture, pos.x, pos.y, width, height, angle);
    }

    public final void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle) {
        drawTexture(texture, x, y, width, height, angle, ColorF.WHITE);
    }

    public final void drawTexture(ResourceLocation texture, Vec2 pos, float width, float height, float angle, int u, int v, int uWidth, int vHeight, ColorF color) {
        drawTexture(texture, pos.x, pos.y, width, height, angle, u, v, uWidth, vHeight, color);
    }

    public final void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle, int u, int v, int uWidth, int vHeight, ColorF color) {
        float minU = (float) u / 256;
        float minV = (float) v / 256;
        float maxU = (float) (u + uWidth) / 256;
        float maxV = (float) (v + vHeight) / 256;
        drawTexture(texture, x, y, width, height, angle, minU, maxU, minV, maxV, color);
    }

    public final void drawTexture(ResourceLocation texture, Vec2 pos, float width, float height, float angle, ColorF color) {
        drawTexture(texture, pos.x, pos.y, width, height, angle, color);
    }

    public final void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle, ColorF color) {
        drawTexture(texture, x, y, width, height, angle, 0.0f, 1.0f, 0.0f, 1.0f, color);
    }

    private void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle, float minU, float maxU, float minV, float maxV, ColorF color) {
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
