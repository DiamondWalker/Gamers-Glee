package gameblock.game;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.packet.GameUpdatePacket;
import gameblock.registry.GameblockItems;
import gameblock.registry.GameblockPackets;
import gameblock.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.Collection;
import java.util.UUID;

public abstract class GameInstance <T extends NetworkManager<?>> {
    public final Game<?> gameType;
    private final boolean clientSide;

    public static final int MAX_X = 100;
    public static final int MAX_Y = 75;
    public static final int MIN_X = -MAX_X;
    public static final int MIN_Y = -MAX_Y;

    private long gameTime = 0;
    private GameState gameState = GameState.ACTIVE;

    public GamePrompt<?> prompt = null;

    public final PlayerManager players;
    public final GameControls controls = new GameControls();
    public final RenderManager renderer = new RenderManager();
    public final GameblockSoundManager soundManager;
    public final T networkManager;

    public GameInstance(boolean clientSide, Game<?> gameType) {
        this.clientSide = clientSide;
        soundManager = clientSide ? new GameblockSoundManager() : null;
        this.players = new PlayerManager(this);
        this.gameType = gameType;
        this.networkManager = getNetworkManager();
    }

    public boolean isClientSide() {
        return clientSide;
    }

    protected int getMaxPlayers() {
        return 1;
    }

    protected void onPlayerJoined(UUID player) {

    }

    protected void onPlayerLeft(UUID player) {

    }

    protected boolean canJoin() {
        return false;
    }

    public String getGameCode() {
        return null;
    }

    public GameInstance<?> findGameWithGameCode(String code) {
        for (ServerPlayer player : players.getHostPlayer().getServer().getPlayerList().getPlayers()) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                GameInstance<?> joinGame = cap.getGame();
                String joinGameCode = joinGame.getGameCode();
                if (joinGameCode != null && code != null && joinGameCode.matches(code)) {
                    return joinGame;
                }
            }
        }

        return null;
    }

    protected abstract T getNetworkManager();

    protected final void setGameState(GameState state) {
        gameState = state;
        if (!isClientSide()) {
            networkManager.sendGameState(state);
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
            gameblockItem = players.getHostPlayer().getItemInHand(hand);
            if (gameblockItem.is(GameblockItems.GAMEBLOCK.get())) {
                break;
            } else {
                gameblockItem = null;
            }
        }

        if (gameblockItem != null) {
            String playerName = players.getHostPlayer().getGameProfile().getName();
            String gameName = gameType.getId().toString();
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
            gameblockItem = players.getHostPlayer().getItemInHand(hand);
            if (gameblockItem.is(GameblockItems.GAMEBLOCK.get())) {
                break;
            } else {
                gameblockItem = null;
            }
        }

        if (gameblockItem != null) {
            String playerName = players.getHostPlayer().getGameProfile().getName();
            String gameName = gameType.getId().toString();
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

    public void initialize() {

    }

    public final void restart() { // TODO: this whole method needs to be redone
        if (!isClientSide()) {
            Player mainPlayer = players.getHostPlayer();
            GameCapability cap = mainPlayer.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                cap.startGame(gameType);
            }
        } else {
            networkManager.sendGameRestart();
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

    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {}

    public boolean isGameOver() {
        return gameState != GameState.ACTIVE;
    }

    public final void baseTick() {
        if (prompt != null && prompt.shouldClose()) prompt = null;

        tick();
        gameTime++;
    }

    protected abstract void tick();

    public abstract void render(float partialTicks);

    public Music getMusic() {
        return null;
    }

    public final void playSound(SoundEvent event, float pitch, float volume) {
        if (soundManager != null) {
            soundManager.play(event, pitch, volume);
        }
    }

    public final void playSound(SoundEvent event) {
        playSound(event, 1.0f, 1.0f);
    }
}
