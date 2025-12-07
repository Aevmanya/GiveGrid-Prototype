package com.GiveGrid.store.repository;

import com.GiveGrid.store.entity.Donation;
import com.GiveGrid.store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByUserOrderByDonatedAtDesc(User user);
}
