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

import com.ixortalk.authorization.server.domain.UserProfile;
import com.ixortalk.authorization.server.rest.UserProfileRestResource;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.inject.Inject;
import java.util.Optional;

public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Inject
    private UserProfileRestResource userProfileRestResource;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) throws IllegalArgumentException {

        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) event.getAuthentication();

        if (!(oAuth2Authentication.getPrincipal() instanceof IxorTalkPrincipal)) {
            return;
        }

        IxorTalkPrincipal ixorTalkPrincipal = (IxorTalkPrincipal) oAuth2Authentication.getPrincipal();

        Optional<UserProfile> existingProfile = userProfileRestResource.findByEmail(ixorTalkPrincipal.getName());

        existingProfile
                .filter(userProfile -> ixorTalkPrincipal.getLoginProvider() != userProfile.getLoginProvider())
                .ifPresent(userProfile -> {
                    throw new IllegalArgumentException("Different profile already exist for principal " + ixorTalkPrincipal.getName());
                });

        if (!existingProfile.isPresent()) {
            userProfileRestResource.save(
                    new UserProfile(
                            ixorTalkPrincipal.getName(),
                            ixorTalkPrincipal.getName(),
                            ixorTalkPrincipal.getFirstName(),
                            ixorTalkPrincipal.getLastName(),
                            ixorTalkPrincipal.getLoginProvider()
                    ));
        }
    }
}
