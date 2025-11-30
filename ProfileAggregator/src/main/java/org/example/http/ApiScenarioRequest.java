package org.example.http;

public record ApiScenarioRequest(String email, String password, OperationType operationType, int count) {
}
