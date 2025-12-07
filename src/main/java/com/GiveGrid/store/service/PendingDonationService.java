package com.GiveGrid.store.service;

import com.GiveGrid.store.entity.PendingDonation;
import com.GiveGrid.store.entity.User;

import java.util.List;

public interface PendingDonationService {

    PendingDonation save(PendingDonation pending);

    List<PendingDonation> getPendingForSeller(User seller);

    List<PendingDonation> getForBuyer(User buyer);

    // Get all approved donations for a seller (history)
    List<PendingDonation> getApprovedForSeller(User seller);

    // ✔ Approve a donation AND set approvedAt + approvedQuantity
    void approve(Long id);

    void reject(Long id);
}
