package com.robot.intellij.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robot.intellij.model.RepoFile;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fetches repository tree using GitHub git/trees API and raw.githubusercontent.com for contents.
 * Lightweight and synchronous; for very large repos consider streaming or limiting paths.
 */
public class GitHubFetcher {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<RepoFile> fetchRepoTree(String repoSpec, String token) throws IOException {
        String owner, repo;
        if (repoSpec == null || repoSpec.isBlank()) throw new IllegalArgumentException("repo is required in app.properties");
        if (repoSpec.startsWith("http")) {
            String[] parts = repoSpec.split("/");
            owner = parts[3];
            repo  = parts[4].replaceAll("\.git$", "");
        } else {
            String[] parts = repoSpec.split("/");
            if (parts.length < 2) throw new IllegalArgumentException("repo must be owner/repo or full URL");
            owner = parts[0]; repo = parts[1];
        }

        JsonNode repoInfo = getJson("https://api.github.com/repos/" + owner + "/" + repo, token);
        String branch = repoInfo.get("default_branch").asText();

        JsonNode tree = getJson("https://api.github.com/repos/" + owner + "/" + repo + "/git/trees/" + branch + "?recursive=1", token).get("tree");

        List<String> dirs = new ArrayList<>();
        List<RepoFile> files = new ArrayList<>();

        for (JsonNode n : tree) {
            String type = n.get("type").asText();
            String path = n.get("path").asText();
            if ("tree".equals(type)) {
                dirs.add(path);
            } else if ("blob".equals(type)) {
                String rawUrl = "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + "/" + path;
                String content = getRaw(rawUrl, token);
                files.add(new RepoFile(path, content, false));
            }
        }

        // directories first by depth
        List<RepoFile> dirFiles = dirs.stream()
                .sorted((a,b) -> Integer.compare(a.split("/").length, b.split("/").length))
                .map(d -> new RepoFile(d, "", true))
                .collect(Collectors.toList());

        List<RepoFile> all = new ArrayList<>();
        all.addAll(dirFiles);
        all.addAll(files);
        return all;
    }

    private static JsonNode getJson(String url, String token) throws IOException {
        Request.Builder b = new Request.Builder().url(url).get();
        if (token != null && !token.isBlank()) b.header("Authorization", "Bearer " + token);
        try (Response r = client.newCall(b.build()).execute()) {
            if (!r.isSuccessful()) throw new IOException("HTTP " + r.code() + " for " + url);
            String body = r.body().string();
            return mapper.readTree(body);
        }
    }

    private static String getRaw(String url, String token) throws IOException {
        Request.Builder b = new Request.Builder().url(url).get();
        if (token != null && !token.isBlank()) b.header("Authorization", "Bearer " + token);
        try (Response r = client.newCall(b.build()).execute()) {
            if (!r.isSuccessful()) throw new IOException("HTTP " + r.code() + " for " + url);
            return r.body().string();
        }
    }
}
