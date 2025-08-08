package net.justonedev.patterngen;

import java.util.ArrayList;
import java.util.List;

public class ColorPatternLine {
    private final List<LetterColor> colors;

    ColorPatternLine(List<LetterColor> colors) {
        this.colors = new ArrayList<>(colors);
    }

    public LetterColor getLetterColor(int letterIndex) {
        return colors.get(letterIndex);
    }
}
