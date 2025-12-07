package com.GiveGrid.store.service;

import com.GiveGrid.store.entity.Product;
import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.repository.CartItemRepository;
import com.GiveGrid.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public List<Product> searchProducts(String query) {
        return productRepository.searchProducts(query);
    }

    // NEW — fetch seller-specific products
    @Override
    public List<Product> getProductsBySeller(User seller) {
        return productRepository.findBySeller(seller);
    }
    @Autowired
    private CartItemRepository cartItemRepository;

    @Transactional
    @Override
    public void deleteProduct(Long id) {

        // delete cart items referencing this product
        cartItemRepository.deleteByProductId(id);

        // delete actual product
        productRepository.deleteById(id);
    }

}