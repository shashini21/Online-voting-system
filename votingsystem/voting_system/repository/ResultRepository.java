package com.votingsystem.voting_system.repository;

import com.votingsystem.voting_system.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    
    List<Result> findByEventId(Long eventId);
    
    @Query("SELECT r FROM Result r WHERE r.event.id = :eventId ORDER BY r.generatedAt DESC")
    List<Result> findByEventIdOrderByGeneratedAtDesc(@Param("eventId") Long eventId);
    
    @Query("SELECT r FROM Result r ORDER BY r.generatedAt DESC")
    List<Result> findAllOrderByGeneratedAtDesc();
    
    @Query("SELECT COUNT(r) FROM Result r WHERE r.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT r FROM Result r WHERE r.event.id = :eventId AND r.generatedAt = (SELECT MAX(r2.generatedAt) FROM Result r2 WHERE r2.event.id = :eventId)")
    Optional<Result> findLatestResultByEventId(@Param("eventId") Long eventId);
}
