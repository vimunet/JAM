package com.robot.intellij.model;

public record RepoFile(String path, String content, boolean isDir) {
    public String dirPath() {
        int i = path.lastIndexOf('/');
        return i > 0 ? path.substring(0, i) : "";
    }
    public String fileName() {
        int i = path.lastIndexOf('/');
        return i >= 0 ? path.substring(i+1) : path;
    }
}
