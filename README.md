# IntelliJ Robot Cloner — Advanced

This project simulates a human developer using `java.awt.Robot` to recreate a public GitHub repository inside an open IntelliJ IDEA window.

Features:
- Fetches repo tree and file contents from GitHub
- Creates folders and files via IntelliJ keyboard shortcuts
- Human-like typing: randomized delays, bursts, occasional typos/backspaces
- Chunked clipboard pastes for large files
- Small mouse wiggles to simulate activity
- Retries and a basic recovery sequence (ESC + refocus)
- Configurable through `src/main/resources/app.properties`

IMPORTANT SAFETY NOTES:
- **IntelliJ must be open and focused** in the Project tool window when the program runs.
- Robot will type into whatever window is active — do not run this while other important apps are focused.
- This tool **visually simulates human activity**. For deterministic/testable installs, prefer cloning the repo via Git and writing files to disk.

Build & run:
```
mvn package
java -jar target/intellij-robot-cloner-advanced-1.0.0-jar-with-dependencies.jar
```

You can edit `app.properties` to change typing speed, enable/disable paste, adjust mouse wiggles, etc.
