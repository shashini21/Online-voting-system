package com.votingsystem.voting_system.config;

import com.votingsystem.voting_system.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        User user = (User) authentication.getPrincipal();
        
        // Check if there's a redirect parameter
        String redirectUrl = request.getParameter("redirect");
        
        // If no redirect parameter, use default role-based redirect
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            switch (user.getRole()) {
                case ADMIN:
                    redirectUrl = "/admin/dashboard";
                    break;
                case VOTER:
                    redirectUrl = "/voter/dashboard";
                    break;
                case NOMINEE:
                    redirectUrl = "/nominee/dashboard";
                    break;
                case SUPPORT:
                    redirectUrl = "/support/dashboard";
                    break;
                default:
                    redirectUrl = "/dashboard";
                    break;
            }
        }
        
        response.sendRedirect(redirectUrl);
    }
}
