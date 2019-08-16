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

import com.ixortalk.authorization.server.security.IxorTalkPrincipal;
import com.ixortalk.authorization.server.security.UserDetails;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

import static java.util.Optional.ofNullable;

@Named
public class ThirdPartyTokenService {

    @Inject
    private TokenStore thirdPartyTokenStore;

    @Inject
    private ThirdPartyLoginProviders thirdPartyLoginProviders;

    public void storeThirdPartyToken(IxorTalkPrincipal ixorTalkPrincipal, OAuth2Authentication oAuth2Authentication) {
        OAuth2AccessToken accessToken;
        ThirdPartyLoginProvider loginProvider = thirdPartyLoginProviders.getLoginProvider(ixorTalkPrincipal.getLoginProvider());

        try {
            accessToken = loginProvider.getOAuth2RestTemplate().getAccessToken();
        } catch (OAuth2Exception e) {
            throw new BadCredentialsException("Could not obtain access token", e);
        } catch (UserRedirectRequiredException e) {
            // should not happen
            return;
        }

        ofNullable(thirdPartyTokenStore.getAccessToken(oAuth2Authentication))
                .ifPresent(existingOAuth2AccessToken -> thirdPartyTokenStore.removeAccessToken(existingOAuth2AccessToken));

        thirdPartyTokenStore.storeAccessToken(accessToken, oAuth2Authentication);
    }

    public Pair<ThirdPartyLoginProvider, OAuth2AccessToken> getStoredThirdPartyToken(OAuth2Authentication oAuth2Authentication) {
        return oAuth2Authentication.getUserAuthentication() instanceof OAuth2Authentication ?
                usingAuthentication(oAuth2Authentication) :
                usingRefreshedAuthentication(oAuth2Authentication);
    }

    private Pair<ThirdPartyLoginProvider, OAuth2AccessToken> usingRefreshedAuthentication(OAuth2Authentication oAuth2Authentication) {
        UserDetails userDetails = (UserDetails) oAuth2Authentication.getUserAuthentication().getPrincipal();

        ThirdPartyLoginProvider thirdPartyLoginProvider = thirdPartyLoginProviders.getLoginProvider(userDetails.getLoginProvider());
        Collection<OAuth2AccessToken> tokensByClientIdAndUserName =
                thirdPartyTokenStore
                        .findTokensByClientIdAndUserName(
                                thirdPartyLoginProvider.getResource().getClientId(),
                                userDetails.getUsername());

        if (tokensByClientIdAndUserName.size() != 1) {
            throw new OAuth2Exception("Unable to refresh third party token");
        }

        return Pair.of(thirdPartyLoginProvider, tokensByClientIdAndUserName.iterator().next());
    }

    private Pair<ThirdPartyLoginProvider, OAuth2AccessToken> usingAuthentication(OAuth2Authentication oAuth2Authentication) {
        OAuth2Authentication thirdPartyOAuth2Authentication = (OAuth2Authentication) oAuth2Authentication.getUserAuthentication();

        return Pair.of(
                thirdPartyLoginProviders.getLoginProvider(((IxorTalkPrincipal) oAuth2Authentication.getPrincipal()).getLoginProvider()),
                thirdPartyTokenStore.getAccessToken(thirdPartyOAuth2Authentication));
    }
}
