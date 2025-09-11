package gameblock.game.os;

import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import gameblock.game.os.packets.MultiplayerPromptPacket;
import gameblock.game.os.packets.SelectGamePacket;
import gameblock.item.CartridgeItem;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockMusic;
import gameblock.registry.GameblockPackets;
import gameblock.registry.GameblockSounds;
import gameblock.util.rendering.ColorF;
import gameblock.util.physics.Direction1D;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.*;

public class GameblockOS extends GameInstance<GameblockOS> {
    private static final int LOGO_FADE_OUT_TIME = 20;
    private static final int ICON_FADE_IN_DELAY = 20;
    private static final int ICON_FADE_IN_TIME = 80;

    protected HashSet<OSIcon> gameIcons = null;

    private GameblockLogoRenderer logoRenderer;
    private GameblockBackgroundRenderer bgRenderer;
    private boolean showStartupScreen;

    public GameblockOS(Player player) {
        this(player, true);
    }

    public GameblockOS(Player player, boolean showStartupScreen) {
        super(player, GameblockGames.GAMEBLOCK_OS);
        this.showStartupScreen = showStartupScreen;
        if (isClientSide()) {
            logoRenderer = new GameblockLogoRenderer(this);
            bgRenderer = new GameblockBackgroundRenderer(this);
        }
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeBoolean(showStartupScreen);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        super.readFromBuffer(buffer);
        showStartupScreen = buffer.readBoolean();
    }

    public OSIcon[] getIcons() {
        return gameIcons.toArray(new OSIcon[0]);
    }

    @Override
    protected void tick() {
        if (gameIcons == null) {
            gameIcons = new HashSet<>();
            LinkedHashSet<GameblockGames.Game<?>> gamesFound = new LinkedHashSet<>();
            int index = 0;

            Inventory playerInventory = getHostPlayer().getInventory();
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                ItemStack stack = playerInventory.getItem(i);
                if (stack != null && stack.getItem() instanceof CartridgeItem<?> cartridge) {
                    if (!gamesFound.contains(cartridge.gameType)) {
                        gamesFound.add(cartridge.gameType);
                        gameIcons.add(new OSIcon(
                                this,
                                () -> selectGameAndSentToClient(cartridge.gameType),
                                cartridge.gameType.logo,
                                Component.translatable("icon.gameblock." + cartridge.gameType.gameID),
                                index++
                        ));
                    }
                }
            }

            gameIcons.add(new OSIcon(
                    this,
                    () -> GameblockPackets.sendToPlayer((ServerPlayer) getHostPlayer(), new MultiplayerPromptPacket()),
                    new ResourceLocation(GameblockMod.MODID, "textures/gui/logo/multiplayer.png"),
                    Component.translatable("icon.gameblock.multiplayer"),
                    index++
            ));
        }

        if (isClientSide()) {
            if (showStartupScreen && getGameTime() == 85) {
                playSound(GameblockSounds.GAMEBLOCK_LOGON.get());
            }
        }

        if (bgRenderer != null) bgRenderer.tick();

        if (isMenuInteractable() && gameIcons != null) {
            Vec2 mousePosition = getMouseCoordinates();
            for (OSIcon icon : gameIcons) {
                icon.tick(mousePosition);
            }
        }
    }

    @Override
    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {
        if (buttonPressed == Direction1D.LEFT && isMenuInteractable() && gameIcons != null) {
            for (OSIcon icon : gameIcons) {
                if (icon.isOverIcon(clickCoordinates)) {
                    GameblockPackets.sendToServer(new SelectGamePacket(icon.index));
                    return;
                }
            }
        }
    }

    private int getLogoDuration() {
        return showStartupScreen ? 200 : 0;
    }

    private int getMenuFadeInTime() {
        return showStartupScreen ? 80 : 20;
    }

    @Override
    public void render() {
        if (getGameTime() <= getLogoDuration()) {
            logoRenderer.render();
        } else if (menuLoaded()) {
            bgRenderer.render();

            float iconTransparency = (getPartialTicks() + getGameTime() - getLogoDuration() - getMenuFadeInTime() - ICON_FADE_IN_DELAY) / ICON_FADE_IN_TIME;
            iconTransparency = Mth.clamp(iconTransparency, 0.0f, 1.0f);

            if (!gameIcons.isEmpty()) {
                for (OSIcon icon : gameIcons) icon.render(iconTransparency);
            } else {
                drawRectangle(0, 0, 200, 200, new ColorF(0, 0, 0, 0.5f), 0);
                drawText(0, 0, 1.0f, new ColorF(1.0f), Component.translatable("gui.gameblock.os.no_cartridges_1"), Component.translatable("gui.gameblock.os.no_cartridges_2"));
            }
        } else { // loading screen
            for (int i = 0; i < 8; i++) {
                float angle = Mth.HALF_PI - (Mth.TWO_PI / 8) * i;
                int currentlyLitRect = (int) ((getGameTime() / 3) % 8);
                drawRectangle(Mth.cos(angle) * 12, Mth.sin(angle) * 12, 5.0f, 4.0f, new ColorF(i == currentlyLitRect ? 1.0f : 0.3f), angle);
            }
        }

        // the fade between the logo screen and the menu screen
        float fade = (getPartialTicks() + getGameTime()) - getLogoDuration();
        if (fade < 0) { // logo fade out
            fade = -fade / LOGO_FADE_OUT_TIME;
        } else { // menu fade in
            fade = fade / getMenuFadeInTime();
        }
        System.out.println(fade);
        if (fade < 1.0f) {
            drawRectangle(0, 0, 200, 200, new ColorF(0, 0, 0, 1.0f - fade), 0);
        }
    }

    protected void selectGameAndSentToClient(GameblockGames.Game<?> game) {
        Inventory playerInventory = getHostPlayer().getInventory();
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (stack != null && stack.getItem() instanceof CartridgeItem<?> cartridge) {
                if (cartridge.gameType == game) {
                    GameCapability cap = getHostPlayer().getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                    if (cap != null) {
                        cap.setGame(game.createInstance(getHostPlayer()));
                    }
                }
            }
        }
    }

    private boolean menuLoaded() {
        return getGameTime() > getLogoDuration() && gameIcons != null;
    }

    private boolean isMenuInteractable() {
        return getGameTime() > getLogoDuration() + getMenuFadeInTime() + ICON_FADE_IN_DELAY + ICON_FADE_IN_TIME;
    }

    @Override
    public Music getMusic() {
        return menuLoaded() ? GameblockMusic.OS : null;
    }
}
