package gameblock.registry;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.core.tools.BasicCommandLineArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = GameblockMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GameblockCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(buildCosmeticCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCosmeticCommand() {
        return Commands.literal("cosmetic")
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("clear");
                            for (GameblockCosmetics.CosmeticType cosmeticType : GameblockCosmetics.getAllTypes())
                                builder.suggest(cosmeticType.id);
                            return builder.buildFuture();
                        })
                        .executes(commandContext -> {
                                    ServerPlayer sender = commandContext.getSource().getPlayer();
                                    String arg = StringArgumentType.getString(commandContext, "type");

                                    GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME).orElse(null);
                                    if (cap != null) {
                                        if (arg.matches("clear")) cap.setCosmetic(null);
                                        GameblockCosmetics.CosmeticType cosmetic = GameblockCosmetics.getTypeFromID(arg);
                                        if (cosmetic == null) return 0;
                                        cap.setCosmetic(cosmetic);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    return 0;
                                }
                        )
                );
    }
}
