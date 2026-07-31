package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.SubjectQueryService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectQueryService subjectQueryService;

    public SubjectController(SubjectQueryService subjectQueryService) {
        this.subjectQueryService = subjectQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUBJECT_VIEW')")
    public List<SubjectResponse> list() {
        return subjectQueryService.list().stream().map(SubjectResponse::from).toList();
    }
}
