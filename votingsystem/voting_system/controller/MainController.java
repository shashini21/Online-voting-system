package com.votingsystem.voting_system.controller;

import com.votingsystem.voting_system.service.EventService;
import com.votingsystem.voting_system.service.ResultService;
import com.votingsystem.voting_system.repository.NomineeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    private EventService eventService;

    @Autowired
    private ResultService resultService;

    @Autowired
    private NomineeRepository nomineeRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("events", eventService.getActiveEvents());
        return "home";
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("events", eventService.getActiveEvents());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/login";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/results")
    public String publicResults(Model model) {
        try {
            // Get all events with their results
            var events = eventService.getAllEvents();
            var results = resultService.getAllResults();

            // Get vote counts for each event
            var voteCountsByEvent = new java.util.HashMap<Long, Long>();
            var nomineeCountsByEvent = new java.util.HashMap<Long, Integer>();
            var nomineeVotesByEvent = new java.util.HashMap<Long, java.util.Map<String, Long>>();

            if (events != null) {
                for (var event : events) {
                    if (event != null) {
                        // Get vote counts for this event
                        var voteCountsByNominee = resultService.getVoteCountsByNominee(event.getId());
                        var totalVotes = voteCountsByNominee != null ?
                                voteCountsByNominee.values().stream().mapToLong(Long::longValue).sum() : 0L;

                        voteCountsByEvent.put(event.getId(), totalVotes);

                        // Get nominee counts using NomineeRepository
                        var nominees = nomineeRepository.findByEventId(event.getId());
                        nomineeCountsByEvent.put(event.getId(), nominees != null ? nominees.size() : 0);

                        // Get nominee vote details
                        nomineeVotesByEvent.put(event.getId(), voteCountsByNominee);
                    }
                }
            }

            model.addAttribute("events", events);
            model.addAttribute("results", results);
            model.addAttribute("voteCountsByEvent", voteCountsByEvent);
            model.addAttribute("nomineeCountsByEvent", nomineeCountsByEvent);
            model.addAttribute("nomineeVotesByEvent", nomineeVotesByEvent);

            return "public-results";

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while loading results: " + e.getMessage());
            return "error";
        }
    }
}
