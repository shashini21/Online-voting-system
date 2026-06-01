package com.votingsystem.voting_system.repository;

import com.votingsystem.voting_system.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    
    List<Vote> findByUserId(Long userId);
    
    List<Vote> findByNomineeId(Long nomineeId);
    
    List<Vote> findByEventId(Long eventId);
    
    @Query("SELECT v FROM Vote v WHERE v.user.id = :userId AND v.event.id = :eventId")
    List<Vote> findByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);
    
    @Query("SELECT v FROM Vote v WHERE v.user.id = :userId AND v.nominee.id = :nomineeId")
    Optional<Vote> findByUserIdAndNomineeId(@Param("userId") Long userId, @Param("nomineeId") Long nomineeId);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.nominee.id = :nomineeId")
    long countByNomineeId(@Param("nomineeId") Long nomineeId);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.user.id = :userId AND v.event.id = :eventId")
    long countByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);
    
    @Query("SELECT v.nominee.category, COUNT(v) FROM Vote v WHERE v.event.id = :eventId GROUP BY v.nominee.category")
    List<Object[]> getVoteCountByCategoryForEvent(@Param("eventId") Long eventId);
}
