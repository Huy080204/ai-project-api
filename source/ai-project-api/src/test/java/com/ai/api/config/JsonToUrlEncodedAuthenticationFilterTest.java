package com.ai.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletRequest;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JsonToUrlEncodedAuthenticationFilterTest {

    private final JsonToUrlEncodedAuthenticationFilter filter = new JsonToUrlEncodedAuthenticationFilter();

    @Test
    void shouldExposeJsonBodyAsParametersWhenTokenRequestIsJson() throws Exception {
        MockHttpServletRequest request = tokenRequest("application/json",
                "{\"grant_type\":\"password\",\"username\":\"admin\",\"password\":\"secret\"}");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        ServletRequest forwarded = chain.getRequest();
        assertThat(forwarded).isNotSameAs(request);
        assertThat(forwarded.getParameter("grant_type")).isEqualTo("password");
        assertThat(forwarded.getParameter("username")).isEqualTo("admin");
        assertThat(forwarded.getParameter("password")).isEqualTo("secret");
        assertThat(forwarded.getParameterMap()).containsOnlyKeys("grant_type", "username", "password");
    }

    @Test
    void shouldReturnEmptyStringWhenJsonParameterIsMissing() throws Exception {
        MockHttpServletRequest request = tokenRequest("application/json", "{\"username\":\"admin\"}");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest().getParameter("password")).isEmpty();
    }

    @Test
    void shouldForwardOriginalRequestWhenContentTypeIsNotJson() throws Exception {
        MockHttpServletRequest request = tokenRequest("application/x-www-form-urlencoded", "username=admin");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void shouldForwardOriginalRequestWhenPathIsNotToken() throws Exception {
        MockHttpServletRequest request = tokenRequest("application/json", "{\"username\":\"admin\"}");
        request.setServletPath("/v1/account/list");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }

    private MockHttpServletRequest tokenRequest(String contentType, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/token");
        request.setServletPath("/api/token");
        request.setContentType(contentType);
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }
}
