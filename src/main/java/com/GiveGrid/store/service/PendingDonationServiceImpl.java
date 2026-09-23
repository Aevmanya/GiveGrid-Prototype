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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class PendingDonationServiceImpl implements PendingDonationService {

    private final PendingDonationRepository repo;
    private final ProductRepository productRepo;
    private final CartItemRepository cartRepo;
    private final JavaMailSender mailSender;
    private final String mailFrom;

    public PendingDonationServiceImpl(
            PendingDonationRepository repo,
            ProductRepository productRepo,
            CartItemRepository cartRepo,
            JavaMailSender mailSender,
            @Value("${givegrid.mail.from:}") String mailFrom
    ) {
        this.repo = repo;
        this.productRepo = productRepo;
        this.cartRepo = cartRepo;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
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
        if (pd == null || pd.getStatus() != PendingDonation.Status.PENDING) return;

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

        sendApprovalEmail(pd, approvedQty);
    }

    private void sendApprovalEmail(PendingDonation pd, int approvedQty) {
        User donor = pd.getBuyer();
        if (donor == null || donor.getEmail() == null || donor.getEmail().trim().isEmpty()) return;

        try {
            String donorName = donor.getFullName() != null && !donor.getFullName().trim().isEmpty()
                    ? donor.getFullName().trim() : donor.getUsername();
            String requestName = pd.getProduct() != null && pd.getProduct().getName() != null
                    ? pd.getProduct().getName() : "your donation request";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(donor.getEmail().trim());
            if (mailFrom != null && !mailFrom.trim().isEmpty()) message.setFrom(mailFrom.trim());
            message.setSubject("Your GiveGrid donation was accepted");
            message.setText(
                    "Hi " + donorName + ",\n\n" +
                    "Good news — your donation request has been accepted by the organisation.\n\n" +
                    "Request: " + requestName + "\n" +
                    "Quantity accepted: " + approvedQty + "\n" +
                    "Condition: " + (pd.getCondition() == null ? "Not specified" : pd.getCondition()) + "\n\n" +
                    "Thank you for helping fulfil a community request through GiveGrid.\n\n" +
                    "— The GiveGrid team"
            );
            mailSender.send(message);
        } catch (Exception ignored) {
            // Notification failure must not undo an accepted donation.
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


