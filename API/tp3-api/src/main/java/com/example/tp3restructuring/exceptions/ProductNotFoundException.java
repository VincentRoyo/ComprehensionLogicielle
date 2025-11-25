package com.example.tp3restructuration.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) { super("Product not found: " + id); }
}
