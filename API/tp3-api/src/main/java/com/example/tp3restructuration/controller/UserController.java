package com.example.tp3restructuration.controller;

import com.example.tp3restructuration.model.User;
import com.example.tp3restructuration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository repo;

    @GetMapping
    public List<User> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable String id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return repo.save(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        repo.deleteById(id);
    }
}


