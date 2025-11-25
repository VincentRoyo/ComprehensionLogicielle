package com.example.tp3restructuration.repository;

import com.example.tp3restructuration.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
