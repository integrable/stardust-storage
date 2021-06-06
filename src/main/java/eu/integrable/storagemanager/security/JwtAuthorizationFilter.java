package eu.integrable.storagemanager.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;


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

            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }

        } catch (JWTVerificationException ex) {
            System.out.println("Can not verify JWT token");
        }

        return null;
    }
}
