package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class UserProfileAggregatorService {

    // Chemins FIXES, relatifs au projet d'agrégation (projetAgregate)
    private static final Path LOG_FILE =
            Paths.get("../logs/APIGenerated/api.log").toAbsolutePath().normalize();

    private static final Path OUT_FILE =
            Paths.get("../logs/Aggregated/profiles.json").toAbsolutePath().normalize();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String aggregateAndWriteProfiles() throws IOException {
        Map<String, UserStats> statsByEmail = aggregate(LOG_FILE);
        Map<String, Object> profilesJson = buildProfilesJson(statsByEmail);

        Files.createDirectories(OUT_FILE.getParent());

        String json = MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(profilesJson);

        try (BufferedWriter writer = Files.newBufferedWriter(OUT_FILE)) {
            writer.write(json);
        }

        return json;
    }

    private Map<String, UserStats> aggregate(Path logFile) throws IOException {
        Map<String, UserStats> statsByEmail = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(logFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                JsonNode node = MAPPER.readTree(line);

                // email
                String email = extractEmail(node);

                // type d'op (READ / WRITE) et resource (products, auth, etc.)
                String opType = getText(node, "opType");
                String resource = getText(node, "resource"); // <-- en accord avec tes logs

                // param minPrice
                Double minPrice = parseDoubleOrNull(getText(node, "query.minPrice"));

                if (email == null || opType == null) {
                    continue;
                }

                UserStats stats = statsByEmail.computeIfAbsent(email, UserStats::new);

                if ("READ".equalsIgnoreCase(opType)) {
                    stats.reads++;
                } else if ("WRITE".equalsIgnoreCase(opType)) {
                    stats.writes++;
                }

                if (isExpensiveSearch(resource, minPrice)) {
                    stats.expensiveSearches++;
                }
            }
        }

        return statsByEmail;
    }

    private boolean isExpensiveSearch(String resource, Double minPrice) {
        if (resource == null) return false;

        if ("products".equalsIgnoreCase(resource)) {
            return minPrice != null && minPrice >= 50.0;
        }
        return false;
    }

    private Map<String, Object> buildProfilesJson(Map<String, UserStats> statsByEmail) {
        List<Map<String, Object>> users = new ArrayList<>();

        for (UserStats s : statsByEmail.values()) {
            Map<String, Object> u = new LinkedHashMap<>();
            u.put("email", s.email);
            u.put("totalReads", s.reads);
            u.put("totalWrites", s.writes);

            int totalOps = s.reads + s.writes;
            String readWriteProfile = "NEUTRAL";
            if (totalOps > 0) {
                double ratioRead = (double) s.reads / totalOps;
                double ratioWrite = (double) s.writes / totalOps;

                if (s.reads > s.writes && ratioRead >= 0.6) {
                    readWriteProfile = "READ_HEAVY";
                } else if (s.writes > s.reads && ratioWrite >= 0.6) {
                    readWriteProfile = "WRITE_HEAVY";
                }
            }
            u.put("readWriteProfile", readWriteProfile);

            u.put("expensiveSearches", s.expensiveSearches);
            String expensiveProfile =
                    (double) s.expensiveSearches /s.reads >= 0.6 ? "EXPENSIVE_SEEKER" : "NORMAL";
            u.put("expensiveProfile", expensiveProfile);

            users.add(u);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("users", users);
        return root;
    }

    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText() : null;
    }

    private String extractEmail(JsonNode node) {
        String direct = getText(node, "email");
        if (direct != null) return direct;

        JsonNode userConnected = node.get("userConnected");
        if (userConnected != null && !userConnected.isNull()) {
            JsonNode emailNode = userConnected.get("email");
            if (emailNode != null && !emailNode.isNull()) {
                return emailNode.asText();
            }
        }
        return null;
    }

    private Double parseDoubleOrNull(String text) {
        if (text == null) return null;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class UserStats {
        final String email;
        int reads;
        int writes;
        int expensiveSearches;

        UserStats(String email) {
            this.email = email;
        }
    }
}
