package com.airouteviva.repository;

import com.airouteviva.entity.RoutingDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoutingDecisionRepository extends JpaRepository<RoutingDecision, Long> {
    Optional<RoutingDecision> findByRequestId(String requestId);
    List<RoutingDecision> findByRequestIdIn(List<String> requestIds);
}
