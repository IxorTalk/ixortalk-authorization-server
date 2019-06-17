package com.ixortalk.authorization.server.security;

import com.ixortalk.authorization.server.domain.LoginProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UrlLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private final RedirectStrategy redirectStrategy;

    private final String redirectUriIxortalkLogout;

    public UrlLogoutSuccessHandler(String redirectUriIxortalkLogout) {
        this.redirectUriIxortalkLogout = redirectUriIxortalkLogout;
        this.redirectStrategy = new DefaultRedirectStrategy();
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response);

        if (response.isCommitted()) {
            this.logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        if (authentication.getPrincipal() instanceof IxorTalkPrincipal && ((IxorTalkPrincipal) authentication.getPrincipal()).getLoginProvider() == LoginProvider.IXORTALK) {
            if (getTargetUrlParameter() != null) {
                targetUrl = redirectUriIxortalkLogout + "?" + getTargetUrlParameter() + "=" + targetUrl;
            }
        }

        this.redirectStrategy.sendRedirect(request, response, targetUrl);
    }
}
