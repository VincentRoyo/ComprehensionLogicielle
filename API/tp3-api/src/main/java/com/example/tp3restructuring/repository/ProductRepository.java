package com.example.tp3restructuring.repository;

import com.example.tp3restructuring.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByPriceGreaterThanEqual(BigDecimal price);
}
