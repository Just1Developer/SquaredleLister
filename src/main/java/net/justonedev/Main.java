package net.justonedev;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Main {

    private static final int DEFAULT_MIN_WORD_LENGTH = 4;
    private static final int DEFAULT_MAX_WORD_LENGTH = 8;
    private static final boolean DEFAULT_SHOULD_TYPE = false;
    private static final int DEFAULT_START_DELAY = 3000;
    private static final int DEFAULT_TYPE_DELAY = 50;

    private static int minWordLength;
    private static int maxWordLength;
    private static boolean shouldType = DEFAULT_SHOULD_TYPE;
    private static int startDelay;
    private static int typeDelay;
    private static String bonusWordOfTheDay;
    private static boolean typeKnownBonusWords;
    private static boolean useAdvancedWordList;

    private static final Pattern parseableNumberRegex = Pattern.compile("\\d{1,5}");
    private static final String BOOLEAN_REGEX = "true|y(es)?";

    private static final String regularWordlistFile = "words/wordlist.txt";
    private static final String largeWordlistFile = "words/wordlist_large.txt";

    private Main() {}

    public static void main(String[] args) {
        readConfig();
        Set<String> wordList = new HashSet<>(readFile(useAdvancedWordList ? largeWordlistFile : regularWordlistFile));
        char[][] squaredle = readFile("squaredle.txt").stream().map(String::toCharArray).toArray(char[][]::new);
        SquaredleSolver solver = new SquaredleSolver(wordList, squaredle, minWordLength, maxWordLength, bonusWordOfTheDay, typeKnownBonusWords, useAdvancedWordList);
        System.out.println("Beginning...");
        var words = solver.solveWordleToFile();
        if (shouldType) {
            SquaredleTipper tipper = new SquaredleTipper(words);
            tipper.typeAll(startDelay, typeDelay);
        }
    }

    private static void readConfig() {
        List<String[]> cfg = readFile("config.txt").stream().map(s -> s.split("=")).toList();
        for (String[] setting : cfg) {
            if (setting.length != 2) continue;
            String key = setting[0].toLowerCase().trim();
            String value = setting[1].toLowerCase().trim();
            if (key.contains("word") && key.contains("length")) {
                if (key.contains("min")) {
                    if (parseableNumberRegex.matcher(value).matches()) {
                        minWordLength = Integer.parseInt(value);
                    } else {
                        minWordLength = DEFAULT_MIN_WORD_LENGTH;
                    }
                } else if (key.contains("max")) {
                    if (parseableNumberRegex.matcher(value).matches()) {
                        maxWordLength = Integer.parseInt(value);
                    } else {
                        maxWordLength = DEFAULT_MAX_WORD_LENGTH;
                    }
                }
            } else if (key.equals("type")) {
                shouldType = value.matches("true|y(es)?");
            } else if (key.contains("delay")) {
                if (key.contains("type") || key.contains("typing")) {
                    if (parseableNumberRegex.matcher(value).matches()) {
                        typeDelay = Integer.parseInt(value);
                    } else {
                        typeDelay = DEFAULT_TYPE_DELAY;
                    }
                } else if (key.contains("start") || key.contains("begin")) {
                    if (parseableNumberRegex.matcher(value).matches()) {
                        startDelay = Integer.parseInt(value);
                    } else {
                        startDelay = DEFAULT_START_DELAY;
                    }
                }
            } else if (key.contains("bonus")) {
                if (key.contains("day")) {
                    bonusWordOfTheDay = value;
                } else {
                    typeKnownBonusWords = value.matches(BOOLEAN_REGEX);
                }
            } else if (key.contains("use")) {
                if (key.contains("advanced") && (key.contains("words") || key.contains("list"))) {
                    useAdvancedWordList = value.matches(BOOLEAN_REGEX);
                }
            }
        }
    }

    private static List<String> readFile(String filename) {
        try {
            return Files.readAllLines(new File(filename).toPath());
        } catch (IOException e) {
            System.out.println("Failed to read wordlist from file " + filename + ": " + e.getMessage());
            return List.of();
        }
    }
}