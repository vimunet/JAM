package com.robot.intellij.robot;

import com.robot.intellij.config.Settings;
import com.robot.intellij.model.RepoFile;
import com.robot.intellij.util.OS;
import com.robot.intellij.util.Retry;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * High-level operations that use Robot + Humanizer to create directories and files inside IntelliJ.
 * This class attempts to be resilient: retries and a simple recovery sequence (ESC, re-focus).
 */
public class IntelliJOperator {
    private final Robot r;
    private final Settings s;
    private final Humanizer human;
    private final OS os;
    private Thread mouseThread;

    public IntelliJOperator(Robot robot, Settings settings) {
        this.r = robot;
        this.s = settings;
        this.human = new Humanizer(settings);
        this.os = settings.getOS();
        startMouseThread();
    }

    public void focusProjectToolWindow() {
        KeyMaps.focusProject(os, r);
        nap(250);
        // give some TABs to land on tree
        KeyMaps.press(r, KeyEvent.VK_TAB);
        nap(90);
        KeyMaps.press(r, KeyEvent.VK_TAB);
        nap(150);
    }

    public void ensureProjectTreeFocused() {
        KeyMaps.press(r, KeyEvent.VK_HOME);
        nap(200);
    }

    public void createDirectories(List<RepoFile> all) {
        List<RepoFile> dirs = all.stream().filter(RepoFile::isDir).collect(Collectors.toList());
        for (RepoFile d : dirs) {
            createDirectoryPath(d.path());
            nap(rand(s.betweenFilesMin(), s.betweenFilesMax()));
        }
    }

    public void createFilesAndTypeContent(List<RepoFile> all) {
        List<RepoFile> files = all.stream().filter(f -> !f.isDir()).collect(Collectors.toList());
        for (RepoFile f : files) {
            Retry.run(s.retryMaxAttempts(), () -> {
                KeyMaps.newFile(os, r);
                nap(350);
                // type path into dialog (IDEA supports nested path creation)
                human.typeHumanText(r, f.path());
                nap(200);
                KeyMaps.press(r, KeyEvent.VK_ENTER);
                nap(600);

                // editor likely focused - type or paste content
                if (s.pasteEnabled() && f.content().length() > s.pasteMaxChunk()) {
                    human.pasteHumanChunks(r, s, f.content());
                } else {
                    human.typeHumanText(r, f.content());
                }

            }, this::recoverFromDialog);

            nap(rand(s.betweenFilesMin(), s.betweenFilesMax()));
        }
    }

    private void createDirectoryPath(String dirPath) {
        if (dirPath == null || dirPath.isBlank()) return;

        Retry.run(s.retryMaxAttempts(), () -> {
            KeyMaps.newDirectory(os, r);
            nap(300);
            human.typeHumanText(r, dirPath);
            nap(200);
            KeyMaps.press(r, KeyEvent.VK_ENTER);
            nap(350);
        }, this::recoverFromDialog);
    }

    private void recoverFromDialog() {
        // ESC, wait, refocus project, home
        KeyMaps.press(r, KeyEvent.VK_ESCAPE);
        nap(150);
        KeyMaps.press(r, KeyEvent.VK_ESCAPE);
        nap(200);
        focusProjectToolWindow();
        ensureProjectTreeFocused();
    }

    private void nap(int ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
    private int rand(int a, int b) { return a + new Random().nextInt(Math.max(1, b - a + 1)); }

    private void startMouseThread() {
        if (s.wiggleEnabled()) {
            MousePaths mp = new MousePaths(r, s);
            mouseThread = new Thread(mp, "mouse-wiggle");
            mouseThread.setDaemon(true);
            mouseThread.start();
        }
    }
}
