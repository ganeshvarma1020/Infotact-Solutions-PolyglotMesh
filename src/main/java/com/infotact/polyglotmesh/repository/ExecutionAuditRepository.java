package com.infotact.polyglotmesh.repository;

import com.infotact.polyglotmesh.entity.ExecutionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExecutionAuditRepository extends JpaRepository<ExecutionAuditLog, Long> {
    List<ExecutionAuditLog> findTop25ByOrderByExecutedAtDesc();
    long countByStatus(String status);
}
