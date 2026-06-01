package com.votingsystem.voting_system.observer;

import com.votingsystem.voting_system.entity.Vote;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.service.VoteService;
import com.votingsystem.voting_system.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Demo controller for testing the Observer Pattern implementation.
 * This controller provides endpoints to test vote operations and observer notifications.
 */
@Controller
public class ObserverPatternDemoController {
    
    @Autowired
    private VoteService voteService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private VoteSubjectImpl voteSubject;
    
    /**
     * Display the observer pattern demo page.
     */
    @GetMapping("/observer-demo")
    public String observerDemo(Model model) {
        model.addAttribute("observers", voteSubject.getObservers());
        model.addAttribute("observerCount", voteSubject.getObserverCount());
        model.addAttribute("events", eventService.getActiveEvents());
        return "observer/observer-demo";
    }
    
    /**
     * Test vote casting with observer notifications.
     */
    @PostMapping("/observer-demo/test-vote")
    @ResponseBody
    public Map<String, Object> testVoteCast(@RequestParam Long userId, 
                                           @RequestParam Long nomineeId, 
                                           @RequestParam Long eventId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Vote vote = voteService.castVote(userId, nomineeId, eventId);
            
            response.put("success", true);
            response.put("message", "Vote cast successfully and observers notified");
            response.put("voteId", vote.getId());
            response.put("observersNotified", voteSubject.getObserverCount());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error casting vote: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Test vote update with observer notifications.
     */
    @PostMapping("/observer-demo/test-update")
    @ResponseBody
    public Map<String, Object> testVoteUpdate(@RequestParam Long voteId, 
                                             @RequestParam Long newNomineeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Vote updatedVote = voteService.updateVote(voteId, newNomineeId);
            
            response.put("success", true);
            response.put("message", "Vote updated successfully and observers notified");
            response.put("voteId", updatedVote.getId());
            response.put("observersNotified", voteSubject.getObserverCount());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating vote: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Test vote deletion with observer notifications.
     */
    @PostMapping("/observer-demo/test-delete")
    @ResponseBody
    public Map<String, Object> testVoteDelete(@RequestParam Long voteId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            voteService.deleteVote(voteId);
            
            response.put("success", true);
            response.put("message", "Vote deleted successfully and observers notified");
            response.put("observersNotified", voteSubject.getObserverCount());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting vote: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Get observer information.
     */
    @GetMapping("/observer-demo/observers")
    @ResponseBody
    public Map<String, Object> getObservers() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("observerCount", voteSubject.getObserverCount());
        response.put("observers", voteSubject.getObservers().stream()
                .map(VoteObserver::getObserverName)
                .toArray());
        
        return response;
    }
    
    /**
     * Manually trigger observer notifications for testing.
     */
    @PostMapping("/observer-demo/trigger-notification")
    @ResponseBody
    public Map<String, Object> triggerNotification(@RequestParam String eventType,
                                                   @RequestParam Long eventId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                response.put("success", false);
                response.put("message", "Event not found");
                return response;
            }
            
            // Create a mock vote for testing
            Vote mockVote = new Vote();
            mockVote.setEvent(event);
            
            User mockUser = new User();
            mockUser.setUsername("test-user");
            
            // Trigger notification based on event type
            switch (eventType.toLowerCase()) {
                case "cast":
                    voteSubject.notifyVoteCast(mockVote, event, mockUser);
                    break;
                case "update":
                    voteSubject.notifyVoteUpdated(null, mockVote, event, mockUser);
                    break;
                case "delete":
                    voteSubject.notifyVoteDeleted(mockVote, event, mockUser);
                    break;
                default:
                    response.put("success", false);
                    response.put("message", "Invalid event type");
                    return response;
            }
            
            response.put("success", true);
            response.put("message", "Notification triggered successfully");
            response.put("observersNotified", voteSubject.getObserverCount());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error triggering notification: " + e.getMessage());
        }
        
        return response;
    }
}
