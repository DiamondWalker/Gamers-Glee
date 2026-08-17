package gameblock.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber
public class GameCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    public static Capability<GameCapability> CAPABILITY_GAME = CapabilityManager.get(new CapabilityToken<>() {});;
    private final LazyOptional<GameCapability> instance;

    private GameCapabilityProvider(Player player) {
        instance = LazyOptional.of(() -> new GameCapability(player));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY_GAME.orEmpty(cap, instance.cast());
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(new ResourceLocation("game_capability"), new GameCapabilityProvider(player));
        }
    }

    @SubscribeEvent
    public static void copyCapabilityToNewPlayer(PlayerEvent.Clone event) {
        GameCapability ogCap = event.getOriginal().getCapability(GameCapabilityProvider.CAPABILITY_GAME).orElse(null);
        GameCapability newCap = event.getEntity().getCapability(GameCapabilityProvider.CAPABILITY_GAME).orElse(null);
        if (ogCap != null && newCap != null && ogCap.getCosmetic() != null) {
            newCap.setCosmetic(ogCap.getCosmetic().type);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        instance.orElseThrow(NullPointerException::new).writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.orElseThrow(NullPointerException::new).readFromNBT(nbt);
    }
}
