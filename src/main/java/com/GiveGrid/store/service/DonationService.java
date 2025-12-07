package com.GiveGrid.store.service;

import com.GiveGrid.store.entity.Donation;
import com.GiveGrid.store.entity.User;

import java.util.List;

public interface DonationService {
    Donation save(Donation donation);
    List<Donation> getDonationsForUser(User user);
}
