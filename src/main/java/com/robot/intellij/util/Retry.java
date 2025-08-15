package com.robot.intellij.util;

public class Retry {
    public static void run(int attempts, Runnable action, Runnable onFailOnce) {
        RuntimeException last = null;
        for (int i = 0; i < attempts; i++) {
            try {
                action.run();
                return;
            } catch (RuntimeException | Error ex) {
                last = new RuntimeException(ex);
                if (onFailOnce != null) onFailOnce.run();
            }
        }
        if (last != null) throw last;
    }
}
