package com.GiveGrid.store.service;

import com.GiveGrid.store.entity.CartItem;
import com.GiveGrid.store.entity.Product;
import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.repository.CartItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartRepo;

    public CartService(CartItemRepository cartRepo) {
        this.cartRepo = cartRepo;
    }

    // ADD TO CART
    public void addToCart(User user, Product product) {

        CartItem existing = cartRepo.findByUserAndProduct(user, product);

        // Always set cart quantity equal to required remaining quantity
        int required = product.getQuantity();

        if (existing != null) {
            existing.setQuantity(required);
            cartRepo.save(existing);
        } else {
            CartItem item = new CartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(required);
            cartRepo.save(item);
        }
    }

    // GET CART ITEMS OF USER
    public List<CartItem> getUserCart(User user) {
        return cartRepo.findByUser(user);
    }

    // GET SINGLE CART ITEM
    public CartItem getItemById(Long id) {
        return cartRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + id));
    }

    // REMOVE FROM CART
    public void removeItem(Long id) {
        cartRepo.deleteById(id);
    }

    public void save(CartItem item) {
        cartRepo.save(item);
    }
}

