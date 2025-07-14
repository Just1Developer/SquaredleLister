package net.justonedev;

import java.util.Base64;
import java.util.List;

@SuppressWarnings("all")
public final class WordlyLinkGenerator {

    private static final String WORDLY_LINK_TEMPLATE = "https://wordly.org?challenge=%s";
    private static List<String> wordList;

    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 5;

    private WordlyLinkGenerator() {
    }

    public static void main(String[] args) {
        wordList = Main.getRegularWordList().stream().filter(w -> w.length() >= MIN_LENGTH && w.length() <= MAX_LENGTH).toList();
        var link = generateRandomChallenge();
        System.out.printf("Link: %s%n%s%n%s%n".formatted(link, link, link.replace("https://", "")));
    }

    private static String generateRandomChallenge() {
        return generateLink(getRandomWord());
    }

    private static String getRandomWord() {
        return wordList.get((int) (Math.random() * wordList.size()));
    }

    private static String generateLink(String word) {
        return String.format(WORDLY_LINK_TEMPLATE, Base64.getEncoder().encodeToString(word.getBytes()));
    }
}
