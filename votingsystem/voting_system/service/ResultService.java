package com.votingsystem.voting_system.service;

import com.votingsystem.voting_system.entity.*;
import com.votingsystem.voting_system.repository.ResultRepository;
import com.votingsystem.voting_system.repository.NomineeRepository;
import com.votingsystem.voting_system.repository.VoteRepository;
import com.votingsystem.voting_system.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ResultService {
    
    @Autowired
    private ResultRepository resultRepository;
    
    @Autowired
    private VoteRepository voteRepository;
    
    @Autowired
    private NomineeRepository nomineeRepository;
    
    @Autowired
    private EventService eventService;
    
    public Result generateResults(Long eventId) {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        
        if (!event.isVotingClosed()) {
            throw new RuntimeException("Cannot generate results while voting is still open");
        }
        
        // Calculate total votes for the event
        long totalVotes = voteRepository.countByEventId(eventId);
        
        // Get vote counts by category (for future use)
        // List<Object[]> voteCountsByCategory = voteRepository.getVoteCountByCategoryForEvent(eventId);
        
        // Create result details
        StringBuilder details = new StringBuilder();
        details.append("Event: ").append(event.getTitle()).append("\n");
        details.append("Total Votes: ").append(totalVotes).append("\n\n");
        
        // Get nominees by category
        List<String> categories = nomineeRepository.findDistinctCategoriesByEventId(eventId);
        for (String category : categories) {
            details.append("Category: ").append(category).append("\n");
            List<Nominee> nominees = nomineeRepository.findByEventIdAndCategory(eventId, category);
            for (Nominee nominee : nominees) {
                long voteCount = voteRepository.countByNomineeId(nominee.getId());
                details.append("- ").append(nominee.getNomineeName())
                       .append(": ").append(voteCount).append(" votes\n");
            }
            details.append("\n");
        }
        
        // Create and save result
        Result result = new Result();
        result.setEvent(event);
        result.setTotalVotes((int) totalVotes);
        result.setDetails(details.toString());
        
        return resultRepository.save(result);
    }
    
    public List<Result> getResultsByEvent(Long eventId) {
        return resultRepository.findByEventIdOrderByGeneratedAtDesc(eventId);
    }
    
    public List<Result> getAllResults() {
        return resultRepository.findAllOrderByGeneratedAtDesc();
    }
    
    public Result getLatestResultByEvent(Long eventId) {
        return resultRepository.findLatestResultByEventId(eventId).orElse(null);
    }
    
    public long countResultsByEvent(Long eventId) {
        return resultRepository.countByEventId(eventId);
    }
    
    public Map<String, Long> getVoteCountsByNominee(Long eventId) {
        if (eventId == null) {
            return new HashMap<>();
        }
        
        try {
            List<Nominee> nominees = nomineeRepository.findByEventId(eventId);
            Map<String, Long> voteCounts = new HashMap<>();
            
            if (nominees != null) {
                for (Nominee nominee : nominees) {
                    if (nominee != null && nominee.getId() != null && nominee.getNomineeName() != null) {
                        long voteCount = voteRepository.countByNomineeId(nominee.getId());
                        voteCounts.put(nominee.getNomineeName(), voteCount);
                    }
                }
            }
            
            return voteCounts;
        } catch (Exception e) {
            System.err.println("Error getting vote counts by nominee for event " + eventId + ": " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    public Map<String, Map<String, Long>> getVoteCountsByCategoryAndNominee(Long eventId) {
        List<String> categories = nomineeRepository.findDistinctCategoriesByEventId(eventId);
        Map<String, Map<String, Long>> results = new HashMap<>();
        
        for (String category : categories) {
            List<Nominee> nominees = nomineeRepository.findByEventIdAndCategory(eventId, category);
            Map<String, Long> categoryResults = new HashMap<>();
            
            for (Nominee nominee : nominees) {
                long voteCount = voteRepository.countByNomineeId(nominee.getId());
                categoryResults.put(nominee.getNomineeName(), voteCount);
            }
            
            results.put(category, categoryResults);
        }
        
        return results;
    }
    
    public List<NomineeVoteCount> getNomineeVoteCounts(Long eventId) {
        if (eventId == null) {
            return new ArrayList<>();
        }
        
        try {
            List<Nominee> nominees = nomineeRepository.findByEventId(eventId);
            List<NomineeVoteCount> results = new ArrayList<>();
            
            if (nominees != null) {
                System.out.println("Found " + nominees.size() + " nominees for event " + eventId);
                
                for (Nominee nominee : nominees) {
                    if (nominee != null && nominee.getId() != null && nominee.getNomineeName() != null) {
                        // Use the vote count from the repository for accuracy
                        long voteCount = voteRepository.countByNomineeId(nominee.getId());
                        System.out.println("Nominee: " + nominee.getNomineeName() + ", Category: " + nominee.getCategory() + ", Votes: " + voteCount);
                        results.add(new NomineeVoteCount(nominee.getNomineeName(), nominee.getCategory(), voteCount));
                    }
                }
                
                // Sort by vote count descending
                results.sort((a, b) -> Long.compare(b.getVoteCount(), a.getVoteCount()));
            }
            
            return results;
        } catch (Exception e) {
            System.err.println("Error getting nominee vote counts for event " + eventId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    // Inner class to hold nominee vote count data
    public static class NomineeVoteCount {
        private String nomineeName;
        private String category;
        private long voteCount;
        
        public NomineeVoteCount(String nomineeName, String category, long voteCount) {
            this.nomineeName = nomineeName;
            this.category = category;
            this.voteCount = voteCount;
        }
        
        // Getters
        public String getNomineeName() { return nomineeName; }
        public String getCategory() { return category; }
        public long getVoteCount() { return voteCount; }
    }

    public void deleteResult(Long resultId) {
        resultRepository.deleteById(resultId);
    }
    
    public void publishResults(Long eventId) {
        Result result = getLatestResultByEvent(eventId);
        if (result == null) {
            throw new RuntimeException("No results found for this event. Please generate results first.");
        }
        
        if (result.getPublished()) {
            throw new RuntimeException("Results for this event are already published");
        }
        
        // Mark the result as published
        result.setPublished(true);
        resultRepository.save(result);
        
        System.out.println("Results published successfully for event ID: " + eventId);
    }
    
    public Optional<Result> getResultById(Long resultId) {
        return resultRepository.findById(resultId);
    }
}
