package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.service.GuardianLinkSpec;
import com.drakalabs.schoolmngsys.people.service.StudentAdmissionDetails;
import com.drakalabs.schoolmngsys.people.service.StudentGuardianLinkService;
import com.drakalabs.schoolmngsys.people.service.StudentQueryService;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageRequestFactory;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;
    private final StudentQueryService studentQueryService;
    private final StudentGuardianLinkService studentGuardianLinkService;

    public StudentController(
            StudentService studentService, StudentQueryService studentQueryService, StudentGuardianLinkService studentGuardianLinkService) {
        this.studentService = studentService;
        this.studentQueryService = studentQueryService;
        this.studentGuardianLinkService = studentGuardianLinkService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public PageResponse<StudentResponse> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestFactory.of(page, size);
        return PageResponse.from(studentQueryService.list(pageable).map(StudentResponse::from));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public StudentResponse get(@PathVariable UUID id) {
        return StudentResponse.from(studentQueryService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT_CREATE')")
    public StudentResponse create(@RequestBody @Valid CreateStudentRequest request) {
        List<GuardianLinkSpec> links =
                request.guardianLinks().stream()
                        .map(
                                link -> new GuardianLinkSpec(
                                        link.guardianId(),
                                        link.relationshipType(),
                                        link.primaryContact(),
                                        link.hasCustody(),
                                        link.receivesBilling(),
                                        link.receivesAcademicReports()))
                        .toList();
        StudentAdmissionDetails admissionDetails = new StudentAdmissionDetails(
                request.nationality(),
                request.previousSchool(),
                request.residentialAddress(),
                request.emergencyContactName(),
                request.emergencyContactPhone(),
                request.emergencyContactRelationship());
        return StudentResponse.from(
                studentService.createStudent(
                        request.firstName(),
                        request.lastName(),
                        request.otherNames(),
                        request.dateOfBirth(),
                        request.gender(),
                        request.admissionDate(),
                        links,
                        admissionDetails));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public StudentResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateStudentBioRequest request) {
        return StudentResponse.from(studentService.updateBio(id, request.firstName(), request.lastName(), request.otherNames()));
    }

    @PutMapping("/{id}/admission-details")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public StudentResponse updateAdmissionDetails(@PathVariable UUID id, @RequestBody UpdateStudentAdmissionDetailsRequest request) {
        StudentAdmissionDetails details = new StudentAdmissionDetails(
                request.nationality(),
                request.previousSchool(),
                request.residentialAddress(),
                request.emergencyContactName(),
                request.emergencyContactPhone(),
                request.emergencyContactRelationship());
        return StudentResponse.from(studentService.updateAdmissionDetails(id, details));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_ARCHIVE')")
    public StudentResponse archive(@PathVariable UUID id) {
        return StudentResponse.from(studentService.archiveStudent(id));
    }

    @GetMapping("/{id}/guardians")
    @PreAuthorize("hasAuthority('GUARDIAN_VIEW')")
    public List<StudentGuardianResponse> listGuardians(@PathVariable UUID id) {
        return studentQueryService.listGuardianLinks(id).stream().map(StudentGuardianResponse::from).toList();
    }

    @PostMapping("/{id}/guardians")
    @PreAuthorize("hasAuthority('GUARDIAN_LINK_MANAGE')")
    public StudentGuardianResponse linkGuardian(@PathVariable UUID id, @RequestBody @Valid LinkGuardianRequest request) {
        return StudentGuardianResponse.from(
                studentGuardianLinkService.link(
                        id,
                        request.guardianId(),
                        request.relationshipType(),
                        request.primaryContact(),
                        request.hasCustody(),
                        request.receivesBilling(),
                        request.receivesAcademicReports()));
    }

    @DeleteMapping("/{id}/guardians/{guardianId}")
    @PreAuthorize("hasAuthority('GUARDIAN_LINK_MANAGE')")
    public void unlinkGuardian(@PathVariable UUID id, @PathVariable UUID guardianId) {
        studentGuardianLinkService.unlink(id, guardianId);
    }
}
