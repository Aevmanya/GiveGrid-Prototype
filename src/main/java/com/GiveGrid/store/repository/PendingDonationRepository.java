package com.GiveGrid.store.repository;

import com.GiveGrid.store.entity.PendingDonation;
import com.GiveGrid.store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingDonationRepository extends JpaRepository<PendingDonation, Long> {

    List<PendingDonation> findBySellerOrderByCreatedAtDesc(User seller);

    List<PendingDonation> findByBuyerOrderByCreatedAtDesc(User buyer);

    List<PendingDonation> findBySellerAndStatusOrderByCreatedAtDesc(
            User seller,
            PendingDonation.Status status
    );

    List<PendingDonation> findByBuyerAndStatusOrderByCreatedAtDesc(
            User buyer,
            PendingDonation.Status status
    );
}


