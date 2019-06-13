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
package com.ixortalk.authorization.server.rest;

import com.ixortalk.authorization.server.domain.UserProfile;
import com.ixortalk.authorization.server.security.IxorTalkPrincipal;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Optional;

@RestController
public class UserInfoController {

    @Inject
    private UserProfileRestResource userProfileRestResource;

    @RequestMapping("/user")
    public Object user(Principal principal) {
        if (!(principal instanceof OAuth2Authentication && ((OAuth2Authentication) principal).getPrincipal() instanceof IxorTalkPrincipal)) {
            return principal;
        }

        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) principal;
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

        return new PrincipalDTO(
                ixorTalkPrincipal,
                oAuth2Authentication.getAuthorities(),
                ixorTalkPrincipal.getUserInfo());
    }
}

