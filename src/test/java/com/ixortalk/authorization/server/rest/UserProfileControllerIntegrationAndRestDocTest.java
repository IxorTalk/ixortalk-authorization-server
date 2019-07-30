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
import com.jayway.restassured.path.json.JsonPath;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.ixortalk.authorization.server.domain.UserProfileTestBuilder.aUserProfile;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class UserProfileControllerIntegrationAndRestDocTest extends AbstractSpringIntegrationTest {

    private static final String USER_A = "user a";
    private static final String USER_B = "user b";
    private static final String USER_C = "user c";

    @Before
    public void before() {
        userProfileRestResource.save(newArrayList(
                aUserProfile().withName(USER_A).build(),
                aUserProfile().withName(USER_B).build(),
                aUserProfile().withName(USER_C).build()
        ));
    }

    @Test
    public void asAdmin() {

        JsonPath jsonPath =
                given(restDocSpecification)
                        .auth().preemptive().oauth2(adminToken().getValue())
                        .filter(
                                document("user-profiles/get-all/as-admin",
                                        preprocessRequest(staticUris(), prettyPrint()),
                                        preprocessResponse(prettyPrint()),
                                        requestHeaders(describeAuthorizationTokenHeader()))
                        )
                .when()
                        .contentType(JSON)
                        .get("/user-profiles")
                .then()
                        .statusCode(HTTP_OK)
                        .extract().jsonPath();

        assertThat(jsonPath.getList("_embedded.userProfiles.name.flatten()")).hasSize(3).containsOnly(USER_A, USER_B, USER_C);
    }


    @Test
    public void asUser() {

        given(restDocSpecification)
                .auth().preemptive().oauth2(userToken().getValue())
                .filter(
                        document("user-profiles/get-all/as-user",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(describeAuthorizationTokenHeader()))
                )
        .when()
                .contentType(JSON)
                .get("/user-profiles")
        .then()
                .statusCode(HTTP_FORBIDDEN);
    }
}