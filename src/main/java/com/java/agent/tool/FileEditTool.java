package com.java.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File edit tool - performs exact string replacements in files
 * Enhanced to match Claude Code Agent Edit tool behavior
 * @author whoami
 */
@Slf4j
@Component
public class FileEditTool extends AbstractAgentTool {

    @Tool(description = """
            Performs exact string replacements in files.

            Usage:
            - You must read the file first before editing (use readFile tool)
            - Preserve exact indentation as it appears in the file
            - The edit will FAIL if oldString is not unique in the file
            - Set replaceAll=true to replace all occurrences (useful for renaming variables)

            Parameters:
            - filePath: The absolute path to the file to modify (required)
            - oldString: The text to replace (must be unique unless replaceAll=true) (required)
            - newString: The text to replace it with (must be different from oldString) (required)
            - replaceAll: Replace all occurrences of oldString (default: false)

            Best Practices:
            - ALWAYS prefer editing existing files over creating new ones
            - Provide enough context in oldString to make it unique
            - Don't include line numbers from the read output in oldString
            - Keep edits focused and minimal

            Example:
            To change a method name:
            oldString="public void process() {"
            newString="public void handleProcess() {"
            """)
    public String editFile(String filePath, String oldString, String newString, Boolean replaceAll) {
        try {
            // Validate inputs
            if (oldString == null || oldString.isEmpty()) {
                return "Error: oldString cannot be empty";
            }

            if (newString == null) {
                return "Error: newString cannot be null";
            }

            if (oldString.equals(newString)) {
                return "Error: oldString and newString must be different";
            }

            Path path = Path.of(filePath);
            if (!Files.exists(path)) {
                return "Error: File not found: " + filePath;
            }

            String content = Files.readString(path);

            // Check if oldString exists
            if (!content.contains(oldString)) {
                return "Error: Text to replace not found in file. " +
                       "Make sure the oldString exactly matches the file content, including whitespace and indentation.";
            }

            boolean shouldReplaceAll = Boolean.TRUE.equals(replaceAll);

            // Check uniqueness if not replaceAll
            if (!shouldReplaceAll) {
                int firstIndex = content.indexOf(oldString);
                int lastIndex = content.lastIndexOf(oldString);
                if (firstIndex != lastIndex) {
                    int occurrences = countOccurrences(content, oldString);
                    return String.format(
                        "Error: oldString appears %d times in the file. " +
                        "Either provide a larger string with more surrounding context to make it unique, " +
                        "or set replaceAll=true to replace all occurrences.",
                        occurrences
                    );
                }
            }

            // Perform replacement
            String newContent;
            if (shouldReplaceAll) {
                newContent = content.replace(oldString, newString);
            } else {
                // Replace only first occurrence
                int index = content.indexOf(oldString);
                newContent = content.substring(0, index) + newString + content.substring(index + oldString.length());
            }

            // Write back to file
            Files.writeString(path, newContent);

            int replacements = shouldReplaceAll ? countOccurrences(content, oldString) : 1;
            return String.format("Successfully edited %s (%d replacement%s made)",
                filePath, replacements, replacements > 1 ? "s" : "");

        } catch (Exception e) {
            log.error("Error editing file: {}", filePath, e);
            return "Error editing file: " + e.getMessage();
        }
    }

    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}

