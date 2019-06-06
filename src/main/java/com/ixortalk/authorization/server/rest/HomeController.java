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

import com.ixortalk.authorization.server.config.IxorTalkConfigProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Inject
    private IxorTalkConfigProperties ixorTalkConfigProperties;

    @RequestMapping("/login")
    @ResponseBody
    public final String login() {
        // TODO wj #2 login page
        return "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "\n" +
                ixorTalkConfigProperties
                        .getThirdPartyLogins()
                        .keySet()
                        .stream()
                        .map(thirdParty ->
                                "<a href=\"/authorization-server"
                                        + ixorTalkConfigProperties.getThirdPartyLogins().get(thirdParty).getLoginPath()
                                        + "\">Login with "
                                        + thirdParty
                                        + "</a>\n")
                        .collect(Collectors.joining("<br/>"))
                +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>\n";
    }
}