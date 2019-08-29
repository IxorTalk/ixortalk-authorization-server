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
import com.ixortalk.authorization.server.rest.FieldDescriptions.UserProfileFields;
import org.junit.Test;

import static com.ixortalk.authorization.server.domain.AuthorityTestBuilder.authority;
import static com.jayway.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class UserInfoController_Evict_IntegrationTest extends AbstractSpringIntegrationTest {


    @Test
    public void evict() throws JsonProcessingException {
        String accessToken = getAccessTokenWithAuthorizationCode().getValue();

        UserProfile userProfile =
                given(this.restDocSpecification)
                        .auth().preemptive().oauth2(accessToken)
                        .filter(
                                document("user/evict",
                                        preprocessRequest(staticUris(), prettyPrint()),
                                        preprocessResponse(prettyPrint()),
                                        requestHeaders(describeAuthorizationTokenHeader()),
                                        responseFields(
                                                UserProfileFields.NAME,
                                                UserProfileFields.EMAIL,
                                                UserProfileFields.FIRST_NAME,
                                                UserProfileFields.LAST_NAME,
                                                UserProfileFields.PROFILE_PICTURE_URL,
                                                UserProfileFields.AUTHORITIES,
                                                UserProfileFields.LOGIN_PROVIDER
                                        )
                                )
                        )
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
                        .post("/user/evict")
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().as(UserProfile.class);

        assertThat(userProfile.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(userProfile.getAuthorities()).containsOnly(authority(UPDATED_ROLE));
    }
}