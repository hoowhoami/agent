package com.java.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Glob tool for fast file pattern matching
 * @author whoami
 */
@Slf4j
@Component
public class GlobTool extends AbstractAgentTool {

    @Tool(description = """
            Fast file pattern matching tool that works with any codebase size.
            Supports glob patterns like "**/*.js" or "src/**/*.ts".
            Returns matching file paths sorted by modification time.

            Parameters:
            - pattern: The glob pattern to match files against (required)
            - path: The directory to search in (defaults to current directory)

            Examples:
            - Find all Java files: pattern="**/*.java"
            - Find TypeScript files in src: pattern="src/**/*.ts"
            - Find specific config files: pattern="**/config/*.yml"
            - Find test files: pattern="**/*Test.java"

            Glob pattern syntax:
            - * matches any characters except /
            - ** matches any characters including /
            - ? matches a single character
            - [abc] matches one character from the set
            - {a,b,c} matches one of the alternatives
            """)
    public String glob(String pattern, String path) {
        try {
            Path searchPath = path != null ? Paths.get(path) : Paths.get(".");

            if (!Files.exists(searchPath)) {
                return "Error: Path does not exist: " + searchPath;
            }

            // Convert glob pattern to PathMatcher
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

            // Find matching files
            List<FileResult> results = new ArrayList<>();
            Files.walkFileTree(searchPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    Path relativePath = searchPath.relativize(file);
                    if (matcher.matches(relativePath) || matcher.matches(file)) {
                        FileResult result = new FileResult();
                        result.path = file.toString();
                        result.modifiedTime = attrs.lastModifiedTime().toMillis();
                        results.add(result);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // Skip files that can't be accessed
                    return FileVisitResult.CONTINUE;
                }
            });

            // Sort by modification time (most recent first)
            results.sort(Comparator.comparingLong((FileResult r) -> r.modifiedTime).reversed());

            // Format output
            if (results.isEmpty()) {
                return "No files found matching pattern: " + pattern;
            }

            StringBuilder output = new StringBuilder();
            output.append("Found ").append(results.size()).append(" file(s):\n\n");

            for (FileResult result : results) {
                output.append(result.path).append("\n");
            }

            return output.toString().trim();

        } catch (Exception e) {
            log.error("Glob search failed", e);
            return "Error: " + e.getMessage();
        }
    }

    private static class FileResult {
        String path;
        long modifiedTime;
    }
}
