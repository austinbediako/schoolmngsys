package com.drakalabs.schoolmngsys.communication.api;

import com.drakalabs.schoolmngsys.communication.domain.MessageCategory;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTemplateRequest(
        @NotBlank(message = "Template code is required") String templateCode,
        @NotBlank(message = "Name is required") String name,
        @NotNull(message = "Channel is required") MessageChannel channel,
        @NotNull(message = "Category is required") MessageCategory category,
        String subjectTemplate,
        @NotBlank(message = "Body template is required") String bodyTemplate,
        Boolean active
) {}
