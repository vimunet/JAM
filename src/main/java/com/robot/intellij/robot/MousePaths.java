package com.robot.intellij.robot;

import com.robot.intellij.config.Settings;

import java.awt.*;
import java.util.Random;

/**
 * Lightweight thread that performs tiny randomized mouse moves ("wiggles") so an activity tracker sees mouse motion.
 */
public class MousePaths implements Runnable {
    private final Robot r;
    private final Settings s;
    private volatile boolean running = true;
    private final Random rnd = new Random();

    public MousePaths(Robot r, Settings s) { this.r = r; this.s = s; }

    @Override
    public void run() {
        while (running && s.wiggleEnabled()) {
            try {
                Thread.sleep(rand(s.wiggleIntervalMin(), s.wiggleIntervalMax()));
                Point p = MouseInfo.getPointerInfo().getLocation();
                int dx = rnd.nextInt(2*s.wiggleDelta()+1) - s.wiggleDelta();
                int dy = rnd.nextInt(2*s.wiggleDelta()+1) - s.wiggleDelta();
                r.mouseMove(p.x + dx, p.y + dy);
            } catch (InterruptedException ignored) {}
        }
    }

    public void stop() { running = false; }

    private int rand(int a, int b) { return a + rnd.nextInt(Math.max(1, b - a + 1)); }
}
