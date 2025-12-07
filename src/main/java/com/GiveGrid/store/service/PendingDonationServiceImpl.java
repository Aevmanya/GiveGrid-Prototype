package com.GiveGrid.store.service;

import com.GiveGrid.store.entity.PendingDonation;
import com.GiveGrid.store.entity.Product;
import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.repository.CartItemRepository;
import com.GiveGrid.store.repository.PendingDonationRepository;
import com.GiveGrid.store.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PendingDonationServiceImpl implements PendingDonationService {

    private final PendingDonationRepository repo;
    private final ProductRepository productRepo;
    private final CartItemRepository cartRepo;

    public PendingDonationServiceImpl(
            PendingDonationRepository repo,
            ProductRepository productRepo,
            CartItemRepository cartRepo
    ) {
        this.repo = repo;
        this.productRepo = productRepo;
        this.cartRepo = cartRepo;
    }

    @Override
    public PendingDonation save(PendingDonation pending) {
        return repo.save(pending);
    }

    @Override
    public List<PendingDonation> getPendingForSeller(User seller) {
        return repo.findBySellerAndStatusOrderByCreatedAtDesc(
                seller, PendingDonation.Status.PENDING
        );
    }

    @Override
    public List<PendingDonation> getForBuyer(User buyer) {
        return repo.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    @Override
    public List<PendingDonation> getApprovedForSeller(User seller) {
        return repo.findBySellerAndStatusOrderByCreatedAtDesc(
                seller, PendingDonation.Status.ACCEPTED
        );
    }

    @Override
    public void approve(Long id) {
        PendingDonation pd = repo.findById(id).orElse(null);
        if (pd == null) return;

        Product product = pd.getProduct();
        if (product == null) return;

        int requestedQty = pd.getQuantity();
        int availableQty = product.getQuantity();

        // Quantity approved = min(requested, remaining)
        int approvedQty = Math.min(requestedQty, availableQty);

        pd.setApprovedQuantity(approvedQty);
        pd.setApprovedAt(LocalDateTime.now());
        pd.setStatus(PendingDonation.Status.ACCEPTED);
        repo.save(pd);

        // 🔻 SUBTRACT from product quantity
        product.setQuantity(availableQty - approvedQty);
        productRepo.save(product);

        // ❌ REMOVE product from buyer carts if quantity = 0
        if (product.getQuantity() <= 0) {
            cartRepo.deleteByProduct(product);
        }
    }

    @Override
    public void reject(Long id) {
        PendingDonation pd = repo.findById(id).orElse(null);
        if (pd != null) {
            pd.setStatus(PendingDonation.Status.REJECTED);
            repo.save(pd);
        }
    }
}


