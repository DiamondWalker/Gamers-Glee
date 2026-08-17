package gameblock.util;

import gameblock.GameblockMod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public class HostedData {
    public static final String DISCORD_URL;
    public static final HashMap<UUID, String> JOIN_MESSAGES = new HashMap<>();

    static {
        String discordLink;
        try {
            URL link = new URL("https://raw.githubusercontent.com/DiamondWalker/Gamer-s-Glee-Data/refs/heads/main/discord.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(link.openStream()));
            discordLink = reader.readLine();
        } catch (Exception e) {
            discordLink = null;
        }

        DISCORD_URL = discordLink;

        try {
            URL link = new URL("https://raw.githubusercontent.com/DiamondWalker/super_scary.jar/refs/heads/master/supporters.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(link.openStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] components = line.split("\\|");
                if (components.length != 2) throw new RuntimeException("Invalid format!");
                UUID uuid = UUID.fromString(components[0]);
                String msg = components[1];
                JOIN_MESSAGES.put(uuid, msg);
            }
        } catch (Exception e) {
            JOIN_MESSAGES.clear();
            GameblockMod.LOGGER.warn("Could not fetch player join messages from GitHub: ", e);

        }
    }
}
