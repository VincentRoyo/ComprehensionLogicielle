package com.example.tp3restructuration.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private BigDecimal price;
    private LocalDate expirationDate;
}
