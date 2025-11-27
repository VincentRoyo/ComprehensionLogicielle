package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Programme autonome à lancer depuis l'IDE.
 * Il lit ../logs/api.log et génère ../logs/profiles.json
 * en fonction des consignes :
 *  - profil mostly-read
 *  - profil mostly-write
 *  - profil qui cherche les produits les plus chers
 */
public class UserProfileAggregator {

    // Chemins FIXES, relatifs au projet d'agrégation (projetAgregate)
    private static final Path LOG_FILE =
            Paths.get("../logs/APIGenerated/api.log").toAbsolutePath().normalize();

    private static final Path OUT_FILE =
            Paths.get("../logs/Aggregated/profiles.json").toAbsolutePath().normalize();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        System.out.println("Lecture du fichier de logs : " + LOG_FILE);
        Map<String, UserStats> statsByEmail = aggregate(LOG_FILE);

        System.out.println("Génération des profils...");
        Map<String, Object> profilesJson = buildProfilesJson(statsByEmail);

        Files.createDirectories(OUT_FILE.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(OUT_FILE)) {
            writer.write(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(profilesJson));
        }

        System.out.println("Profils générés dans : " + OUT_FILE);
    }

    private static Map<String, UserStats> aggregate(Path logFile) throws IOException {
        Map<String, UserStats> statsByEmail = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(logFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                JsonNode node = MAPPER.readTree(line);

                String email = getText(node, "email");
                String opType = getText(node, "opType");
                String resource = getText(node, "resource");
                String path = getText(node, "path");

                if (email == null || opType == null) {
                    // Pas d'utilisateur ou pas de type d'opération → on ignore
                    continue;
                }

                UserStats stats = statsByEmail.computeIfAbsent(email, UserStats::new);

                if ("READ".equalsIgnoreCase(opType)) {
                    stats.reads++;
                } else if ("WRITE".equalsIgnoreCase(opType)) {
                    stats.writes++;
                }

                // Détection d'une recherche des produits les plus chers
                if (isExpensiveSearch(resource, path)) {
                    stats.expensiveSearches++;
                }
            }
        }

        return statsByEmail;
    }

    /**
     * Définition "produits les plus chers".
     * Adapte cette condition à la route réelle utilisée dans ton API.
     *
     * Ici : on considère que les requêtes sur /products/expensive sont
     * des recherches de produits chers.
     */
    private static boolean isExpensiveSearch(String resource, String path) {
        if (resource == null || path == null) return false;

        return "products".equalsIgnoreCase(resource)
                && path.contains("/products/expensive");
    }

    private static Map<String, Object> buildProfilesJson(Map<String, UserStats> statsByEmail) {
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
                    readWriteProfile = "READ_HEAVY";     // mostly-read
                } else if (s.writes > s.reads && ratioWrite >= 0.6) {
                    readWriteProfile = "WRITE_HEAVY";    // mostly-write
                }
            }
            u.put("readWriteProfile", readWriteProfile);

            u.put("expensiveSearches", s.expensiveSearches);
            String expensiveProfile =
                    s.expensiveSearches >= 1 ? "EXPENSIVE_SEEKER" : "NORMAL";
            u.put("expensiveProfile", expensiveProfile);

            users.add(u);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("users", users);
        return root;
    }

    private static String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText() : null;
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
