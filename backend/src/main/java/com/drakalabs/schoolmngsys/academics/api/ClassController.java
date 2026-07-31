package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassService;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageRequestFactory;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassController {

    private final ClassService classService;
    private final ClassQueryService classQueryService;

    public ClassController(ClassService classService, ClassQueryService classQueryService) {
        this.classService = classService;
        this.classQueryService = classQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLASS_VIEW')")
    public PageResponse<ClassResponse> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestFactory.of(page, size);
        return PageResponse.from(classQueryService.list(pageable).map(ClassResponse::from));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLASS_CREATE')")
    public ClassResponse create(@RequestBody @Valid CreateClassRequest request) {
        return ClassResponse.from(classService.createClass(request.classLevelCode(), request.stream(), request.capacity()));
    }

    @PostMapping("/{id}/class-teacher")
    @PreAuthorize("hasAuthority('TEACHER_ASSIGNMENT_MANAGE')")
    public ClassTeacherAssignmentResponse assignClassTeacher(
            @PathVariable UUID id, @RequestBody @Valid AssignClassTeacherRequest request) {
        return ClassTeacherAssignmentResponse.from(
                classService.assignClassTeacher(id, request.academicYearId(), request.teacherStaffId()));
    }
}
