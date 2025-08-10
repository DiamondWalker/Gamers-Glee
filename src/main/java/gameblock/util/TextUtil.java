package gameblock.util;

public class TextUtil {
    public static String getTimeString(long ticks, boolean includeHours, boolean requireExtraDigitForTop) {
        StringBuilder result = new StringBuilder();

        long seconds = ticks / 20;
        int minutes = (int)(seconds / 60);

        seconds -= (minutes * 60);

        if (includeHours) {
            int hours = minutes / 60;
            minutes -= (hours * 60);
            result.append(formatWithUnits(hours, requireExtraDigitForTop ? 2 : 1) + ':');
        }

        result.append(formatWithUnits(minutes, requireExtraDigitForTop && !includeHours ? 2 : 1) + ':');

        result.append(formatWithUnits(seconds, 2));

        return result.toString();
    }

    public static String formatWithUnits(long value, int minUnits) {
        StringBuilder result = new StringBuilder(String.valueOf(value));
        while (result.length() < minUnits) result.insert(0, '0');
        return result.toString();
    }
}
