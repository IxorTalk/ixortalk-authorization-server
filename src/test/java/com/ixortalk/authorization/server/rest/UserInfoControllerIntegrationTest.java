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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ixortalk.authorization.server.AbstractSpringIntegrationTest;
import com.ixortalk.authorization.server.domain.UserProfile;
import org.junit.Test;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.google.common.collect.Lists.newArrayList;
import static com.ixortalk.authorization.server.TestConfigConstants.THIRD_PARTY_LOGIN_IXORTALK_CLIENT_ID;
import static com.ixortalk.authorization.server.domain.AuthorityTestBuilder.authority;
import static com.ixortalk.authorization.server.domain.LoginProvider.IXORTALK;
import static com.ixortalk.authorization.server.domain.UserProfileTestBuilder.aUserProfile;
import static com.ixortalk.test.util.Randomizer.nextString;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class UserInfoControllerIntegrationTest extends AbstractSpringIntegrationTest {

    private static final String UPDATED_FIRST_NAME = "updatedFirstName";
    private static final String UPDATED_ROLE = "ROLE_UPDATED";
    private static final String REFRESHED_ACCESS_TOKEN = "refreshedAccessToken";

    @Test
    public void getUserInfo_NoProfileExists() {
        UserProfile userProfile =
                given()
                        .auth().preemptive().oauth2(getAccessTokenWithAuthorizationCode().getValue())
                        .when()
                        .get("/user")
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().as(UserProfile.class);

        assertThat(userProfile)
                .isEqualToIgnoringGivenFields(
                        aUserProfile()
                                .withName(PRINCIPAL_NAME_IXORTALK)
                                .withEmail(PRINCIPAL_NAME_IXORTALK)
                                .withFirstName(FIRST_NAME_IXORTALK_PRINCIPAL)
                                .withLastName(LAST_NAME_IXORTALK_PRINCIPAL)
                                .withProfilePictureUrl(PROFILE_PICTURE_URL_IXORTALK_PRINCIPAL)
                                .withAuthorities(authority(ROLE_IXORTALK_ROLE_1), authority(ROLE_IXORTALK_ROLE_2))
                                .withLoginProvider(IXORTALK)
                                .build()
                );
    }

    @Test
    public void getUserInfo_ProfileExists() {

        userProfileRestResource.save(
                aUserProfile()
                        .withName(PRINCIPAL_NAME_IXORTALK)
                        .withEmail(PRINCIPAL_NAME_IXORTALK)
                        .withFirstName(nextString("persistedFirstName"))
                        .withLastName(nextString("persistedLastName"))
                        .withProfilePictureUrl(nextString("persistedProfilePictureUrl"))
                        .withLoginProvider(IXORTALK)
                        .build());

        UserProfile userProfile =
                given()
                        .auth().preemptive().oauth2(getAccessTokenWithAuthorizationCode().getValue())
                        .when()
                        .get("/user")
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().as(UserProfile.class);

        assertThat(userProfile)
                .isEqualToIgnoringGivenFields(
                        aUserProfile()
                                .withName(PRINCIPAL_NAME_IXORTALK)
                                .withEmail(PRINCIPAL_NAME_IXORTALK)
                                .withFirstName(FIRST_NAME_IXORTALK_PRINCIPAL)
                                .withLastName(LAST_NAME_IXORTALK_PRINCIPAL)
                                .withProfilePictureUrl(PROFILE_PICTURE_URL_IXORTALK_PRINCIPAL)
                                .withAuthorities(authority(ROLE_IXORTALK_ROLE_1), authority(ROLE_IXORTALK_ROLE_2))
                                .withLoginProvider(IXORTALK)
                                .build(),
                        "id"
                );
    }

    @Test
    public void getUserInfo_WithRefreshToken() {

        userProfileRestResource.save(
                aUserProfile()
                        .withName(PRINCIPAL_NAME_IXORTALK)
                        .withEmail(PRINCIPAL_NAME_IXORTALK)
                        .withFirstName(nextString("persistedFirstName"))
                        .withLastName(nextString("persistedLastName"))
                        .withProfilePictureUrl(nextString("persistedProfilePictureUrl"))
                        .withLoginProvider(IXORTALK)
                        .build());

        UserProfile userProfile =
                given()
                        .auth().preemptive().oauth2(getAccessTokenWithRefreshToken(getAccessTokenWithAuthorizationCode().getRefreshToken()).getValue())
                        .when()
                        .get("/user")
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().as(UserProfile.class);

        assertThat(userProfile)
                .isEqualToIgnoringGivenFields(
                        aUserProfile()
                                .withName(PRINCIPAL_NAME_IXORTALK)
                                .withEmail(PRINCIPAL_NAME_IXORTALK)
                                .withFirstName(FIRST_NAME_IXORTALK_PRINCIPAL)
                                .withLastName(LAST_NAME_IXORTALK_PRINCIPAL)
                                .withProfilePictureUrl(PROFILE_PICTURE_URL_IXORTALK_PRINCIPAL)
                                .withAuthorities(authority(ROLE_IXORTALK_ROLE_1), authority(ROLE_IXORTALK_ROLE_2))
                                .withLoginProvider(IXORTALK)
                                .build(),
                        "id"
                );
    }

    @Test
    public void getUserInfo_ThirdPartyProfileUpdated() throws JsonProcessingException {
        String accessToken = getAccessTokenWithAuthorizationCode().getValue();

        UserProfile userProfile =
                given()
                        .auth().preemptive().oauth2(accessToken)
                        .when()
                        .get("/user")
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().as(UserProfile.class);

        assertThat(userProfile.getFirstName()).isEqualTo(FIRST_NAME_IXORTALK_PRINCIPAL);
        assertThat(userProfile.getAuthorities()).containsOnly(authority(ROLE_IXORTALK_ROLE_1), authority(ROLE_IXORTALK_ROLE_2));

        updateThirdPartyUserInfo();

        userProfile =
                given()
                        .auth().preemptive().oauth2(accessToken)
                        .when()
                        .get("/user")
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().as(UserProfile.class);

        assertThat(userProfile.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(userProfile.getAuthorities()).containsOnly(authority(UPDATED_ROLE));
    }

    @Test
    public void getUserInfo_ThirdPartyAccessTokenProperlyRefreshed() throws JsonProcessingException {
        String accessToken = getAccessTokenWithAuthorizationCode().getValue();

        given()
                .auth().preemptive().oauth2(accessToken)
                .when()
                .get("/user")
                .then()
                .statusCode(HTTP_OK);

        expirePersistedThirdPartyAccessToken();
        updateThirdPartyUserInfo();

        thirdPartyIxorTalkWireMockRule.stubFor(
                post(urlEqualTo("/oauth/token"))
                        .withRequestBody(containing("grant_type=refresh_token&refresh_token=" + IXORTALK_THIRD_PARTY_REFRESH_TOKEN))
                        .willReturn(okJson(objectMapper.writeValueAsString(createOAuth2AccessToken(REFRESHED_ACCESS_TOKEN, nextString("refreshToken"))))));

        UserProfile userProfile =
                given()
                        .auth().preemptive().oauth2(accessToken)
                        .when()
                        .get("/user")
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().as(UserProfile.class);

        assertThat(userProfile.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(userProfile.getAuthorities()).containsOnly(authority(UPDATED_ROLE));
        assertThat(thirdPartyTokenStore.findTokensByClientIdAndUserName(THIRD_PARTY_LOGIN_IXORTALK_CLIENT_ID.configValue(), PRINCIPAL_NAME_IXORTALK))
                .hasSize(1)
                .extracting(OAuth2AccessToken::getValue)
                .containsExactly(REFRESHED_ACCESS_TOKEN);
    }

    private void expirePersistedThirdPartyAccessToken() {
        OAuth2AccessToken thirdPartyOAuth2AccessToken = thirdPartyTokenStore.readAccessToken(IXORTALK_THIRD_PARTY_ACCESS_TOKEN);
        OAuth2Authentication thirdPartyOAuth2Authentication = thirdPartyTokenStore.readAuthentication(thirdPartyOAuth2AccessToken);
        ((DefaultOAuth2AccessToken) thirdPartyOAuth2AccessToken).setExpiration(new Date(currentTimeMillis() - 1));
        thirdPartyTokenStore.storeAccessToken(thirdPartyOAuth2AccessToken, thirdPartyOAuth2Authentication);
    }

    private void updateThirdPartyUserInfo() throws JsonProcessingException {
        userInfoIxorTalk.put("firstName", UPDATED_FIRST_NAME);
        thirdPartyPrincipalIxorTalk.put("authorities", newArrayList(singletonMap("name", UPDATED_ROLE)));
        thirdPartyPrincipalIxorTalk.put("userInfo", userInfoIxorTalk);
        stubThirdPartyUserInfo(thirdPartyIxorTalkWireMockRule, thirdPartyPrincipalIxorTalk);
    }
}