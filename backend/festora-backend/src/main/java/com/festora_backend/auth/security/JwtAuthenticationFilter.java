
package com.festora_backend.auth.security;

import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.isValid(token)
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                try {
                    Claims claims = jwtService.extractClaims(token);

                    Long userId = Long.valueOf(claims.getSubject());
                    Object tokenVersionClaim = claims.get("tokenVersion");

                    if (!(tokenVersionClaim instanceof Number tokenVersion)) {
                        response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED
                        );
                        return;
                    }

                    User user = userRepository.findById(userId)
                            .orElse(null);

                    if (user == null
                            || !user.isEnabled()
                            || user.getStatus() != AccountStatus.ACTIVE
                            || user.getTokenVersion() != tokenVersion.longValue()) {
                        response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED
                        );
                        return;
                    }

                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(
                                    user.getEmail()
                            );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                } catch (RuntimeException exception) {
                    SecurityContextHolder.clearContext();
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED
                    );
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
