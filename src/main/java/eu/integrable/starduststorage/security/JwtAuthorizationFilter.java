package eu.integrable.starduststorage.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private JwtTokenService jwtTokenService;

    public JwtAuthorizationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws IOException, ServletException {
        // Get authorization header
        String headerEntry = httpServletRequest.getHeader("Authorization");

        // Filter if header not exists or is not Bearer,
        String token = "";
        if (headerEntry != null && headerEntry.startsWith("Bearer ")) {

            token = headerEntry.replace("Bearer ", "");
        } else {

            // Alternatively check token parameter and filter if not exist
            String parameterEntry = httpServletRequest.getParameter("token");
            if (parameterEntry != null) {
                token = parameterEntry;
            } else {

                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }
        }
        
        UsernamePasswordAuthenticationToken authentication = getAuthentication(token);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {

        try {
            // Decode token
            DecodedJWT decodedJWT = jwtTokenService.getDecodedJwt(token);

            // Get username
            String user = decodedJWT.getSubject();

            // Roles / authorities
            List<GrantedAuthority> authorities = new ArrayList<>();

            // Check if admin
            Boolean isAdmin = false;
            Claim claimIsAdmin = decodedJWT.getClaim("admin");
            if (!claimIsAdmin.isNull()) isAdmin = claimIsAdmin.asBoolean();
            if (isAdmin == true) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_WRITER"));
            }

            // Check if allow to upload
            Boolean isWriter = false;
            Claim claimIsWriter = decodedJWT.getClaim("writer");
            if (!claimIsWriter.isNull()) isWriter = claimIsWriter.asBoolean();
            if (isWriter == true) authorities.add(new SimpleGrantedAuthority("ROLE_WRITER"));

            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, authorities);
            }

        } catch (JWTVerificationException ex) {
            System.out.println("Can not verify JWT token");
        }

        return null;
    }
}
