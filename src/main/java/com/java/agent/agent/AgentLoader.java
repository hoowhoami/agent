package com.java.agent.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agent configuration loader
 */
@Slf4j
@Component
public class AgentLoader {
    private static final Pattern FRONTMATTER_PATTERN = Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n(.*)$", Pattern.DOTALL);
    private final Map<String, AgentConfig> agentCache = new HashMap<>();

    public List<AgentConfig> loadAgents() {
        agentCache.clear();

        AgentConfig builtinAgent = createBuiltinAgent();
        agentCache.put(builtinAgent.getAgentType(), builtinAgent);

        loadFromDirectory(getUserAgentDir(), "user");
        loadFromDirectory(getProjectAgentDir(), "project");

        return new ArrayList<>(agentCache.values());
    }

    public AgentConfig getAgent(String agentType) {
        if (agentCache.isEmpty()) {
            loadAgents();
        }
        return agentCache.get(agentType);
    }

    private AgentConfig createBuiltinAgent() {
        AgentConfig agent = new AgentConfig();
        agent.setAgentType("general-purpose");
        agent.setWhenToUse("General-purpose agent for researching complex questions, searching for code, and executing multi-step tasks");
        agent.setTools(List.of("*"));
        agent.setSystemPrompt("You are a general-purpose agent. Use available tools to complete tasks efficiently.");
        agent.setLocation("built-in");
        return agent;
    }

    private void loadFromDirectory(Path dir, String location) {
        if (!Files.exists(dir)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.md")) {
            for (Path file : stream) {
                try {
                    AgentConfig agent = parseAgentFile(file, location);
                    if (agent != null) {
                        agentCache.put(agent.getAgentType(), agent);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse agent file: {}", file, e);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load agents from: {}", dir, e);
        }
    }

    private AgentConfig parseAgentFile(Path file, String location) throws IOException {
        String content = Files.readString(file);
        Matcher matcher = FRONTMATTER_PATTERN.matcher(content);

        if (!matcher.matches()) return null;

        String frontmatter = matcher.group(1);
        String body = matcher.group(2);

        Map<String, String> metadata = parseFrontmatter(frontmatter);

        if (!metadata.containsKey("name") || !metadata.containsKey("description")) {
            return null;
        }

        AgentConfig agent = new AgentConfig();
        agent.setAgentType(metadata.get("name"));
        agent.setWhenToUse(metadata.get("description"));
        agent.setTools(parseTools(metadata.get("tools")));
        agent.setSystemPrompt(body.trim());
        agent.setLocation(location);
        agent.setModelName(metadata.get("model_name"));

        return agent;
    }

    private Map<String, String> parseFrontmatter(String frontmatter) {
        Map<String, String> result = new HashMap<>();
        for (String line : frontmatter.split("\n")) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                result.put(parts[0].trim(), parts[1].trim());
            }
        }
        return result;
    }

    private List<String> parseTools(String tools) {
        if (tools == null || tools.equals("*")) {
            return List.of("*");
        }
        return Arrays.asList(tools.split(","));
    }

    private Path getUserAgentDir() {
        return Paths.get(System.getProperty("user.home"), ".kode", "agents");
    }

    private Path getProjectAgentDir() {
        return Paths.get(System.getProperty("user.dir"), ".kode", "agents");
    }
}
