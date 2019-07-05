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
package com.ixortalk.authorization.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.ixortalk.authorization.server.config.IxorTalkConfigProperties;
import com.ixortalk.authorization.server.rest.UserProfileRepository;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RedirectConfig;
import com.jayway.restassured.filter.session.SessionFilter;
import com.jayway.restassured.response.Response;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.ixortalk.authorization.server.TestConfigConstants.TEST_CLIENT_ID;
import static com.ixortalk.authorization.server.TestConfigConstants.TEST_CLIENT_REDIRECT_URI;
import static com.ixortalk.authorization.server.TestConfigConstants.TEST_CLIENT_SECRET;
import static com.ixortalk.authorization.server.TestConfigConstants.THIRD_PARTY_LOGIN_IXORTALK;
import static com.ixortalk.test.util.Randomizer.nextString;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.RestAssuredConfig.config;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonMap;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static wiremock.org.apache.http.HttpHeaders.LOCATION;
import static wiremock.org.eclipse.jetty.http.HttpStatus.MOVED_TEMPORARILY_302;

@SpringBootTest(classes = AuthorizationServerApplication.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@RunWith(SpringRunner.class)
public abstract class AbstractSpringIntegrationTest {

    public static final String PRINCIPAL_NAME_IXORTALK = nextString("principalNameIxorTalk");
    public static final String FIRST_NAME_IXORTALK_PRINCIPAL = nextString("firstName");
    public static final String LAST_NAME_IXORTALK_PRINCIPAL = nextString("lastName");

    public static final String PRINCIPAL_NAME_EVENTBRITE = nextString("principalNameEventbrite");
    public static final String FIRST_NAME_EVENTBRITE_PRINCIPAL = nextString("first_name_eventbrite");
    public static final String LAST_NAME_EVENTBRITE_PRINCIPAL = nextString("last_name_eventbrite");
    public static final String PROFILE_PICTURE_URL_IXORTALK_PRINCIPAL = nextString("profilePictureUrl");

    @Rule
    public WireMockRule thirdPartyIxorTalkWireMockRule = new WireMockRule(
            options()
                    .port(65101)
                    .extensions(new ResponseTemplateTransformer(false)));

    @Rule
    public WireMockRule thirdPartyEventbriteWireMockRule = new WireMockRule(
            options()
                    .port(65102)
                    .extensions(new ResponseTemplateTransformer(false)));

    protected Map<String, Object> thirdPartyPrincipalIxorTalk;
    protected Map<String, Object> thirdPartyPrincipalEventbrite;

    protected SessionFilter sessionFilter;

    @Inject
    private CrudRepository<?, ?>[] crudRepositories;

    @Inject
    protected IxorTalkConfigProperties ixorTalkConfigProperties;

    @Inject
    protected UserProfileRepository userProfileRepository;

    @Inject
    protected ObjectMapper objectMapper;

    @LocalServerPort
    protected int port;

    @Value("${server.context-path}")
    protected String contextPath;

    private static String extractAuthorizationCodeFromRedirect(String location) throws URISyntaxException {
        return new URIBuilder(location)
                .getQueryParams()
                .stream()
                .filter(nameValuePair -> nameValuePair.getName().equals("code"))
                .findFirst()
                .map(NameValuePair::getValue)
                .orElseThrow(() -> new IllegalStateException("A code should be available"));
    }

    @Before
    public final void setupRestAssuredAndOrganizationMocking() {
        RestAssured.port = port;
        RestAssured.basePath = contextPath;
        RestAssured.config = config().redirect(RedirectConfig.redirectConfig().followRedirects(false));
    }

    @Before
    public void setupThirdPartyLogins() throws JsonProcessingException {
        Map<String, Object> userInfoIxorTalk = newHashMap();
        userInfoIxorTalk.put("profilePictureUrl", PROFILE_PICTURE_URL_IXORTALK_PRINCIPAL);
        userInfoIxorTalk.put("firstName", FIRST_NAME_IXORTALK_PRINCIPAL);
        userInfoIxorTalk.put("lastName", LAST_NAME_IXORTALK_PRINCIPAL);

        thirdPartyPrincipalIxorTalk = newHashMap();
        thirdPartyPrincipalIxorTalk.put("name", PRINCIPAL_NAME_IXORTALK);
        thirdPartyPrincipalIxorTalk.put("userInfo", userInfoIxorTalk);

        thirdPartyPrincipalEventbrite = newHashMap();
        thirdPartyPrincipalEventbrite.put("emails", newArrayList(singletonMap("email", PRINCIPAL_NAME_EVENTBRITE)));
        thirdPartyPrincipalEventbrite.put("first_name", FIRST_NAME_EVENTBRITE_PRINCIPAL);
        thirdPartyPrincipalEventbrite.put("last_name", LAST_NAME_EVENTBRITE_PRINCIPAL);

        stubThirdPartyOAuth2Login(thirdPartyIxorTalkWireMockRule, thirdPartyPrincipalIxorTalk);
        stubThirdPartyOAuth2Login(thirdPartyEventbriteWireMockRule, thirdPartyPrincipalEventbrite);
    }

    protected String getLocalURL() {
        return "http://localhost:" + port + "" + contextPath;
    }

    @After
    public void cleanCrudRepositories() {
        stream(crudRepositories).forEach(CrudRepository::deleteAll);
    }

    private void stubThirdPartyOAuth2Login(WireMockRule thirdPartyWireMockRule, Map<String, Object> thirdPartyPrincipalResponse) throws JsonProcessingException {
        thirdPartyWireMockRule.stubFor(get(urlPathMatching("/oauth/authorize?.*"))
                .willReturn(
                        temporaryRedirect("{{request.requestLine.query.redirect_uri}}?state={{request.requestLine.query.state}}&code=" + nextString("oauth2-code"))
                                .withTransformers("response-template")));
        thirdPartyWireMockRule.stubFor(post(urlEqualTo("/oauth/token")).willReturn(okJson("{\"access_token\":\"" + nextString("access-token-") + "\"}")));
        thirdPartyWireMockRule.stubFor(get(urlPathEqualTo("/user-info")).willReturn(okJson(objectMapper.writeValueAsString(thirdPartyPrincipalResponse))));
    }

    @Before
    public void initSessionFilter() {
        this.sessionFilter = new SessionFilter();
    }

    protected Response performOAuth2Login(TestConfigConstants thirdPartyLoginProvider) {
        String location =
                given()
                        .filter(sessionFilter)
                        .when()
                        .get(ixorTalkConfigProperties.getThirdPartyLogins().get(thirdPartyLoginProvider.configValue()).getLoginPath())
                        .then()
                        .statusCode(MOVED_TEMPORARILY_302)
                        .extract().header(LOCATION);

        location =
                given()
                        .filter(sessionFilter)
                        .when()
                        .get(location)
                        .then()
                        .statusCode(MOVED_TEMPORARILY_302)
                        .extract().header(LOCATION);

        location =
                given()
                        .filter(sessionFilter)
                        .when()
                        .get(location)
                        .then()
                        .statusCode(MOVED_TEMPORARILY_302)
                        .extract().header(LOCATION);

        return given()
                .filter(sessionFilter)
                .when()
                .get(location);
    }

    protected OAuth2AccessToken getAccessTokenWithAuthorizationCode() {
        return given()
                .filter(sessionFilter)
                .auth().preemptive().basic(TEST_CLIENT_ID.configValue(), TEST_CLIENT_SECRET.configValue())
                .parameters("grant_type", "authorization_code")
                .parameters("code", getAuthorizationCode(THIRD_PARTY_LOGIN_IXORTALK))
                .parameters("redirect_uri", TEST_CLIENT_REDIRECT_URI.configValue())
                .when()
                .post("/oauth/token")
                .then()
                .statusCode(HTTP_OK)
                .extract().as(OAuth2AccessToken.class);
    }

    protected OAuth2AccessToken getAccessTokenWithRefreshToken(OAuth2RefreshToken refreshToken) {
        return given()
                .filter(sessionFilter)
                .auth().preemptive().basic(TEST_CLIENT_ID.configValue(), TEST_CLIENT_SECRET.configValue())
                .parameters("grant_type", "refresh_token")
                .parameters("refresh_token", refreshToken.getValue())
                .when()
                .post("/oauth/token")
                .then()
                .log().all()
                .statusCode(HTTP_OK)
                .extract().as(OAuth2AccessToken.class);
    }

    private String getAuthorizationCode(TestConfigConstants thirdPartyLoginProvider)  {
        given()
                .filter(sessionFilter)
                .urlEncodingEnabled(false)
                .parameters("client_id", TEST_CLIENT_ID.configValue())
                .parameters("redirect_uri", TEST_CLIENT_REDIRECT_URI.configValue())
                .parameters("response_type", "code")
                .parameters("state", nextString("state-"))
                .when()
                .get("/oauth/authorize");

        try {
            return extractAuthorizationCodeFromRedirect(
                    performOAuth2Login(thirdPartyLoginProvider)
                            .then()
                            .statusCode(MOVED_TEMPORARILY_302)
                            .extract().header(LOCATION));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Error retrieving authorization code from redirect: " + e.getMessage(), e);
        }
    }
}
