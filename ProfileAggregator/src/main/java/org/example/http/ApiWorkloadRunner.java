package org.example.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Random;

public class ApiWorkloadRunner {

    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;

    private final Random random = new Random();

    public ApiWorkloadRunner(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void runScenario(List<ApiScenarioRequest> requests) throws Exception {
        for (ApiScenarioRequest req : requests) {
            String token = login(req.email(), req.password());
            for (int i = 0; i < req.count(); i++) {
                switch (req.operationType()) {
                    case READ_PRODUCTS_LIST -> doReadProductsList(token, false);
                    case READ_PRODUCTS_EXPENSIVE -> doReadProductsList(token, true);
                    case WRITE_CREATE_PRODUCT -> doCreateProduct(token);
                }
            }
        }
    }

    private String login(String email, String password) throws Exception {
        String body = mapper.createObjectNode()
                .put("email", email)
                .put("password", password)
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("Login failed for " + email + " : " + response.statusCode() + " / " + response.body());
        }

        JsonNode node = mapper.readTree(response.body());
        return node.get("token").asText();
    }

    private void doReadProductsList(String token, boolean expensive) throws Exception {
        String uri = baseUrl + "/products/";
        if (expensive) {
            uri += "?minPrice=50.0";
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        client.send(request, HttpResponse.BodyHandlers.discarding());
    }

    private void doCreateProduct(String token) throws Exception {
        BigDecimal price = BigDecimal.valueOf(10 + random.nextInt(90));
        String expirationDate = "2026-12-31"; // LocalDate côté Java

        var node = mapper.createObjectNode();
        node.put("name", "Generated product " + random.nextInt(100000));
        node.put("price", price);
        node.put("expirationDate", expirationDate);

        String body = mapper.writeValueAsString(node);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/products"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.discarding());
    }
}
