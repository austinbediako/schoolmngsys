package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.service.FeeItemSpec;
import com.drakalabs.schoolmngsys.finance.service.FeeScheduleService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeeScheduleController {

    private final FeeScheduleService feeScheduleService;

    public FeeScheduleController(FeeScheduleService feeScheduleService) {
        this.feeScheduleService = feeScheduleService;
    }

    @PostMapping("/api/v1/fee-schedules")
    @PreAuthorize("hasAuthority('FEE_SCHEDULE_MANAGE')")
    public FeeScheduleResponse create(@RequestBody @Valid CreateFeeScheduleRequest request) {
        var items = request.items().stream().map(item -> new FeeItemSpec(item.name(), item.amount(), item.mandatory())).toList();
        return FeeScheduleResponse.from(feeScheduleService.create(request.classLevelId(), request.termId(), items));
    }

    @PostMapping("/api/v1/fee-schedules/{id}/approve")
    @PreAuthorize("hasAuthority('FEE_SCHEDULE_APPROVE')")
    public FeeScheduleResponse approve(@PathVariable UUID id) {
        return FeeScheduleResponse.from(feeScheduleService.approve(id));
    }

    @GetMapping("/api/v1/fee-schedules/{id}")
    @PreAuthorize("hasAuthority('FEE_SCHEDULE_VIEW')")
    public FeeScheduleResponse get(@PathVariable UUID id) {
        return FeeScheduleResponse.from(feeScheduleService.get(id));
    }
}
