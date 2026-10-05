package com.airouteviva.repository;

import com.airouteviva.entity.ResourceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceSnapshotRepository extends JpaRepository<ResourceSnapshot, Long> {
    Optional<ResourceSnapshot> findByRequestId(String requestId);
    List<ResourceSnapshot> findByRequestIdInOrderByTimestampAsc(List<String> requestIds);
}
