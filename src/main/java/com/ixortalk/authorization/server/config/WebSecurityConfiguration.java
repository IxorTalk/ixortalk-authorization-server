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

import com.ixortalk.authorization.server.domain.LoginProvider;
import com.ixortalk.authorization.server.security.IxorTalkPrincipal;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CompositeFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

import javax.inject.Inject;
import javax.servlet.Filter;

import static java.util.stream.Collectors.toList;
import static org.springframework.boot.autoconfigure.security.SecurityProperties.ACCESS_OVERRIDE_ORDER;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
@EnableWebSecurity
@EnableOAuth2Client
@Order(ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Inject
    private IxorTalkConfigProperties ixorTalkConfigProperties;

    @Inject
    private OAuth2ClientContext oAuth2ClientContext;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**").authorizeRequests()
                .antMatchers("/login", "/actuator/**").permitAll()
                .anyRequest().authenticated()
                .and()
                    .exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(ixorTalkConfigProperties.getSecurity().getLoginUrl()))
                .and()
                    .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessHandler(logoutSuccessHandler())
                .and()
                    .addFilterBefore(createCompositeSSOFilter(), BasicAuthenticationFilter.class);
    }

    private LogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler logoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        logoutSuccessHandler.setDefaultTargetUrl(ixorTalkConfigProperties.getLogout().getDefaultRedirectUri());
        logoutSuccessHandler.setTargetUrlParameter(ixorTalkConfigProperties.getLogout().getRedirectUriParamName());
        return logoutSuccessHandler;
    }

    private CompositeFilter createCompositeSSOFilter() {
        CompositeFilter filter = new CompositeFilter();
        filter.setFilters(
                ixorTalkConfigProperties
                        .getThirdPartyLogins()
                        .values()
                        .stream()
                        .map(thirdPartyLogin -> ssoFilter(thirdPartyLogin.getPrincipalExtractorType(), thirdPartyLogin.getClientResource(), thirdPartyLogin.getLoginPath()))
                        .collect(toList())
        );
        return filter;
    }

    private Filter ssoFilter(LoginProvider principalExtractorType, IxorTalkConfigProperties.ClientResources client, String path) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(client.getClient(), oAuth2ClientContext);
        filter.setRestTemplate(oAuth2RestTemplate);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(
                client.getResource().getUserInfoUri(),
                client.getClient().getClientId());
        tokenServices.setRestTemplate(oAuth2RestTemplate);
        tokenServices.setPrincipalExtractor(createPrincipalExtractor(principalExtractorType, oAuth2RestTemplate));
        filter.setTokenServices(tokenServices);
        return filter;
    }

    private PrincipalExtractor createPrincipalExtractor(LoginProvider principalExtractorType, OAuth2RestTemplate userInfoProviderRestTemplate) {
        return map -> new IxorTalkPrincipal(
                principalExtractorType,
                principalExtractorType.getPrincipalName(map),
                principalExtractorType.getPrincipalName(map),
                principalExtractorType.getFirstName(map),
                principalExtractorType.getLastName(map),
                principalExtractorType.getProfilePictureUrl(map, userInfoProviderRestTemplate),
                principalExtractorType.getUserInfo(map));
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter oAuth2ClientContextFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(oAuth2ClientContextFilter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    FilterRegistrationBean forwardedHeaderFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
