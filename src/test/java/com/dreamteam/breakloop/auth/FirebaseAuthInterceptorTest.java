package com.dreamteam.breakloop.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebaseAuthInterceptorTest {

    private final FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);
    private final FirebaseAuthInterceptor interceptor =
            new FirebaseAuthInterceptor(() -> firebaseAuth);
    private final MockHttpServletResponse response =
            new MockHttpServletResponse();

    private MockHttpServletRequest request(String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "PUT", "/api/users/u1/activity-logs/l1");
        request.setAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                Map.of("uid", "u1"));
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        return request;
    }

    private FirebaseToken tokenOf(String uid) {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn(uid);
        return token;
    }

    @Test
    void missingHeaderIs401() throws Exception {
        assertFalse(interceptor.preHandle(request(null), response, null));
        assertEquals(401, response.getStatus());
    }

    @Test
    void nonBearerSchemeIs401() throws Exception {
        assertFalse(interceptor.preHandle(request("Basic abc"), response, null));
        assertEquals(401, response.getStatus());
    }

    @Test
    void rejectedTokenIs401() throws Exception {
        when(firebaseAuth.verifyIdToken("bad"))
                .thenThrow(mock(FirebaseAuthException.class));

        assertFalse(interceptor.preHandle(
                request("Bearer bad"), response, null));
        assertEquals(401, response.getStatus());
    }

    @Test
    void emptyTokenIs401() throws Exception {
        when(firebaseAuth.verifyIdToken(""))
                .thenThrow(new IllegalArgumentException("empty"));

        assertFalse(interceptor.preHandle(request("Bearer "), response, null));
        assertEquals(401, response.getStatus());
    }

    @Test
    void tokenOfAnotherUserIs403() throws Exception {
        FirebaseToken token = tokenOf("other");
        when(firebaseAuth.verifyIdToken("good")).thenReturn(token);

        assertFalse(interceptor.preHandle(
                request("Bearer good"), response, null));
        assertEquals(403, response.getStatus());
    }

    @Test
    void tokenOfSameUserPasses() throws Exception {
        FirebaseToken token = tokenOf("u1");
        when(firebaseAuth.verifyIdToken("good")).thenReturn(token);

        assertTrue(interceptor.preHandle(
                request("Bearer good"), response, null));
        assertEquals(200, response.getStatus());
    }

    @Test
    void preflightPassesWithoutToken() throws Exception {
        MockHttpServletRequest request = request(null);
        request.setMethod("OPTIONS");

        assertTrue(interceptor.preHandle(request, response, null));
    }
}
