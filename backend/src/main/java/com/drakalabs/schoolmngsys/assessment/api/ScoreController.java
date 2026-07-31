package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.service.ScoreEntry;
import com.drakalabs.schoolmngsys.assessment.service.ScoreService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PostMapping("/api/v1/assessment-components/{id}/scores")
    @PreAuthorize("hasAuthority('SCORE_ENTER')")
    public List<ScoreResponse> enterScores(@PathVariable UUID id, @RequestBody @Valid EnterScoresRequest request) {
        List<ScoreEntry> entries =
                request.entries().stream()
                        .map(e -> new ScoreEntry(e.enrollmentId(), e.rawScore(), e.exempted(), e.naReason()))
                        .toList();
        return scoreService.enterScoresBulk(id, entries).stream().map(ScoreResponse::from).toList();
    }
}
