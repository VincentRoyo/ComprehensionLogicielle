package com.example.tp3restructuration.Service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            String auth = req.getHeader("Authorization");

            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);

                String userId = jwtService.validateAndExtractUserId(token);
                String email = jwtService.extractEmail(token);

                MDC.put("userId", userId);
                MDC.put("email", email);
            } else {
                MDC.put("userId", "anonymous");
            }

            chain.doFilter(req, res);

        } finally {
            MDC.clear();
        }
    }
}
