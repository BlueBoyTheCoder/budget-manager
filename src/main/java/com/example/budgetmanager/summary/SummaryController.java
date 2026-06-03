package com.example.budgetmanager.summary;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public SummaryDto getSummary() {
        return summaryService.getSummary();
    }
}