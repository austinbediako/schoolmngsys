package com.drakalabs.schoolmngsys.auth.config;

import com.drakalabs.schoolmngsys.shared.web.error.ProblemResponseWriter;
import com.drakalabs.schoolmngsys.shared.web.error.ProblemType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ProblemResponseWriter problemResponseWriter;

    public ProblemAuthenticationEntryPoint(ProblemResponseWriter problemResponseWriter) {
        this.problemResponseWriter = problemResponseWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        problemResponseWriter.write(request, response, ProblemType.AUTH_REQUIRED, "Authentication is required");
    }
}
