package it.baldilorenzo.hibernate_envers_101.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the user from the X-User header and publishes it into the ThreadLocal
 * so the RevisionListener can write it into REVINFO.
 * In a real application this would come from Spring Security's SecurityContextHolder.
 */
@Component
public class AuditUserFilter extends OncePerRequestFilter {

    public static final String USER_HEADER = "X-User";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String user = request.getHeader(USER_HEADER);
            if (user != null && !user.isBlank()) {
                AuditUserContext.setUser(user);
            }
            filterChain.doFilter(request, response);
        } finally {
            AuditUserContext.clear();
        }
    }
}
