package com.moneyfi.transaction.batch.repository;

import com.moneyfi.transaction.batch.entity.BatchJobDetailsAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchJobDetailsAddonRepository extends JpaRepository<BatchJobDetailsAddon, Long> {
}
