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

import com.ixortalk.authorization.server.AbstractSpringIntegrationTest;
import com.ixortalk.authorization.server.domain.UserProfile;
import org.junit.Test;

import static com.ixortalk.authorization.server.domain.AuthorityTestBuilder.authority;
import static com.ixortalk.authorization.server.domain.LoginProvider.IXORTALK;
import static com.ixortalk.authorization.server.domain.UserProfileTestBuilder.aUserProfile;
import static com.ixortalk.test.util.Randomizer.nextString;
import static com.jayway.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class UserInfoControllerIntegrationTest extends AbstractSpringIntegrationTest {

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
}