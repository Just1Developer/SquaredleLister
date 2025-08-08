package net.justonedev.patterngen;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public final class RegexBuilder {

    private static final String GENERAL_PATTERN = "^%s$";

    private final String word;
    private final int wordLength;
    private final Map<Character, Integer> letterOccurences;

    RegexBuilder(String word, Map<Character, Integer> letterOccurences) {
        this.word = word;
        this.wordLength = word.length();
        this.letterOccurences = new HashMap<>(letterOccurences);
    }

    public void buildForLine(ColorPatternLine patternLine) {
        StringBuilder pattern = new StringBuilder();
        pattern.append(buildWordlengthLookaheadPattern(wordLength));



        String finalPattern = GENERAL_PATTERN.formatted(pattern);
    }

    private LetterColor evaluateLetterColor(char letter, int index) {
        if (word.charAt(index) == letter) {
            return LetterColor.GREEN;
        } else if (letterOccurences.containsKey(letter)) {
            return LetterColor.YELLOW;
        }
        return LetterColor.GRAY;
    }

    private static String buildWordlengthLookaheadPattern(int length) {
        return "(?=^.*{%d}$)".formatted(length);
    }

    // I dont think i need this:
    private static String buildCharacterContainsLookaheadPattern(char character, int amount) {
        return "(?=^(?:.*%c){%d}$)".formatted(character, amount);
    }
}
