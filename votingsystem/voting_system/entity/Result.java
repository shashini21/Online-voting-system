package com.votingsystem.voting_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @Column(name = "total_votes", nullable = false)
    private Integer totalVotes;
    
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "published", nullable = false)
    private Boolean published = false;
    
    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
