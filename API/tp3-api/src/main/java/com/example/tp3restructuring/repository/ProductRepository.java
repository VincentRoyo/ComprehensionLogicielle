package com.example.tp3restructuring.repository;

import com.example.tp3restructuring.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    boolean existsByName(String name);
    void deleteByName(String name);
    Optional<Product> findByName(String name);
    List<Product> findByPriceGreaterThanEqual(double price);
}
