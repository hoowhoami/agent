package com.java.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Grep tool for powerful code search using regex patterns
 * Similar to ripgrep functionality
 * @author whoami
 */
@Slf4j
@Component
public class GrepTool extends AbstractAgentTool {

    @Tool(description = """
            A powerful search tool for finding content in files using regex patterns.

            Parameters:
            - pattern: The regex pattern to search for (required)
            - path: Directory to search in (defaults to current directory)
            - glob: Glob pattern to filter files (e.g., "*.java", "**/*.ts")
            - type: File type filter (e.g., "java", "py", "js")
            - caseInsensitive: Case insensitive search (default: false)
            - outputMode: "content" (shows matches), "files_with_matches" (only filenames), "count" (match counts)
            - contextLines: Number of context lines to show (default: 0)
            - headLimit: Limit results to first N matches (default: 100)
            - multiline: Enable multiline mode where patterns can span lines (default: false)

            Examples:
            - Search for "TODO" in Java files: pattern="TODO", glob="*.java"
            - Find class definitions: pattern="class\\s+\\w+", type="java"
            - Case-insensitive search: pattern="error", caseInsensitive=true
            """)
    public String grep(
            String pattern,
            String path,
            String glob,
            String type,
            Boolean caseInsensitive,
            String outputMode,
            Integer contextLines,
            Integer headLimit,
            Boolean multiline) {

        try {
            // Setup parameters
            Path searchPath = path != null ? Paths.get(path) : Paths.get(".");
            boolean ignoreCase = Boolean.TRUE.equals(caseInsensitive);
            String mode = outputMode != null ? outputMode : "files_with_matches";
            int context = contextLines != null ? contextLines : 0;
            int limit = headLimit != null ? headLimit : 100;
            boolean isMultiline = Boolean.TRUE.equals(multiline);

            // Compile regex pattern
            int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
            if (isMultiline) {
                flags |= Pattern.MULTILINE | Pattern.DOTALL;
            }
            Pattern searchPattern = Pattern.compile(pattern, flags);

            // Get file extension filter
            String fileExtension = getFileExtension(type, glob);

            // Search files
            List<SearchResult> results = new ArrayList<>();
            searchFiles(searchPath, searchPattern, fileExtension, mode, context, limit, isMultiline, results);

            // Format output
            return formatResults(results, mode, limit);

        } catch (Exception e) {
            log.error("Grep search failed", e);
            return "Error: " + e.getMessage();
        }
    }

    private void searchFiles(
            Path searchPath,
            Pattern pattern,
            String fileExtension,
            String mode,
            int context,
            int limit,
            boolean multiline,
            List<SearchResult> results) throws IOException {

        try (Stream<Path> paths = Files.walk(searchPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> shouldIncludeFile(p, fileExtension))
                 .forEach(file -> {
                     if (results.size() >= limit) return;
                     searchInFile(file, pattern, mode, context, multiline, results);
                 });
        }
    }

    private void searchInFile(
            Path file,
            Pattern pattern,
            String mode,
            int context,
            boolean multiline,
            List<SearchResult> results) {

        try {
            if (multiline) {
                // Read entire file for multiline search
                String content = Files.readString(file);
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    SearchResult result = new SearchResult();
                    result.file = file.toString();
                    result.matches = new ArrayList<>();

                    do {
                        MatchInfo match = new MatchInfo();
                        match.lineNumber = countLines(content, matcher.start());
                        match.content = matcher.group();
                        result.matches.add(match);
                    } while (matcher.find() && result.matches.size() < 100);

                    results.add(result);
                }
            } else {
                // Line by line search
                List<String> lines = Files.readAllLines(file);
                SearchResult result = new SearchResult();
                result.file = file.toString();
                result.matches = new ArrayList<>();

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        MatchInfo match = new MatchInfo();
                        match.lineNumber = i + 1;
                        match.content = line;

                        // Add context lines
                        if (context > 0) {
                            match.contextBefore = getContextLines(lines, i - context, i);
                            match.contextAfter = getContextLines(lines, i + 1, i + 1 + context);
                        }

                        result.matches.add(match);
                    }
                }

                if (!result.matches.isEmpty()) {
                    results.add(result);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to search in file: {}", file, e);
        }
    }

    private boolean shouldIncludeFile(Path file, String fileExtension) {
        if (fileExtension == null) return true;
        String fileName = file.getFileName().toString();
        return fileName.endsWith(fileExtension);
    }

    private String getFileExtension(String type, String glob) {
        if (type != null) {
            return switch (type.toLowerCase()) {
                case "java" -> ".java";
                case "py", "python" -> ".py";
                case "js", "javascript" -> ".js";
                case "ts", "typescript" -> ".ts";
                case "go" -> ".go";
                case "rs", "rust" -> ".rs";
                case "cpp", "cc" -> ".cpp";
                case "c" -> ".c";
                case "h" -> ".h";
                default -> "." + type;
            };
        }

        if (glob != null) {
            // Simple glob to extension conversion
            if (glob.startsWith("*.")) {
                return glob.substring(1);
            }
            if (glob.contains("*.")) {
                return glob.substring(glob.indexOf("*.") + 1);
            }
        }

        return null;
    }

    private List<String> getContextLines(List<String> lines, int start, int end) {
        List<String> context = new ArrayList<>();
        for (int i = Math.max(0, start); i < Math.min(lines.size(), end); i++) {
            context.add(lines.get(i));
        }
        return context;
    }

    private int countLines(String text, int position) {
        int lines = 1;
        for (int i = 0; i < position && i < text.length(); i++) {
            if (text.charAt(i) == '\n') lines++;
        }
        return lines;
    }

    private String formatResults(List<SearchResult> results, String mode, int limit) {
        if (results.isEmpty()) {
            return "No matches found.";
        }

        StringBuilder output = new StringBuilder();

        switch (mode) {
            case "content" -> {
                int count = 0;
                for (SearchResult result : results) {
                    output.append("\n").append(result.file).append(":\n");
                    for (MatchInfo match : result.matches) {
                        if (count++ >= limit) break;

                        if (match.contextBefore != null && !match.contextBefore.isEmpty()) {
                            for (String line : match.contextBefore) {
                                output.append("  ").append(line).append("\n");
                            }
                        }

                        output.append(match.lineNumber).append(": ").append(match.content).append("\n");

                        if (match.contextAfter != null && !match.contextAfter.isEmpty()) {
                            for (String line : match.contextAfter) {
                                output.append("  ").append(line).append("\n");
                            }
                        }
                    }
                    if (count >= limit) break;
                }
            }
            case "count" -> {
                for (SearchResult result : results) {
                    output.append(result.file).append(": ").append(result.matches.size()).append("\n");
                }
            }
            default -> { // files_with_matches
                for (SearchResult result : results) {
                    output.append(result.file).append("\n");
                }
            }
        }

        return output.toString().trim();
    }

    private static class SearchResult {
        String file;
        List<MatchInfo> matches;
    }

    private static class MatchInfo {
        int lineNumber;
        String content;
        List<String> contextBefore;
        List<String> contextAfter;
    }
}
