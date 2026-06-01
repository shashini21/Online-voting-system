package com.votingsystem.voting_system.repository;

import com.votingsystem.voting_system.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    List<Ticket> findByUserId(Long userId);
    
    List<Ticket> findByStatus(Ticket.Status status);
    
    @Query("SELECT t FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<Ticket> findByStatusOrderByCreatedAtDesc(@Param("status") Ticket.Status status);
    
    @Query("SELECT t FROM Ticket t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    List<Ticket> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Ticket t ORDER BY t.createdAt DESC")
    List<Ticket> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    long countByStatus(@Param("status") Ticket.Status status);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
