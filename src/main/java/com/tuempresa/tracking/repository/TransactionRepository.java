package com.tuempresa.tracking.repository;

import com.tuempresa.tracking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByStripeSessionId(String stripeSessionId);

    // 🔥 LOCK LÓGICO SIMPLIFICADO: Funciona con tu clase original sin crashear.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Transaction t SET t.status = 'PAID', t.email = :email WHERE t.stripeSessionId = :sessionId AND t.status = 'PENDING'")
    int markAsPaidIfPending(@Param("sessionId") String sessionId, @Param("email") String email);
}
