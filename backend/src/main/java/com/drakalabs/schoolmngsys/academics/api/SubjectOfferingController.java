package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.SubjectOfferingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubjectOfferingController {

    private final SubjectOfferingService subjectOfferingService;

    public SubjectOfferingController(SubjectOfferingService subjectOfferingService) {
        this.subjectOfferingService = subjectOfferingService;
    }

    @GetMapping("/api/v1/classes/{classId}/subject-offerings")
    @PreAuthorize("hasAuthority('SUBJECT_OFFERING_MANAGE')")
    public List<ClassSubjectOfferingResponse> listOfferings(
            @PathVariable UUID classId, @RequestParam(required = false) UUID academicYearId) {
        if (academicYearId == null) {
            return List.of();
        }
        return subjectOfferingService.listOfferings(classId, academicYearId).stream()
                .map(ClassSubjectOfferingResponse::from)
                .toList();
    }

    @PostMapping("/api/v1/classes/{classId}/subject-offerings")
    @PreAuthorize("hasAuthority('SUBJECT_OFFERING_MANAGE')")
    public ClassSubjectOfferingResponse create(
            @PathVariable UUID classId, @RequestBody @Valid CreateSubjectOfferingRequest request) {
        return ClassSubjectOfferingResponse.from(
                subjectOfferingService.createOffering(classId, request.subjectId(), request.academicYearId()));
    }

    @PostMapping("/api/v1/subject-offerings/{id}/teacher")
    @PreAuthorize("hasAuthority('TEACHER_ASSIGNMENT_MANAGE')")
    public ClassSubjectOfferingResponse assignTeacher(
            @PathVariable UUID id, @RequestBody @Valid AssignSubjectTeacherRequest request) {
        return ClassSubjectOfferingResponse.from(subjectOfferingService.assignTeacher(id, request.teacherStaffId()));
    }
}
