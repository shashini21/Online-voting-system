package com.votingsystem.voting_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(nullable = false)
    private String venue;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "banner_url")
    private String bannerUrl;
    
    @Column(name = "nomination_deadline", nullable = false)
    private LocalDateTime nominationDeadline;
    
    @Column(name = "voting_deadline", nullable = false)
    private LocalDateTime votingDeadline;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Nominee> nominees;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vote> votes;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Result> results;
    
    public boolean isVotingOpen() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(votingDeadline) && now.isAfter(nominationDeadline);
    }
    
    public boolean isNominationOpen() {
        return LocalDateTime.now().isBefore(nominationDeadline);
    }
    
    public boolean isVotingClosed() {
        return LocalDateTime.now().isAfter(votingDeadline);
    }
}
