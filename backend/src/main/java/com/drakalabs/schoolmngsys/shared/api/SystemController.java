package com.drakalabs.schoolmngsys.shared.api;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @GetMapping("/status")
    public SystemStatusResponse getStatus() {
        return new SystemStatusResponse("UBS-LMIS", "0.0.1-SNAPSHOT", "UP", Instant.now());
    }
}
