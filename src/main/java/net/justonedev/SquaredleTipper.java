package net.justonedev;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Queue;

public class SquaredleTipper {
    private final Robot robot;
    private final Queue<SquaredleSolver.Word> words;

    public SquaredleTipper(Queue<SquaredleSolver.Word> words) {
        Robot localRobot;
        this.words = words;
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Graphics Environment is headless, cannot access low level input controls. Typing will be disabled.");
            robot = null;
            return;
        }
        try {
            localRobot = new Robot();
        } catch (AWTException ignored) {
            localRobot = null;
        }
        robot = localRobot;
    }

    public void typeAll(int startDelayMS, int typeDelayMS) {
        if (robot == null) return;
        sleep(startDelayMS);
        while (!words.isEmpty()) {
            var word = words.poll();
            assert word != null;
            typeWord(word.word());
            pressEnter();
            sleep(typeDelayMS);
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            throw new RuntimeException(ignored);
        }
    }

    public void typeWord(String word) {
        if (robot == null) return;
        System.out.println("Typing word " + word);
        for (char c : word.toCharArray()) {
            typeLetter(c);
        }
    }

    private void typeLetter(char c) {
        pressKey(getKeyFromLetter(c));
    }

    private void pressEnter() {
        pressKey(KeyEvent.VK_ENTER);
    }

    private void pressKey(int c) {
        robot.keyPress(c);
        robot.keyRelease(c);
    }

    private int getKeyFromLetter(char c) {
        return KeyEvent.VK_A + Character.toLowerCase(c) - 'a';
    }
}
