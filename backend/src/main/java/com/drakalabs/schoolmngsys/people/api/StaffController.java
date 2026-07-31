package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.service.StaffQueryService;
import com.drakalabs.schoolmngsys.people.service.StaffService;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageRequestFactory;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffService staffService;
    private final StaffQueryService staffQueryService;

    public StaffController(StaffService staffService, StaffQueryService staffQueryService) {
        this.staffService = staffService;
        this.staffQueryService = staffQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STAFF_VIEW')")
    public PageResponse<StaffResponse> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestFactory.of(page, size);
        return PageResponse.from(staffQueryService.list(pageable).map(StaffResponse::from));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF_VIEW')")
    public StaffResponse get(@PathVariable UUID id) {
        return StaffResponse.from(staffQueryService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STAFF_CREATE')")
    public StaffResponse create(@RequestBody @Valid CreateStaffRequest request) {
        return StaffResponse.from(
                staffService.createStaff(
                        request.staffNumber(),
                        request.firstName(),
                        request.lastName(),
                        request.staffType(),
                        request.gesRegistrationNumber(),
                        request.employmentStartDate()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF_UPDATE')")
    public StaffResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateStaffBioRequest request) {
        return StaffResponse.from(staffService.updateBio(id, request.firstName(), request.lastName(), request.gesRegistrationNumber()));
    }

    @PostMapping("/{id}/end-employment")
    @PreAuthorize("hasAuthority('STAFF_END_EMPLOYMENT')")
    public StaffResponse endEmployment(@PathVariable UUID id, @RequestBody @Valid EndEmploymentRequest request) {
        return StaffResponse.from(staffService.endEmployment(id, request.endDate()));
    }

    @GetMapping("/{id}/qualifications")
    @PreAuthorize("hasAuthority('STAFF_VIEW')")
    public List<StaffQualificationResponse> listQualifications(@PathVariable UUID id) {
        return staffQueryService.listQualifications(id).stream().map(StaffQualificationResponse::from).toList();
    }

    @PostMapping("/{id}/qualifications")
    @PreAuthorize("hasAuthority('STAFF_UPDATE')")
    public StaffQualificationResponse addQualification(@PathVariable UUID id, @RequestBody @Valid AddQualificationRequest request) {
        return StaffQualificationResponse.from(
                staffService.addQualification(id, request.qualification(), request.institution(), request.yearObtained()));
    }
}
