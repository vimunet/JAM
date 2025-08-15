package com.robot.intellij.robot;

import com.robot.intellij.util.OS;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Centralized key mappings and low-level key helpers.
 * Adjust for custom keymaps. These are conservative defaults that work on standard IntelliJ setups.
 */
public class KeyMaps {

    public static void press(Robot r, int... keys) {
        for (int k : keys) r.keyPress(k);
        for (int i = keys.length - 1; i >= 0; i--) r.keyRelease(keys[i]);
        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
    }

    public static void newFile(OS os, Robot r) {
        switch (os) {
            case WINDOWS, LINUX -> press(r, KeyEvent.VK_ALT, KeyEvent.VK_INSERT);
            case MAC -> press(r, KeyEvent.VK_META, KeyEvent.VK_N);
        }
        sleep(200);
    }

    public static void newDirectory(OS os, Robot r) {
        switch (os) {
            case WINDOWS, LINUX -> press(r, KeyEvent.VK_ALT, KeyEvent.VK_INSERT);
            case MAC -> press(r, KeyEvent.VK_META, KeyEvent.VK_N);
        }
        sleep(200);
    }

    public static void focusProject(OS os, Robot r) {
        switch (os) {
            case WINDOWS, LINUX -> press(r, KeyEvent.VK_ALT, KeyEvent.VK_1);
            case MAC -> press(r, KeyEvent.VK_META, KeyEvent.VK_1);
        }
    }

    public static void paste(OS os, Robot r) {
        if (os == OS.MAC) press(r, KeyEvent.VK_META, KeyEvent.VK_V);
        else press(r, KeyEvent.VK_CONTROL, KeyEvent.VK_V);
    }

    public static void copy(OS os, Robot r) {
        if (os == OS.MAC) press(r, KeyEvent.VK_META, KeyEvent.VK_C);
        else press(r, KeyEvent.VK_CONTROL, KeyEvent.VK_C);
    }

    public static void typeChar(Robot r, char c) {
        int key = KeyEvent.getExtendedKeyCodeForChar(c);
        if (key == KeyEvent.CHAR_UNDEFINED) return;
        boolean upper = Character.isUpperCase(c);
        boolean needsShift = upper || "~!@#$%^&*()_+{}|:\"<>?".indexOf(c) >= 0;
        if (needsShift) r.keyPress(KeyEvent.VK_SHIFT);
        r.keyPress(key);
        r.keyRelease(key);
        if (needsShift) r.keyRelease(KeyEvent.VK_SHIFT);
        sleep(8);
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
