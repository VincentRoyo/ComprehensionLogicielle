package com.example.tp3restructuration.repository;

import com.example.tp3restructuration.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
