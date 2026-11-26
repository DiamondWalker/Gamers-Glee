package gameblock.game.games.os;

import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.Game;
import gameblock.game.GameInstance;
import gameblock.item.CartridgeItem;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockMusic;
import gameblock.registry.GameblockSounds;
import gameblock.util.ColorF;
import gameblock.util.Direction1D;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.*;

public class GameblockOS extends GameInstance<OSNetworkManager> {
    private static final int LOGO_DURATION = 200;
    private static final int LOGO_FADE_OUT_TIME = 20;
    private static final int MENU_FADE_IN_TIME = 80;
    private static final int ICON_FADE_IN_DELAY = 20;
    private static final int ICON_FADE_IN_TIME = 80;

    ArrayList<OSIcon> gameIcons = new ArrayList<>();

    private GameblockLogoRenderer logoRenderer;
    private GameblockBackgroundRenderer bgRenderer;

    public GameblockOS(boolean clientSide) {
        super(clientSide, GameblockGames.GAMEBLOCK_OS.get());

        if (isClientSide()) {
            logoRenderer = new GameblockLogoRenderer(this);
            bgRenderer = new GameblockBackgroundRenderer(this);
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        if (!isClientSide()) {
            ArrayList<Game<?>> availableGames = new ArrayList<>();

            Inventory playerInventory = players.getHostPlayer().getInventory();
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                ItemStack stack = playerInventory.getItem(i);
                if (stack.getItem() instanceof CartridgeItem<?> cartridge) {
                    Game<?> game = cartridge.getGame();
                    if (!availableGames.contains(game)) {
                        availableGames.add(game);
                    }
                }
            }

            populateIconsFromGamesList(availableGames);
            networkManager.sendGamesList(availableGames);
        }
    }

    void populateIconsFromGamesList(List<Game<?>> games) {
        gameIcons.clear();

        for (Game<?> game : games) {
            addIcon(
                    () -> selectGameAndSentToClient(game),
                    game.getIconLocation(),
                    game.getName()
            );
        }

        addIcon(
                networkManager::sendMultiplayerPrompt,
                ResourceLocation.fromNamespaceAndPath(GameblockMod.MODID, "textures/gui/logo/multiplayer.png"),
                Component.translatable("icon.gameblock.multiplayer")
        );
    }

    private void addIcon(Runnable action, ResourceLocation texture, Component name) {
        int index = gameIcons.size();

        int x = (index % 4) * 40 - 60;
        int y = 45 - (index / 4) * 30;

        gameIcons.add(
                new OSIcon(
                        this,
                        action,
                        texture,
                        name,
                        new Vec2(x, y)
                )
        );
    }

    @Override
    protected void tick() {
        if (isClientSide()) {
            if (getGameTime() == 85) {
                playSound(GameblockSounds.GAMEBLOCK_LOGON.get());
            }
        }

        if (bgRenderer != null) bgRenderer.tick();

        if (isMenuInteractable() && gameIcons != null) {
            Vec2 mousePosition = controls.getMouseCoordinates();
            for (OSIcon icon : gameIcons) {
                icon.tick(mousePosition);
            }
        }
    }

    @Override
    protected OSNetworkManager getNetworkManager() {
        return new OSNetworkManager(this);
    }

    @Override
    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {
        if (buttonPressed == Direction1D.LEFT && isMenuInteractable() && gameIcons != null) {
            for (int i = 0; i < gameIcons.size(); i++) {
                if (gameIcons.get(i).isOverIcon(clickCoordinates)) {
                    networkManager.sendSelectGame(i);
                    return;
                }
            }
        }
    }

    @Override
    public void render(float partialTicks) {
        if (getGameTime() <= LOGO_DURATION) {
            logoRenderer.render(partialTicks);
        } else if (menuLoaded()) {
            bgRenderer.render(partialTicks);

            float iconTransparency = (partialTicks + getGameTime() - LOGO_DURATION - MENU_FADE_IN_TIME - ICON_FADE_IN_DELAY) / ICON_FADE_IN_TIME;
            iconTransparency = Mth.clamp(iconTransparency, 0.0f, 1.0f);

            if (!gameIcons.isEmpty()) {
                for (OSIcon icon : gameIcons) icon.render(partialTicks, iconTransparency);
            } else {
                renderer.drawRectangle(0, 0, 200, 200, new ColorF(0, 0, 0, 0.5f), 0);
                renderer.drawText(0, 0, 1.0f, new ColorF(1.0f), Component.translatable("gui.gameblock.os.no_cartridges_1"), Component.translatable("gui.gameblock.os.no_cartridges_2"));
            }
        } else { // loading screen
            for (int i = 0; i < 8; i++) {
                float angle = Mth.HALF_PI - (Mth.TWO_PI / 8) * i;
                int currentlyLitRect = (int) ((getGameTime() / 3) % 8);
                renderer.drawRectangle(Mth.cos(angle) * 12, Mth.sin(angle) * 12, 5.0f, 4.0f, new ColorF(i == currentlyLitRect ? 1.0f : 0.3f), angle);
            }
        }

        // the fade between the logo screen and the menu screen
        float fade = (partialTicks + getGameTime()) - LOGO_DURATION;
        if (fade < 0) { // logo fade out
            fade = -fade / LOGO_FADE_OUT_TIME;
        } else { // menu fade in
            fade = fade / MENU_FADE_IN_TIME;
        }
        if (fade < 1.0f) {
            renderer.drawRectangle(0, 0, 200, 200, new ColorF(0, 0, 0, 1.0f - fade), 0);
        }
    }

    protected void selectGameAndSentToClient(Game<?> game) {
        Player mainPlayer = players.getHostPlayer();

        Inventory playerInventory = mainPlayer.getInventory();
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (stack != null && stack.getItem() instanceof CartridgeItem<?> cartridge) {
                if (cartridge.getGame() == game) {
                    GameCapability cap = mainPlayer.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                    if (cap != null) {
                        cap.startGame(game);
                    }
                }
            }
        }
    }

    private boolean menuLoaded() {
        return getGameTime() > LOGO_DURATION && gameIcons != null;
    }

    private boolean isMenuInteractable() {
        return getGameTime() > LOGO_DURATION + MENU_FADE_IN_TIME + ICON_FADE_IN_DELAY + ICON_FADE_IN_TIME;
    }

    @Override
    public Music getMusic() {
        return menuLoaded() ? GameblockMusic.OS : null;
    }
}
