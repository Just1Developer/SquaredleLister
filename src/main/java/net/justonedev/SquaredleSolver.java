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
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public final class SquaredleSolver {
    private final char[][] squaredle;
    private int minWordLength = 4;
    private int maxWordLength = 8;
    private final String bonusOfTheDay;
    @SuppressWarnings("all")    // todo later
    private final boolean typeKnownBonusWords;
    private final boolean useAdvancedWordList;
    private Coordinate[][] coordinateObjCache;
    private Map<Coordinate, Set<Coordinate>> coordinateNeighborCache;

    private int maxWordLengthBruteforce;

    private final Set<String> wordList;

    public SquaredleSolver(Set<String> wordListRef, char[][] squaredleRef, int minWordLength, int maxWordLength, String bonusOfTheDay, boolean typeKnownBonusWords, boolean useAdvancedWordList) {
        this.wordList = wordListRef;
        this.squaredle = squaredleRef;
        this.minWordLength = minWordLength;
        this.bonusOfTheDay = bonusOfTheDay;
        this.typeKnownBonusWords = typeKnownBonusWords;
        this.useAdvancedWordList = useAdvancedWordList;
        maxWordLengthBruteforce = Math.max(7, 18 - squaredle.length); //Math.max(7, 12 - squaredle.length / 2);
        this.maxWordLength = Math.min(maxWordLength, maxWordLengthBruteforce);
    }

    public Queue<Word> solveWordleToFile() {
        coordinateNeighborCache = new HashMap<>();
        coordinateObjCache = new Coordinate[squaredle.length][squaredle[0].length];
        for (int y = 0; y < squaredle.length; y++) {
            for (int x = 0; x < squaredle[y].length; x++) {
                Coordinate c = new Coordinate(this, x, y);
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
        Queue<Word> wordList = new LinkedList<>();

        int currentLength = 0;
        printf("%n-------------------------------------%n%n");
        printf("[%s] All Squaredle Words. Map:%n", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
        printMap(squaredle);
        printf("Using %s word list.%n", useAdvancedWordList ? "advanced" : "regular");
        printf("%nFound %d words.%n", words.size());
        while (!words.isEmpty()) {
            var found = words.poll();
            if (!found.word.equals(bonusOfTheDay)) {
                wordList.offer(found);
            }
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

        return wordList;
    }
    
    private void print(String msg) {
        printf(msg + "%n");
    }

    private final List<String> fileContents = new ArrayList<>();
    private void printf(String msg, Object... args) {
        String string = msg.formatted(args);
        fileContents.add(string);
        //System.out.print(string);
    }

    private Optional<Word> findWord(String _word) {
        Queue<List<Coordinate>> paths = new LinkedList<>();
        char[] word = _word.toCharArray();
        // Find all start letters:
        for (int y = 0; y < squaredle.length; y++) {
            for (int x = 0; x < squaredle[y].length; x++) {
                if (charAt(coord(x, y)) == word[0]) {
                    var list = new ArrayList<Coordinate>();
                    list.add(coord(x, y));
                    paths.add(list);
                }
            }
        }
        return findWordRec(paths, word, 1);
    }

    private Optional<Word> findWordRec(Queue<List<Coordinate>> paths, char[] word, int currentLetterIndex) {
        if (paths.isEmpty()) return Optional.empty();
        if (currentLetterIndex >= word.length) {
            return Optional.of(new Word(String.valueOf(word), paths.poll()));
        }
        for (int i = 0; i < paths.size(); i++) {
            var path = paths.poll();
            var last = path.getLast();
            var neighbors = last.getAllAdjacent(path);
            for (var neighbor : neighbors) {
                if (charAt(neighbor) == word[currentLetterIndex]) {
                    var newPath = new ArrayList<>(path);
                    newPath.add(neighbor);
                    paths.offer(newPath);
                }
            }
        }
        return findWordRec(paths, word, currentLetterIndex + 1);
    }

    private PriorityQueue<Word> findAllWords() {
        PriorityQueue<Word> allWords = new PriorityQueue<>(Comparator.comparing(word -> ((Word) word).word.length()).thenComparing(word -> ((Word) word).word));
        for (int length = minWordLength; length <= maxWordLength; length++) {
            print("Begin finding words of length: " + length);
            System.out.println("Begin finding words of length: " + length);
            Set<Word> words = new HashSet<>();
            for (int y = 0; y < squaredle.length; y++) {
                System.out.println("[Word Length " + length + "] Starting with row " + (y + 1));
                for (int x = 0; x < squaredle[y].length; x++) {
                    words.addAll(findWords(x, y, length));
                }
            }
            words.stream().distinct().forEach(allWords::offer);
            print("Finished finding words of length: " + length);
        }
        System.out.println("Starting finding all longer words...");
        for (String word : wordList) {
            if (word.length() > maxWordLength) {
                var wordOpt = findWord(word);
                wordOpt.ifPresent(allWords::add);
            }
        }
        return allWords;
    }

    private void printPath(List<Coordinate> coords) {
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

    private void printMap(char[][] map) {
        for (char[] chars : map) {
            for (char aChar : chars) {
                printf(" " + aChar);
            }
            printf("%n%n");
        }
    }

    private Set<Word> findWords(int startX, int startY, int length) {
        Coordinate coord = coord(startX, startY);
        StringBuilder word = new StringBuilder();
        word.append(charAt(coord));
        List<Coordinate> visited = new LinkedList<>();
        visited.add(coord);
        Set<Word> words = new HashSet<>();
        findNextWords(words, length, visited, coord, word);
        return words;
    }

    private char charAt(Coordinate coordinate) {
        return Character.toLowerCase(squaredle[coordinate.y][coordinate.x]);
    }

    private Coordinate coord(int x, int y) {
        return coordinateObjCache[y][x];
    }

    private void findNextWords(Set<Word> words, int targetLength, List<Coordinate> visited, Coordinate current, StringBuilder currentWord) {
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

    private record Coordinate(SquaredleSolver solver, int x, int y) {
        private Set<Coordinate> getAllAdjacent() {
            Set<Coordinate> adjacent = new HashSet<>();
            if (this.y > 0) adjacent.add(solver.coordinateObjCache[this.y - 1][this.x]);    // Top Middle
            if (this.y < solver.coordinateObjCache.length - 1) adjacent.add(solver.coordinateObjCache[this.y + 1][this.x]);    // Bottom Middle
            if (this.x > 0) {
                adjacent.add(solver.coordinateObjCache[this.y][this.x - 1]);    // Middle Left
                if (this.y > 0) adjacent.add(solver.coordinateObjCache[this.y - 1][this.x - 1]);    // Top Left
                if (this.y < solver.coordinateObjCache.length - 1) adjacent.add(solver.coordinateObjCache[this.y + 1][this.x - 1]);    // Bottom Left
            }
            if (this.x < solver.coordinateObjCache[this.y].length - 1) {
                adjacent.add(solver.coordinateObjCache[this.y][this.x + 1]);             // Middle Right
                if (this.y > 0) adjacent.add(solver.coordinateObjCache[this.y - 1][this.x + 1]);    // Top Right
                if (this.y < solver.coordinateObjCache.length - 1) adjacent.add(solver.coordinateObjCache[this.y + 1][this.x + 1]);    // Bottom Right
            }
            return adjacent;
        }

        private Set<Coordinate> getAllAdjacent(Collection<Coordinate> visited) {
            return solver.coordinateNeighborCache.get(this)
                    .stream()
                    .filter(coordinate -> coordinate != null && !visited.contains(coordinate))
                    .collect(Collectors.toSet());
        }
    }

    public record Word(String word, List<Coordinate> path) {
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