package com.tuempresa.tracking.repository;

import com.tuempresa.tracking.model.StripeEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeEventRepository extends JpaRepository<StripeEventRecord, Long> {
}
