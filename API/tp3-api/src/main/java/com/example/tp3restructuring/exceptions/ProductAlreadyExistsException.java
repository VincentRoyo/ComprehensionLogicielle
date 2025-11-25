package com.example.tp3restructuration.exceptions;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String id) { super("Product already exists: " + id); }
}
