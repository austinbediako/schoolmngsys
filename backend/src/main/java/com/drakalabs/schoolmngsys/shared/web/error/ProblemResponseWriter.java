package com.drakalabs.schoolmngsys.shared.web.error;

import com.drakalabs.schoolmngsys.shared.web.trace.TraceIdHolder;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/**
 * Writes an RFC 7807 problem directly onto the response — for the two failure points that run
 * before {@code DispatcherServlet} dispatch (Spring Security's entry point / access-denied
 * handler) and so can never reach {@link GlobalExceptionHandler}'s {@code @ExceptionHandler}s.
 */
@Component
public class ProblemResponseWriter {

    private final ObjectMapper objectMapper;

    public ProblemResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, ProblemType type, String detail)
            throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(type.defaultStatus(), detail);
        problem.setType(type.uri());
        problem.setTitle(type.title());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("traceId", TraceIdHolder.current());

        response.setStatus(type.defaultStatus().value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
