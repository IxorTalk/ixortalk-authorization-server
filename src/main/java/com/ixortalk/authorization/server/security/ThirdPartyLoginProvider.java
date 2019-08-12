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
package com.ixortalk.authorization.server.security;

import com.ixortalk.authorization.server.domain.LoginProvider;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

public class ThirdPartyLoginProvider {

    private OAuth2ProtectedResourceDetails resource;
    private LoginProvider loginProvider;
    private String loginPath;
    private OAuth2RestTemplate oAuth2RestTemplate;
    private UserInfoTokenServices userInfoTokenServices;

    public ThirdPartyLoginProvider(
            OAuth2ProtectedResourceDetails resource,
            LoginProvider loginProvider,
            String loginPath,
            OAuth2RestTemplate oAuth2RestTemplate,
            UserInfoTokenServices userInfoTokenServices) {
        this.resource = resource;
        this.loginProvider = loginProvider;
        this.loginPath = loginPath;
        this.oAuth2RestTemplate = oAuth2RestTemplate;
        this.userInfoTokenServices = userInfoTokenServices;
    }

    public OAuth2ProtectedResourceDetails getResource() {
        return resource;
    }

    public LoginProvider getLoginProvider() {
        return loginProvider;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public OAuth2RestTemplate getOAuth2RestTemplate() {
        return oAuth2RestTemplate;
    }

    public UserInfoTokenServices getUserInfoTokenServices() {
        return userInfoTokenServices;
    }
}
