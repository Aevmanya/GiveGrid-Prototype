package com.GiveGrid.store.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The buyer who donated
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // The product donated (reference)
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // For this implementation we store one donation per unit (quantity = 1)
    // but keep the field for flexibility
    private Integer quantity;

    // Condition of the donated item (e.g. New, Used, etc.)
    @Column(name = "item_condition")
    private String condition;


    // When donation was recorded
    private LocalDateTime donatedAt = LocalDateTime.now();

    // --- getters / setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public LocalDateTime getDonatedAt() { return donatedAt; }
    public void setDonatedAt(LocalDateTime donatedAt) { this.donatedAt = donatedAt; }

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

}
