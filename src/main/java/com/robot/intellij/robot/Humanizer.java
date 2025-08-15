package com.robot.intellij.robot;

import com.robot.intellij.config.Settings;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * Responsible for 'human-like' typing and paste behavior:
 * - randomized per-character delays
 * - occasional bursts
 * - simulated typos/backspaces
 * - chunked clipboard pastes
 */
public class Humanizer {
    private final Settings s;
    private final Random rnd = new Random();

    public Humanizer(Settings settings) { this.s = settings; }

    public void typeHumanText(Robot r, String text) {
        if (text == null) return;
        String[] words = text.split("(\\s+)");
        int wordsSinceError = 0;

        for (int i = 0; i < words.length; i++) {
            // simulate occasional fast bursts
            if (rnd.nextDouble() < s.burstProb()) {
                int cnt = rand(s.burstWordsMin(), s.burstWordsMax());
                for (int j = 0; j < cnt && i < words.length; j++, i++) {
                    typeWord(r, words[i]);
                    typeChar(r, ' ');
                }
            }
            if (i >= words.length) break;

            typeWord(r, words[i]);
            typeChar(r, ' ');
            wordsSinceError++;

            if (s.errEveryNWords() > 0 && wordsSinceError >= s.errEveryNWords() && rnd.nextBoolean()) {
                // small typo then backspace
                char typo = (char)('a' + rnd.nextInt(26));
                typeChar(r, typo);
                nap(rand(s.minTypeDelay(), s.maxTypeDelay()));
                backspace(r, 1 + rnd.nextInt(Math.max(1, s.errMaxBackspaces())));
                wordsSinceError = 0;
            }
            nap(rand(s.minTypeDelay(), s.maxTypeDelay()));
        }
    }

    public void pasteHumanChunks(Robot r, Settings settings, String content) {
        if (content == null) return;
        int idx = 0;
        while (idx < content.length()) {
            int remaining = content.length() - idx;
            int chunk = Math.min(remaining, settings.pasteMaxChunk());
            String sub = content.substring(idx, idx + chunk);
            pushClipboard(sub);
            // perform paste
            KeyMaps.paste(settings.getOS(), r);
            idx += chunk;
            nap(rand(settings.pastePauseMin(), settings.pastePauseMax()));
        }
    }

    public void typeWord(Robot r, String w) {
        for (char c : w.toCharArray()) {
            typeChar(r, c);
            nap(rand(s.minTypeDelay(), s.maxTypeDelay()) / 2);
        }
    }

    public void typeChar(Robot r, char c) {
        KeyMaps.typeChar(r, c);
    }

    public void backspace(Robot r, int count) {
        for (int i = 0; i < count; i++) {
            KeyMaps.press(r, KeyEvent.VK_BACK_SPACE);
            nap(rand(s.minTypeDelay(), s.maxTypeDelay()));
        }
    }

    private void pushClipboard(String s) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
        } catch (Exception ignored) {}
    }

    private void nap(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private int rand(int a, int b) {
        return a + rnd.nextInt(Math.max(1, b - a + 1));
    }
}
