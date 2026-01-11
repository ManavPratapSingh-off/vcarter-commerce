package com.vcarter.ecommerce.service;

import com.vcarter.ecommerce.entity.Product;
import com.vcarter.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repo;

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Product getProductById (Long Id) {
        return repo.findById(Id).orElse(null);
    }

    public Product getProductByName(String Name) {
        return repo.findByName(Name).orElse(null);
    }

    public List<Product> getProductsByCategory(String Category) { return repo.findProductsByCategory(Category); }

    public Product saveProduct(Product product) {
        return repo.save(product);
    }

    public Product updateProduct(Product product) {
        Product existingProduct = repo.findById(product.getId()).orElse(null);
        if (existingProduct != null) {
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setCategory(product.getCategory());
            existingProduct.setAvailabilityStatus(product.getAvailabilityStatus());

            repo.save(existingProduct);
        }
        return existingProduct;
    }

    public void deleteProduct(Long Id) {
        repo.deleteById(Id);
    }
}
