package com.votingsystem.voting_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UploadsController {

    private final Path uploadRoot = Paths.get("uploads");

    @GetMapping("/uploads")
    public String listUploads(Model model) throws IOException {
        if (!Files.exists(uploadRoot)) {
            Files.createDirectories(uploadRoot);
        }
        List<String> files = Files.list(uploadRoot)
                .filter(Files::isRegularFile)
                .map(path -> "/uploads/" + path.getFileName().toString())
                .collect(Collectors.toList());
        model.addAttribute("files", files);
        return "uploads/index";
    }
}


