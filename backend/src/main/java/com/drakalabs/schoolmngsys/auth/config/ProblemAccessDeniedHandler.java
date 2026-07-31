package com.drakalabs.schoolmngsys.auth.config;

import com.drakalabs.schoolmngsys.shared.web.error.ProblemResponseWriter;
import com.drakalabs.schoolmngsys.shared.web.error.ProblemType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemResponseWriter problemResponseWriter;

    public ProblemAccessDeniedHandler(ProblemResponseWriter problemResponseWriter) {
        this.problemResponseWriter = problemResponseWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        problemResponseWriter.write(
                request, response, ProblemType.FORBIDDEN, "You do not have permission to perform this action");
    }
}
