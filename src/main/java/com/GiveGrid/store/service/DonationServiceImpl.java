package com.GiveGrid.store.service;

import com.GiveGrid.store.entity.Donation;
import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.repository.DonationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;

    public DonationServiceImpl(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    @Override
    public Donation save(Donation donation) {
        return donationRepository.save(donation);
    }

    @Override
    public List<Donation> getDonationsForUser(User user) {
        return donationRepository.findByUserOrderByDonatedAtDesc(user);
    }
}

