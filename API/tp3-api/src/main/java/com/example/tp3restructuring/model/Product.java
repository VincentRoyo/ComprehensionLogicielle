package com.example.tp3restructuring.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private double price;
    private LocalDate expirationDate;
}
