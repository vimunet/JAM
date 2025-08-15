package com.robot.intellij;

import com.robot.intellij.config.Settings;
import com.robot.intellij.github.GitHubFetcher;
import com.robot.intellij.model.RepoFile;
import com.robot.intellij.robot.IntelliJOperator;
import com.robot.intellij.util.OS;

import java.awt.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        Settings settings = Settings.load();
        String repoSpec = settings.getRepo();

        System.out.println("Repo: " + repoSpec);
        System.out.println("Fetching repository structure from GitHub...");
        List<RepoFile> files = GitHubFetcher.fetchRepoTree(repoSpec, settings.getGithubToken());

        Robot robot = new Robot();
        robot.setAutoDelay(40);

        IntelliJOperator op = new IntelliJOperator(robot, settings);

        System.out.println("\n== Preparation ==\n");
        System.out.println("Please open IntelliJ IDEA and focus the Project tool window (or place caret where you'd like files created).");
        System.out.println("You have 8 seconds to switch to IntelliJ...");
        Thread.sleep(8000);

        // anchor focus
        op.focusProjectToolWindow();
        op.ensureProjectTreeFocused();

        // create directories first
        op.createDirectories(files);

        // create files and type contents
        op.createFilesAndTypeContent(files);

        System.out.println("\nAll done. Review the project in IntelliJ.");
    }
}
