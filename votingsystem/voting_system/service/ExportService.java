package com.votingsystem.voting_system.service;

import com.votingsystem.voting_system.entity.Result;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.service.ResultService.NomineeVoteCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private ResultService resultService;

    public byte[] exportResultsToPdf(Long resultId) throws IOException {
        Result result = resultService.getResultById(resultId).orElse(null);
        if (result == null) {
            throw new IllegalArgumentException("Result not found");
        }

        Event event = result.getEvent();
        List<NomineeVoteCount> nomineeVoteCounts = resultService.getNomineeVoteCounts(event.getId());

        StringBuilder content = new StringBuilder();
        content.append("VOTING RESULTS REPORT\n");
        content.append("====================\n\n");
        content.append("Event: ").append(event.getTitle()).append("\n");
        content.append("Date: ").append(event.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        content.append("Venue: ").append(event.getVenue()).append("\n");
        content.append("Total Votes: ").append(result.getTotalVotes()).append("\n");
        content.append("Generated: ").append(result.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");

        if (nomineeVoteCounts != null && !nomineeVoteCounts.isEmpty()) {
            content.append("VOTING RESULTS:\n");
            content.append("Rank\tNominee\t\tCategory\t\tVotes\n");
            content.append("----\t-------\t\t--------\t\t-----\n");
            
            for (int i = 0; i < nomineeVoteCounts.size(); i++) {
                NomineeVoteCount nominee = nomineeVoteCounts.get(i);
                content.append(String.format("%d\t%s\t\t%s\t\t%d\n", 
                    i + 1, 
                    nominee.getNomineeName(), 
                    nominee.getCategory(), 
                    nominee.getVoteCount()));
            }
        }

        return content.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportResultsToExcel(Long resultId) throws IOException {
        Result result = resultService.getResultById(resultId).orElse(null);
        if (result == null) {
            throw new IllegalArgumentException("Result not found");
        }

        Event event = result.getEvent();
        List<NomineeVoteCount> nomineeVoteCounts = resultService.getNomineeVoteCounts(event.getId());

        StringBuilder content = new StringBuilder();
        content.append("Event,").append(event.getTitle()).append("\n");
        content.append("Date,").append(event.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        content.append("Venue,").append(event.getVenue()).append("\n");
        content.append("Total Votes,").append(result.getTotalVotes()).append("\n");
        content.append("Generated,").append(result.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        content.append("Rank,Nominee,Category,Votes\n");

        if (nomineeVoteCounts != null && !nomineeVoteCounts.isEmpty()) {
            for (int i = 0; i < nomineeVoteCounts.size(); i++) {
                NomineeVoteCount nominee = nomineeVoteCounts.get(i);
                content.append(String.format("%d,%s,%s,%d\n", 
                    i + 1, 
                    nominee.getNomineeName(), 
                    nominee.getCategory(), 
                    nominee.getVoteCount()));
            }
        }

        return content.toString().getBytes(StandardCharsets.UTF_8);
    }
}
