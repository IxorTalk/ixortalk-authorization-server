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
package com.ixortalk.authorization.server.security.thirdparty;

import com.ixortalk.authorization.server.domain.Authority;
import com.ixortalk.authorization.server.domain.UserProfile;
import com.ixortalk.authorization.server.rest.UserProfileRestResource;
import com.ixortalk.authorization.server.security.IxorTalkPrincipal;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.inject.Inject;
import javax.inject.Named;

import static java.util.stream.Collectors.toSet;

@Named
public class ThirdPartyProfileService {

    @Inject
    private UserProfileRestResource userProfileRestResource;

    @Inject
    private ThirdPartyLoginProviders thirdPartyLoginProviders;

    @Inject
    private TokenStore tokenStore;

    @Inject
    private ThirdPartyTokenService thirdPartyTokenService;

    public void updateProfile(OAuth2Authentication oAuth2Authentication) {
        if (!(oAuth2Authentication.getPrincipal() instanceof IxorTalkPrincipal)) {
            return;
        }

        IxorTalkPrincipal ixorTalkPrincipal = (IxorTalkPrincipal) oAuth2Authentication.getPrincipal();

        if (isThirdPartyAuthentication(oAuth2Authentication)) {
            thirdPartyTokenService.storeThirdPartyToken(ixorTalkPrincipal, oAuth2Authentication);
        }

        userProfileRestResource.save(
                userProfileRestResource.findByEmail(ixorTalkPrincipal.getName())
                        .map(userProfile -> userProfile.assertCorrectProvider(ixorTalkPrincipal.getLoginProvider()))
                        .map(existing ->
                                existing.update(
                                        ixorTalkPrincipal.getName(),
                                        ixorTalkPrincipal.getName(),
                                        ixorTalkPrincipal.getFirstName(),
                                        ixorTalkPrincipal.getLastName(),
                                        ixorTalkPrincipal.getProfilePictureUrl(),
                                        oAuth2Authentication
                                                .getAuthorities()
                                                .stream()
                                                .map(GrantedAuthority::getAuthority)
                                                .map(Authority::authority)
                                                .collect(toSet()),
                                        ixorTalkPrincipal.getLoginProvider()
                                ))
                        .orElseGet(() ->
                                new UserProfile(
                                        ixorTalkPrincipal.getName(),
                                        ixorTalkPrincipal.getName(),
                                        ixorTalkPrincipal.getFirstName(),
                                        ixorTalkPrincipal.getLastName(),
                                        ixorTalkPrincipal.getProfilePictureUrl(),
                                        oAuth2Authentication
                                                .getAuthorities()
                                                .stream()
                                                .map(GrantedAuthority::getAuthority)
                                                .map(Authority::authority)
                                                .collect(toSet()),
                                        ixorTalkPrincipal.getLoginProvider()
                                )));
    }

    private boolean isThirdPartyAuthentication(OAuth2Authentication oAuth2Authentication) {
        return thirdPartyLoginProviders.getThirdPartyClientIds().contains(oAuth2Authentication.getOAuth2Request().getClientId());
    }

    public void refreshThirdPartyPrincipal(OAuth2Authentication oAuth2Authentication) {
        // TODO wj #19 concurrency

        Pair<ThirdPartyLoginProvider, OAuth2AccessToken> storedThirdPartyOAuth2AccessToken = thirdPartyTokenService.getStoredThirdPartyToken(oAuth2Authentication);

        ThirdPartyLoginProvider thirdPartyLoginProvider = storedThirdPartyOAuth2AccessToken.getFirst();
        OAuth2AccessToken oAuth2AccessToken = storedThirdPartyOAuth2AccessToken.getSecond();

        thirdPartyLoginProvider.getOAuth2RestTemplate().getOAuth2ClientContext().setAccessToken(oAuth2AccessToken);

        OAuth2AccessToken accessToken;
        try {
            accessToken = thirdPartyLoginProvider.getOAuth2RestTemplate().getAccessToken();
        } catch (OAuth2Exception e) {
            throw new BadCredentialsException("Could not obtain access token", e);
        }

        OAuth2Authentication refreshedThirdPartyOAuth2Authentication = thirdPartyLoginProvider.getUserInfoTokenServices().loadAuthentication(accessToken.getValue());
        OAuth2Authentication refreshedInternalOAuth2Authentication = new OAuth2Authentication(oAuth2Authentication.getOAuth2Request(), refreshedThirdPartyOAuth2Authentication);

        tokenStore.storeAccessToken(tokenStore.getAccessToken(refreshedInternalOAuth2Authentication), refreshedInternalOAuth2Authentication);

        updateProfile(refreshedThirdPartyOAuth2Authentication);
    }


}
