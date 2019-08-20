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
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.ixortalk.authorization.server.domain.LoginProvider.IXORTALK;

@ConfigurationProperties("ixortalk")
public class IxorTalkConfigProperties {

    private Security security = new Security();

    private Map<String, ThirdPartyLogin> thirdPartyLogins = newHashMap();

    private Logout logout = new Logout();

    public Security getSecurity() {
        return security;
    }

    public Map<String, ThirdPartyLogin> getThirdPartyLogins() {
        return thirdPartyLogins;
    }

    public Logout getLogout() {
        return logout;
    }

    public static class Security {

        private String loginUrl;

        private UserInfoCache userInfoCache = new UserInfoCache();

        private final Authentication authentication = new Authentication();

        public String getLoginUrl() {
            return loginUrl;
        }

        public UserInfoCache getUserInfoCache() {
            return userInfoCache;
        }

        public void setLoginUrl(String loginUrl) {
            this.loginUrl = loginUrl;
        }

        public Authentication getAuthentication() {
            return authentication;
        }

        public static class Authentication {

            private Set<String> defaultRedirectUris = newHashSet();

            private final Map<String, Oauth> oauthClients = newHashMap();

            public Map<String, Oauth> getOauthClients() {
                return oauthClients;
            }

            public Set<String> getDefaultRedirectUris() {
                return defaultRedirectUris;
            }

            public void setDefaultRedirectUris(Set<String> defaultRedirectUris) {
                this.defaultRedirectUris = defaultRedirectUris;
            }

            public static class Oauth {

                private String clientid;

                private String secret;

                private int tokenValidityInSeconds = 1800;

                private Set<String> authorities = newHashSet();

                private Set<String> scopes = newHashSet("openid", "read", "write");

                private Set<String> authorizedGrantTypes = newHashSet("password", "refresh_token", "authorization_code", "implicit", "client_credentials");

                private Set<String> autoApproveScopes = newHashSet("openid", "read", "write");

                private Set<String> redirectUris = newHashSet();

                public String getClientid() {
                    return clientid;
                }

                public void setClientid(String clientid) {
                    this.clientid = clientid;
                }

                public String getSecret() {
                    return secret;
                }

                public void setSecret(String secret) {
                    this.secret = secret;
                }

                public int getTokenValidityInSeconds() {
                    return tokenValidityInSeconds;
                }

                public void setTokenValidityInSeconds(int tokenValidityInSeconds) {
                    this.tokenValidityInSeconds = tokenValidityInSeconds;
                }

                public Set<String> getAuthorities() {
                    return authorities;
                }

                public void setAuthorities(Set<String> authorities) {
                    this.authorities = authorities;
                }

                public Set<String> getScopes() {
                    return scopes;
                }

                public void setScopes(Set<String> scopes) {
                    this.scopes = scopes;
                }

                public Set<String> getAuthorizedGrantTypes() {
                    return authorizedGrantTypes;
                }

                public void setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
                    this.authorizedGrantTypes = authorizedGrantTypes;
                }

                public Set<String> getAutoApproveScopes() {
                    return autoApproveScopes;
                }

                public void setAutoApproveScopes(Set<String> autoApproveScopes) {
                    this.autoApproveScopes = autoApproveScopes;
                }

                public void setRedirectUris(Set<String> redirectUris) {
                    this.redirectUris = redirectUris;
                }

                public Set<String> getRedirectUris() {
                    return redirectUris;
                }
            }
        }
    }

    public static class UserInfoCache {

        private long ttlInSeconds = 10;

        public long getTtlInSeconds() {
            return ttlInSeconds;
        }

        public void setTtlInSeconds(long ttlInSeconds) {
            this.ttlInSeconds = ttlInSeconds;
        }
    }

    public static class ThirdPartyLogin {

        private String loginPath;
        private ClientResources clientResource;
        private LoginProvider principalExtractorType = IXORTALK;

        public String getLoginPath() {
            return loginPath;
        }

        public void setLoginPath(String loginPath) {
            this.loginPath = loginPath;
        }

        public ClientResources getClientResource() {
            return clientResource;
        }

        public void setClientResource(ClientResources clientResource) {
            this.clientResource = clientResource;
        }

        public LoginProvider getPrincipalExtractorType() {
            return principalExtractorType;
        }

        public void setPrincipalExtractorType(LoginProvider principalExtractorType) {
            this.principalExtractorType = principalExtractorType;
        }
    }

    public static class ClientResources {

        @NestedConfigurationProperty
        private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

        @NestedConfigurationProperty
        private ResourceServerProperties resource = new ResourceServerProperties();

        public AuthorizationCodeResourceDetails getClient() {
            return client;
        }

        public ResourceServerProperties getResource() {
            return resource;
        }
    }

    public static class Logout {

        private String defaultRedirectUri;
        private String redirectUriParamName;
        private String redirectUriIxortalkLogout;

        public void setDefaultRedirectUri(String defaultRedirectUri) {
            this.defaultRedirectUri = defaultRedirectUri;
        }

        public String getDefaultRedirectUri() {
            return defaultRedirectUri;
        }

        public String getRedirectUriParamName() {
            return redirectUriParamName;
        }

        public void setRedirectUriParamName(String redirectUriParamName) {
            this.redirectUriParamName = redirectUriParamName;
        }

        public String getRedirectUriIxortalkLogout() {
            return redirectUriIxortalkLogout;
        }

        public void setRedirectUriIxortalkLogout(String redirectUriIxortalkLogout) {
            this.redirectUriIxortalkLogout = redirectUriIxortalkLogout;
        }
    }
}
