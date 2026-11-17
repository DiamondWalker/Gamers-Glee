package gameblock.game;

import gameblock.registry.GameblockCustomRegistries;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RenderItemInFrameEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class Game<T extends GameInstance> {
    private final Function<Player, T> constructor;
    private ResourceLocation id = null;
    private String translationKey = null;
    private ResourceLocation iconLocation = null;

    public Game(Function<Player, T> constructor) {
        this.constructor = constructor;
    }

    public T createInstance(Player player) {
        return constructor.apply(player);
    }

    private void createIdsIfNull() {
        if (id == null) {
            id = GameblockCustomRegistries.GAMES.get().getKey(this);
            translationKey = Util.makeDescriptionId("game", id);
            iconLocation = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/gui/logo/" + id.getPath() + ".png");
        }
    }

    public ResourceLocation getId() {
        createIdsIfNull();

        return id;
    }
    public Component getName() {
        createIdsIfNull();

        return Component.translatable(translationKey);
    }

    public ResourceLocation getIconLocation() {
        createIdsIfNull();

        return iconLocation;
    }
}
