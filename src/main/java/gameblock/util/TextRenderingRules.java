package gameblock.util;

import net.minecraft.client.gui.Font;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Stack;

public class TextRenderingRules {
    int maxWidth = Integer.MAX_VALUE;
    int maxLines = Integer.MAX_VALUE;
    Direction1D alignment = Direction1D.CENTER;

    public TextRenderingRules setMaxWidth() {
        return setMaxWidth(Integer.MAX_VALUE);
    }
    public TextRenderingRules setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public TextRenderingRules setMaxLines() {
        return setMaxLines(Integer.MAX_VALUE);
    }

    public TextRenderingRules setMaxLines(int lines) {
        this.maxLines = lines;
        return this;
    }

    public String[] splitIntoLines(Font font, String text) {
        if (font.width(text) <= maxWidth) return new String[]{text};

        String[] lines = new String[maxLines];

        StringBuilder lineBuilder = new StringBuilder();
        for (String word : text.split(" ")) {
            if (!lineBuilder.isEmpty()) lineBuilder.append(' ');
            lineBuilder.append(word);
        }
        lines[0] = lineBuilder.toString();
        for (int i = 1; i < lines.length; i++) lines[i] = "";

        boolean keepGoing = true;
        while (keepGoing) {
            keepGoing = false;
            for (int i = 0; i < lines.length - 1; i++) {
                String currentLine = lines[i];
                String nextLine = lines[i + 1];

                int splitIndex = currentLine.lastIndexOf(" ");
                if (splitIndex > 0) {
                    String currentLineAfter = currentLine.substring(0, splitIndex);
                    String nextLineAfter = nextLine.isEmpty() ?
                            currentLine.substring(splitIndex + 1) + nextLine :
                            currentLine.substring(splitIndex + 1) + ' ' + nextLine;

                    while (Math.abs(font.width(nextLineAfter) - font.width(currentLineAfter)) < Math.abs(font.width(nextLine) - font.width(currentLine))) {
                        currentLine = currentLineAfter;
                        nextLine = nextLineAfter;
                        keepGoing = true;

                        splitIndex = currentLine.lastIndexOf(" ");
                        if (splitIndex > 0) {
                            currentLineAfter = currentLine.substring(0, splitIndex);
                            nextLineAfter = currentLine.substring(splitIndex + 1) + ' ' + nextLine;
                        } else {
                            break;
                        }
                    }
                }

                lines[i] = currentLine;
                lines[i + 1] = nextLine;
            }
        }

        ArrayList<String> finalLines = new ArrayList<>();
        for (String line : lines) {
            if (line != null && !line.isEmpty()) finalLines.add(line);
        }
        return finalLines.toArray(new String[0]);
    }
}
