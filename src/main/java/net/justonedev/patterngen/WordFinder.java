package net.justonedev.patterngen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordFinder {

    private final String word;
    private final ColorPattern targetPattern;

    private final Map<Character, Integer> letterOccurrences;
    private final RegexBuilder regexBuilder;

    private final List<String> result;

    public WordFinder(String word, ColorPattern targetPattern) {
        this.word = word;
        this.targetPattern = targetPattern;
        this.result = new ArrayList<>();
        letterOccurrences = new HashMap<>();
        buildLetterOccurrences();
        regexBuilder = new RegexBuilder(word, letterOccurrences);
    }

    public void solve() {

    }

    public String getSolvedWord(int index) {
        return result.get(index);
    }

    public List<String> getSolvedWords() {
        return new ArrayList<>(result);
    }

    private void buildLetterOccurrences() {
        letterOccurrences.clear();
        for (char c : word.toCharArray()) {
            letterOccurrences.put(c, letterOccurrences.getOrDefault(c, 0) + 1);
        }
    }
}
