package com.GiveGrid.store.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private Integer quantity;

    @Column(name = "product_condition")
    private String condition = "New";

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }    // ← NEW
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }


}
