package exp.nidoham.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BanglaWordDetector {

    // Pattern to detect any Bangla character in the string
    private static final Pattern BANGLA_PATTERN = Pattern.compile("[\\u0980-\\u09FF]");

    /**
     * Detects if the input string contains any Bangla script characters.
     * Returns true if there is at least one Bangla character.
     *
     * @param input The string to check.
     * @return true if it contains Bangla, false otherwise.
     */
    public static boolean containsBangla(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        Matcher matcher = BANGLA_PATTERN.matcher(input);
        return matcher.find();
    }
}