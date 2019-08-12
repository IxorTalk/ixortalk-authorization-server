/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-present IxorTalk CVBA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ixortalk.authorization.server.config;

import com.ixortalk.authorization.server.security.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Inject
    private IxorTalkConfigProperties ixorTalkConfigProperties;

    @Inject
    private DataSource dataSource;

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Bean
    public TokenStore thirdPartyTokenStore() {
        JdbcTokenStore thirdPartyTokenStore = new JdbcTokenStore(dataSource);
        thirdPartyTokenStore.setInsertAccessTokenSql("insert into third_pty_oauth_access_token (token_id, token, authentication_id, user_name, client_id, authentication, refresh_token) values (?, ?, ?, ?, ?, ?, ?)");
        thirdPartyTokenStore.setSelectAccessTokenSql("select token_id, token from third_pty_oauth_access_token where token_id = ?");
        thirdPartyTokenStore.setSelectAccessTokenAuthenticationSql("select token_id, authentication from third_pty_oauth_access_token where token_id = ?");
        thirdPartyTokenStore.setSelectAccessTokenFromAuthenticationSql("select token_id, token from third_pty_oauth_access_token where authentication_id = ?");
        thirdPartyTokenStore.setSelectAccessTokensFromUserNameAndClientIdSql("select token_id, token from third_pty_oauth_access_token where user_name = ? and client_id = ?");
        thirdPartyTokenStore.setSelectAccessTokensFromUserNameSql("select token_id, token from third_pty_oauth_access_token where user_name = ?");
        thirdPartyTokenStore.setSelectAccessTokensFromClientIdSql("select token_id, token from third_pty_oauth_access_token where client_id = ?");
        thirdPartyTokenStore.setDeleteAccessTokenSql("delete from third_pty_oauth_access_token where token_id = ?");
        thirdPartyTokenStore.setDeleteAccessTokenFromRefreshTokenSql("delete from third_pty_oauth_access_token where refresh_token = ?");
        thirdPartyTokenStore.setInsertRefreshTokenSql("insert into third_pty_oauth_refresh_token (token_id, token, authentication) values (?, ?, ?)");
        thirdPartyTokenStore.setSelectRefreshTokenSql("select token_id, token from third_pty_oauth_refresh_token where token_id = ?");
        thirdPartyTokenStore.setSelectRefreshTokenAuthenticationSql("select token_id, authentication from third_pty_oauth_refresh_token where token_id = ?");
        thirdPartyTokenStore.setDeleteRefreshTokenSql("delete from third_pty_oauth_refresh_token where token_id = ?");
        return thirdPartyTokenStore;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .tokenStore(tokenStore())
                .userDetailsService(new UserDetailsService())
                // See https://github.com/spring-projects/spring-security-oauth/issues/140
                .addInterceptor(new HandlerInterceptorAdapter() {
                    @Override
                    public void postHandle(HttpServletRequest request,
                                           HttpServletResponse response, Object handler,
                                           ModelAndView modelAndView) throws Exception {
                        if (modelAndView != null
                                && modelAndView.getView() instanceof RedirectView) {
                            RedirectView redirect = (RedirectView) modelAndView.getView();
                            String url = redirect.getUrl();
                            if (url.contains("code=") || url.contains("error=")) {
                                HttpSession session = request.getSession(false);
                                if (session != null) {
                                    session.invalidate();
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory();
        ixorTalkConfigProperties.getSecurity().getAuthentication().getOauthClients()
                .values()
                .stream()
                .forEach(client ->
                        clients
                                .and()
                                .withClient(client.getClientid())
                                .scopes(client.getScopes().toArray(new String[]{}))
                                .authorities(client.getAuthorities().toArray(new String[]{}))
                                .authorizedGrantTypes(client.getAuthorizedGrantTypes().toArray(new String[]{}))
                                .autoApprove(client.getAutoApproveScopes().toArray(new String[]{}))
                                .secret(client.getSecret())
                                .accessTokenValiditySeconds(client.getTokenValidityInSeconds())
                                .redirectUris(ixorTalkConfigProperties.getSecurity().getAuthentication().getDefaultRedirectUris().toArray(new String[0]))
                                .redirectUris(client.getRedirectUris().toArray(new String[0]))
                );
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer.allowFormAuthenticationForClients();
    }
}
