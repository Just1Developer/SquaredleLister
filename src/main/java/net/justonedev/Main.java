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
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class Main {

    private static final char[][] squaredle = {
            { 'E', 'U', 'T', 'A' },
            { 'B', 'H', 'S', 'B' },
            { 'R', 'O', 'A', 'P' },
            { 'E', 'W', 'O', 'M' },
    };
    private static final int MIN_WORD_LENGTH = 4;
    private static final int MAX_WORD_LENGTH = 8;
    private static Coordinate[][] coordinateObjCache;
    private static Map<Coordinate, Set<Coordinate>> coordinateNeighborCache;

    private static Set<String> wordList = new HashSet<>();

    private Main() {}

    public static void main(String[] args) {
        try {
            wordList = new HashSet<>(Files.readAllLines(new File("wordlist.txt").toPath()));
            printf("Registered %d valid words%n", wordList.size());
        } catch (IOException e) {
            wordList = new HashSet<>();
            System.out.println("Failed to read wordlist from file wordlist.txt: " + e.getMessage());
        }

        coordinateNeighborCache = new HashMap<Coordinate, Set<Coordinate>>();
        coordinateObjCache = new Coordinate[squaredle.length][squaredle[0].length];
        for (int y = 0; y < squaredle.length; y++) {
            for (int x = 0; x < squaredle[y].length; x++) {
                Coordinate c = new Coordinate(x, y);
                coordinateObjCache[y][x] = c;
            }
        }
        // Cache needs to be populated for this, so 2 loops:
        for (int y = 0; y < squaredle.length; y++) {
            for (int x = 0; x < squaredle[y].length; x++) {
                Coordinate c = coordinateObjCache[y][x];
                coordinateNeighborCache.put(c, c.getAllAdjacent());
            }
        }

        var words = findAllWords();
        int currentLength = 0;
        printf("%n-------------------------------------%n%n");
        printf("[%s] All Squaredle Words. Map:%n", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
        printMap(squaredle);
        printf("%nFound %d words%n.", words.size());
        while (!words.isEmpty()) {
            var found = words.poll();
            var word = found.word;
            var path = found.path;
            if (word.length() > currentLength) {
                currentLength = word.length();
                printf("%nWords of length: %d%n", currentLength);
            }
            print(word.toLowerCase());
            printPath(path);
        }

        try {
            File f = new File("./output.txt");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            fileContents.forEach(line -> {
                try {
                    Files.writeString(f.toPath(), line, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (IllegalStateException | IOException e) {
            System.out.println("Failed to print to file: " + e.getMessage());
        }
    }
    
    private static void print(String msg) {
        printf(msg + "%n");
    }

    private static final List<String> fileContents = new ArrayList<>();
    private static void printf(String msg, Object... args) {
        String string = msg.formatted(args);
        fileContents.add(string);
        //System.out.print(string);
    }

    private static PriorityQueue<Word> findAllWords() {
        PriorityQueue<Word> allWords = new PriorityQueue<>(Comparator.comparing(word -> ((Word) word).word.length()).thenComparing(word -> ((Word) word).word));
        for (int length = MIN_WORD_LENGTH; length <= MAX_WORD_LENGTH; length++) {
            print("Begin finding words of length: " + length);
            Set<Word> words = new HashSet<>();
            for (int y = 0; y < squaredle.length; y++) {
                for (int x = 0; x < squaredle[y].length; x++) {
                    words.addAll(findWords(x, y, length));
                }
            }
            words.stream().distinct().forEach(allWords::offer);
            print("Finished finding words of length: " + length);
        }
        return allWords;
    }

    private static void printPath(List<Coordinate> coords) {
        if (coords.isEmpty()) return;
        final char[][] map = new char[squaredle.length][squaredle[0].length];
        for (char[] row : map) {
            Arrays.fill(row, 'o');
        }
        int i = 1;
        for (Coordinate coord : coords) {
            map[coord.y][coord.x] = (char) (i > 9 ? 'a' - 10 + i++ : i++ + '0');
        }
        printMap(map);
    }

    private static void printMap(char[][] map) {
        for (char[] chars : map) {
            for (char aChar : chars) {
                printf(" " + aChar);
            }
            printf("%n%n");
        }
    }

    private static Set<Word> findWords(int startX, int startY, int length) {
        Coordinate coord = coord(startX, startY);
        StringBuilder word = new StringBuilder();
        word.append(charAt(coord));
        List<Coordinate> visited = new LinkedList<>();
        visited.add(coord);
        Set<Word> words = new HashSet<>();
        findNextWords(words, length, visited, coord, word);
        return words;
    }

    private static char charAt(Coordinate coordinate) {
        return Character.toLowerCase(squaredle[coordinate.y][coordinate.x]);
    }

    private static Coordinate coord(int x, int y) {
        return coordinateObjCache[y][x];
    }

    private static void findNextWords(Set<Word> words, int targetLength, List<Coordinate> visited, Coordinate current, StringBuilder currentWord) {
        if (currentWord.length() == targetLength) {
            String word = currentWord.toString();
            if (wordList.contains(word)) words.add(new Word(word, visited.stream().toList()));
            return;
        }
        for (Coordinate next : current.getAllAdjacent(visited)) {
            currentWord.append(charAt(next));
            visited.add(next);
            findNextWords(words, targetLength, visited, next, currentWord);
            visited.remove(next);
            currentWord.deleteCharAt(currentWord.length() - 1);
        }
    }

    private record Coordinate(int x, int y) {
        private Set<Coordinate> getAllAdjacent() {
            Set<Coordinate> adjacent = new HashSet<>();
            if (this.y > 0) adjacent.add(coordinateObjCache[this.y - 1][this.x]);    // Top Middle
            if (this.y < coordinateObjCache.length - 1) adjacent.add(coordinateObjCache[this.y + 1][this.x]);    // Bottom Middle
            if (this.x > 0) {
                adjacent.add(coordinateObjCache[this.y][this.x - 1]);    // Middle Left
                if (this.y > 0) adjacent.add(coordinateObjCache[this.y - 1][this.x - 1]);    // Top Left
                if (this.y < coordinateObjCache.length - 1) adjacent.add(coordinateObjCache[this.y + 1][this.x - 1]);    // Bottom Left
            }
            if (this.x < coordinateObjCache[this.y].length - 1) {
                adjacent.add(coordinateObjCache[this.y][this.x + 1]);             // Middle Right
                if (this.y > 0) adjacent.add(coordinateObjCache[this.y - 1][this.x + 1]);    // Top Right
                if (this.y < coordinateObjCache.length - 1) adjacent.add(coordinateObjCache[this.y + 1][this.x + 1]);    // Bottom Right
            }
            return adjacent;
        }

        private Set<Coordinate> getAllAdjacent(Collection<Coordinate> visited) {
            return coordinateNeighborCache.get(this)
                    .stream()
                    .filter(coordinate -> coordinate != null && !visited.contains(coordinate))
                    .collect(Collectors.toSet());
        }
    }

    private record Word(String word, List<Coordinate> path) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Word word1 = (Word) o;
            return Objects.equals(word, word1.word);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(word);
        }
    }
}