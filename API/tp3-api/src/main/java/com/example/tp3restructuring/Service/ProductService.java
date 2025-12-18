package com.example.tp3restructuring.Service;

import com.example.tp3restructuring.exceptions.ProductAlreadyExistsException;
import com.example.tp3restructuring.exceptions.ProductNotFoundException;
import com.example.tp3restructuring.model.Product;
import com.example.tp3restructuring.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    public List<Product> list() {
        return repo.findAll();
    }

    public Product get(String name) {
        return repo.findByName(name).orElseThrow(() -> new ProductNotFoundException(name));
    }

    public Product create(Product p) {
        if (p.getId() != null && repo.existsById(p.getId())) {
            throw new ProductAlreadyExistsException(p.getId());
        }
        return repo.save(p);
    }

    public List<Product> findByMinPrice(Double minPrice) {
        if (minPrice == null) {
            return repo.findAll();
        }
        return repo.findByPriceGreaterThanEqual(minPrice);
    }

    public Product update(String name, Product p) {
        if (!repo.existsByName(name)) throw new ProductNotFoundException(name);
        p.setName(name);
        return repo.save(p);
    }

    public void delete(String name) {
        if (!repo.existsByName(name)) throw new ProductNotFoundException(name);
        repo.deleteByName(name);
    }
}
