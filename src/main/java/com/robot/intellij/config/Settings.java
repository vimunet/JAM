package com.robot.intellij.config;

import com.robot.intellij.util.OS;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
    private final Properties p = new Properties();

    private Settings() {}

    public static Settings load() {
        Settings s = new Settings();
        try (InputStream in = Settings.class.getResourceAsStream("/app.properties")) {
            if (in != null) s.p.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load app.properties", e);
        }

        // Env overrides
        String token = System.getenv("GITHUB_TOKEN");
        if (token != null && !token.isBlank()) s.p.setProperty("github.token", token);

        return s;
    }

    public String getRepo() { return p.getProperty("repo", "").trim(); }
    public String getGithubToken() { return p.getProperty("github.token", "").trim(); }

    public OS getOS() {
        String explicit = p.getProperty("os", "").trim();
        if (!explicit.isBlank()) {
            try { return OS.valueOf(explicit.toUpperCase()); }
            catch (Exception ignored) {}
        }
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("win")) return OS.WINDOWS;
        if (osName.contains("mac")) return OS.MAC;
        return OS.LINUX;
    }

    public int minTypeDelay() { return Integer.parseInt(p.getProperty("typing.minDelayMs","30")); }
    public int maxTypeDelay() { return Integer.parseInt(p.getProperty("typing.maxDelayMs","180")); }
    public int errEveryNWords() { return Integer.parseInt(p.getProperty("typing.errorEveryNWords","45")); }
    public int errMaxBackspaces() { return Integer.parseInt(p.getProperty("typing.errorMaxBackspaces","3")); }
    public double burstProb() { return Double.parseDouble(p.getProperty("typing.burstProbability","0.20")); }
    public int burstWordsMin() { return Integer.parseInt(p.getProperty("typing.burstWordsMin","3")); }
    public int burstWordsMax() { return Integer.parseInt(p.getProperty("typing.burstWordsMax","8")); }

    public boolean pasteEnabled() { return Boolean.parseBoolean(p.getProperty("paste.enable","true")); }
    public int pasteMaxChunk() { return Integer.parseInt(p.getProperty("paste.maxChunk","140")); }
    public int pastePauseMin() { return Integer.parseInt(p.getProperty("paste.pauseBetweenChunksMsMin","200")); }
    public int pastePauseMax() { return Integer.parseInt(p.getProperty("paste.pauseBetweenChunksMsMax","600")); }

    public boolean wiggleEnabled() { return Boolean.parseBoolean(p.getProperty("mouse.wiggle.enable","true")); }
    public int wiggleIntervalMin() { return Integer.parseInt(p.getProperty("mouse.wiggle.intervalMsMin","2000")); }
    public int wiggleIntervalMax() { return Integer.parseInt(p.getProperty("mouse.wiggle.intervalMsMax","7000")); }
    public int wiggleDelta() { return Integer.parseInt(p.getProperty("mouse.wiggle.deltaPx","5")); }

    public int betweenFilesMin() { return Integer.parseInt(p.getProperty("between.files.sleepMsMin","1000")); }
    public int betweenFilesMax() { return Integer.parseInt(p.getProperty("between.files.sleepMsMax","3200")); }

    public int retryMaxAttempts() { return Integer.parseInt(p.getProperty("retry.maxAttempts","4")); }

    public boolean focusAbortOnChange() { return Boolean.parseBoolean(p.getProperty("focus.abortOnChange","true")); }
    public int focusCheckIntervalMs() { return Integer.parseInt(p.getProperty("focus.checkIntervalMs","700")); }
}
