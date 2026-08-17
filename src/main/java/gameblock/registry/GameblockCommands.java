package gameblock.registry;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.util.HostedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
                                    if (HostedData.JOIN_MESSAGES.containsKey(sender.getUUID())) {
                                        String arg = StringArgumentType.getString(commandContext, "type");

                                        GameCapability cap = sender.getCapability(GameCapabilityProvider.CAPABILITY_GAME).orElse(null);
                                        if (cap != null) {
                                            if (arg.matches("clear")) {
                                                cap.setCosmetic(null);
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            GameblockCosmetics.CosmeticType cosmetic = GameblockCosmetics.getTypeFromID(arg);
                                            if (cosmetic == null) {
                                                commandContext.getSource().sendFailure(Component.literal("Not a valid cosmetic type"));
                                                return 0;
                                            }
                                            cap.setCosmetic(cosmetic);

                                            return Command.SINGLE_SUCCESS;
                                        } else {
                                            commandContext.getSource().sendFailure(Component.literal("Could not access cosmetic capability"));
                                        }
                                        return 0;
                                    } else {
                                        commandContext.getSource().sendFailure(Component.literal("This command is reserved for supporters"));
                                        return 0;
                                    }
                                }
                        )
                );
    }
}
