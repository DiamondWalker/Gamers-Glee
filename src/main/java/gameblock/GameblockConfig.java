package gameblock;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;

public class GameblockConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> REPEAT_UPDATE_NOTIFICATION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PROMOTE_DISCORD_SERVER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("Gameblock Config");
        REPEAT_UPDATE_NOTIFICATION = builder.comment("If true, players will be reminded to update the mod every 3 hours of playtime").define("Repeat update notification", true);
        PROMOTE_DISCORD_SERVER = builder.comment("If true, a link to the Gamer's Glee Discord server will be posted in chat on world join").define("Send Discord link", true);
        builder.pop();

        SPEC = builder.build();
    }
}
