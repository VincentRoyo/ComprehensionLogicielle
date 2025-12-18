package com.example.tp3restructuring.controller;

import com.example.tp3restructuring.Service.ProductService;
import com.example.tp3restructuring.exceptions.ProductAlreadyExistsException;
import com.example.tp3restructuring.exceptions.ProductNotFoundException;
import com.example.tp3restructuring.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    @GetMapping()
    public List<Product> getProducts(
            @RequestParam(required = false) Double minPrice
    ) {
        return service.findByMinPrice(minPrice);
    }

    @GetMapping("/{name}")
    public Product get(@PathVariable String name) {
        return service.get(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody Product p) {
        return service.create(p);
    }

    @PutMapping("/{name}")
    public Product update(@PathVariable String name, @RequestBody Product p) {
        return service.update(name, p);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String name) {
        service.delete(name);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProductNotFoundException.class)
    public String handleNotFound(ProductNotFoundException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ProductAlreadyExistsException.class)
    public String handleConflict(ProductAlreadyExistsException ex) {
        return ex.getMessage();
    }
}
