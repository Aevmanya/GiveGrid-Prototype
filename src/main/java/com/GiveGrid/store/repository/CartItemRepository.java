package com.GiveGrid.store.repository;

import com.GiveGrid.store.entity.CartItem;
import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    CartItem findByUserAndProduct(User user, Product product);
    void deleteByProductId(Long productId);
    void deleteByProduct(Product product);
}




