package gameblock.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class HostedData {
    public static final String DISCORD_URL;

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
    }
}
