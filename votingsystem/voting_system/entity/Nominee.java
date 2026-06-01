package com.votingsystem.voting_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "nominees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nominee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @Column(nullable = false)
    private String category;
    
    @Column(name = "nominee_name", nullable = false)
    private String nomineeName;

    @Column(name = "description", length = 2000)
    private String description;

    // Public URL served via /uploads/** resource handler
    @Column(name = "media_url")
    private String mediaUrl;

    // image or video (simple discriminator)
    @Column(name = "media_type")
    private String mediaType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "nominee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vote> votes;
    
    public int getVoteCount() {
        return votes != null ? votes.size() : 0;
    }
}
