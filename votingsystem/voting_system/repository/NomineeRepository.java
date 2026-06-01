package com.votingsystem.voting_system.repository;

import com.votingsystem.voting_system.entity.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NomineeRepository extends JpaRepository<Nominee, Long> {
    
    @Query("SELECT n FROM Nominee n LEFT JOIN FETCH n.votes WHERE n.event.id = :eventId")
    List<Nominee> findByEventId(@Param("eventId") Long eventId);
    
    List<Nominee> findByEventIdAndCategory(Long eventId, String category);
    
    @Query("SELECT DISTINCT n.category FROM Nominee n WHERE n.event.id = :eventId")
    List<String> findDistinctCategoriesByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT n FROM Nominee n WHERE n.event.id = :eventId ORDER BY n.category, n.nomineeName")
    List<Nominee> findByEventIdOrderByCategoryAndName(@Param("eventId") Long eventId);
    
    @Query("SELECT COUNT(n) FROM Nominee n WHERE n.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT COUNT(n) FROM Nominee n WHERE n.event.id = :eventId AND n.category = :category")
    long countByEventIdAndCategory(@Param("eventId") Long eventId, @Param("category") String category);

    @Query("SELECT n FROM Nominee n WHERE n.event.id IN :eventIds ORDER BY n.event.date DESC, n.category, n.nomineeName")
    List<Nominee> findByEventIds(@Param("eventIds") List<Long> eventIds);

    List<Nominee> findByUserUsernameOrderByEventDateDescCategoryAscNomineeNameAsc(String username);
    
    @Query("SELECT n FROM Nominee n WHERE n.user.username = :username ORDER BY n.event.date DESC, n.category ASC, n.nomineeName ASC")
    List<Nominee> findByUsernameCustom(@Param("username") String username);
}
