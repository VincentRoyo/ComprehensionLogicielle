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

    public Product get(String id) {
        return repo.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product create(Product p) {
        if (p.getId() != null && repo.existsById(p.getId())) {
            throw new ProductAlreadyExistsException(p.getId());
        }
        return repo.save(p);
    }

    public Product update(String id, Product p) {
        if (!repo.existsById(id)) throw new ProductNotFoundException(id);
        p.setId(id);
        return repo.save(p);
    }

    public void delete(String id) {
        if (!repo.existsById(id)) throw new ProductNotFoundException(id);
        repo.deleteById(id);
    }
}
