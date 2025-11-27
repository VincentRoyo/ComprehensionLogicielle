package com.example.tp3restructuring.repository;

import com.example.tp3restructuring.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
