package com.GiveGrid.store.service;

import com.GiveGrid.store.entity.Product;
import com.GiveGrid.store.entity.User;
import java.util.List;

public interface ProductService {
    Product saveProduct(Product product);

    List<Product> getAllProducts();

    Product getProductById(Long id);

    List<Product> searchProducts(String query);

    // NEW — fetch products by seller
    List<Product> getProductsBySeller(User seller);

    void deleteProduct(Long id);
}
