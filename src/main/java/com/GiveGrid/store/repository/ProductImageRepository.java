package com.GiveGrid.store.repository;

import com.GiveGrid.store.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByIdAsc(Long productId);
    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);
    void deleteByProductId(Long productId);
}
