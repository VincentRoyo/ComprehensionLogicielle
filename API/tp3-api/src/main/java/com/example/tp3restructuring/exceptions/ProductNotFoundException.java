package com.example.tp3restructuring.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) { super("Product not found: " + id); }
}
