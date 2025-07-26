package gameblock.capability;

import gameblock.game.Game;
import gameblock.gui.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegisterCapability
public class GameCapability {
    private Game game = null;

    public boolean isPlaying() {
        return game != null;
    }

    public void setGame(Game game, boolean clientSide) {
        this.game = game;
        if (clientSide) {
            if (game == null) {
                if (Minecraft.getInstance().screen instanceof GameScreen) Minecraft.getInstance().screen.onClose();
            } else {
                Minecraft.getInstance().setScreen(new GameScreen(game));
            }
        }
    }

    public Game getGame() {
        return game;
    }
}
