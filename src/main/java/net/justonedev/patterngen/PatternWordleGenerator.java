package net.justonedev.patterngen;

import net.justonedev.Main;

import java.util.List;

@SuppressWarnings("all")
public final class PatternWordleGenerator {

    private static List<String> wordList;

    private static final int WORD_LENGTH = 5;

    private PatternWordleGenerator() {
    }

    public static void main(String[] args) {
        wordList = Main.getRegularWordList().stream().filter(w -> w.length() == WORD_LENGTH).toList();
    }

    private static void readConfig() {
        // todo later
    }
}
