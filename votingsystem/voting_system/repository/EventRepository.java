package com.votingsystem.voting_system.repository;

import com.votingsystem.voting_system.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    @Query("SELECT e FROM Event e WHERE e.votingDeadline > :now ORDER BY e.date ASC")
    List<Event> findActiveEvents(@Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Event e WHERE e.votingDeadline <= :now ORDER BY e.date DESC")
    List<Event> findCompletedEvents(@Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Event e WHERE e.nominationDeadline > :now ORDER BY e.date ASC")
    List<Event> findEventsWithOpenNomination(@Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Event e WHERE e.nominationDeadline <= :now AND e.votingDeadline > :now ORDER BY e.date ASC")
    List<Event> findEventsWithOpenVoting(@Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Event e ORDER BY e.date ASC")
    List<Event> findAllOrderByDate();
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.votingDeadline > :now")
    long countActiveEvents(@Param("now") LocalDateTime now);
}
