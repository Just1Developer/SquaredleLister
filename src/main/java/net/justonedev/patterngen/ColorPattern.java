package net.justonedev.patterngen;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ColorPattern {
    private final List<ColorPatternLine> pattern;

    private ColorPattern(List<ColorPatternLine> pattern) {
        // Pointer copy is okay because of private constructor
        // Parse method makes new arrays for each parse
        this.pattern = pattern;
    }

    public LetterColor getLetterColor(int rowIndex, int letterIndex) {
        return pattern.get(rowIndex).getLetterColor(letterIndex);
    }

    public static ColorPattern parse(String[] pattern) {
        // . or N: Gray (n is for nothing)
        // X or Y: Yellow
        // # or G: Green

        // best use of streams ever
        return new ColorPattern(Arrays.stream(pattern)
                .map(s -> { List<Character> a = new LinkedList<>();
                    for (char c : s.toCharArray()) {
                        a.add(c);
                    } return a;})
                .map((li) -> li.stream()
                        .map(c -> switch (c) {
                            case '#', 'G' -> LetterColor.GREEN;
                            case 'X', 'Y' -> LetterColor.YELLOW;
                            default -> LetterColor.GRAY;
                        }).toList())
                .map(ColorPatternLine::new)
                .toList());
    }
}
